package cn.autoforged.yuzusoft.transformation;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Map;

/**
 * 生物专属技能系统。
 * <p>
 * 玩家变身成下列生物后，手持变身法杖左键即可释放对应技能（普通攻击被拦截）：
 * <ul>
 *     <li>苦力怕 —— 自爆：以玩家为爆心的 3 格爆炸，玩家自身免疫（爆炸源排除施放者）；</li>
 *     <li>骷髅/流浪者/沼骸/幻术师/掠夺者 —— 射箭：向视线方向射出箭矢（不消耗箭）；</li>
 *     <li>末影人 —— 瞬移：沿视线方向随机瞬移 6~30 格，落到安全地面；</li>
 *     <li>烈焰人 —— 连发火球：一次释放连射 3 发小火球；</li>
 *     <li>铁傀儡 —— 重击击退：对周围 4 格内生物造成伤害并强力击飞；</li>
 *     <li>潜影贝 —— 潜影弹：向 24 格内最近的生物发射一枚追踪潜影弹；</li>
 *     <li>唤魔者 —— 尖牙：沿视线方向打出一排尖牙；</li>
 *     <li>恶魂 —— 大火球：发射一枚爆裂火球（爆炸源为玩家，不会炸到自己）；</li>
 *     <li>旋风人 —— 风弹：发射一枚击退风弹；</li>
 *     <li>凋灵 —— 凋灵之首：发射一枚附带凋零的凋灵之首；</li>
 *     <li>末影龙 —— 龙息弹：发射一枚会留下持续伤害龙息云的龙息弹；</li>
 *     <li>雪傀儡 —— 雪球：抛出一枚雪球；</li>
 *     <li>羊驼/行商羊驼 —— 口水弹：吐出一口口水；</li>
 *     <li>女巫 —— 喷溅药水：向 16 格内最近的生物投掷瞬间伤害药水；</li>
 *     <li>溺尸 —— 投掷三叉戟；</li>
 *     <li>守卫者/远古守卫者 —— 激光：向视线方向发射一道激光；</li>
 *     <li>监守者 —— 音爆：命中造成 10 点伤害并强力击退。</li>
 * </ul>
 * yuzusoft 生物的专属技能见 {@link YuzusoftSkills}。
 * 此外，天生会飞的生物（蝙蝠/蜜蜂/鹦鹉等）变身即获得飞行能力，蜘蛛类变身可爬墙，
 * 均无需左键触发；这些无需冷却，故不占技能位。
 * 每个技能有独立冷却，冷却以玩家持久化数据中的到期游戏刻记录。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class SkillSystem {
    public enum Skill {
        CREEPER, SKELETON, ENDERMAN, BLAZE, IRON_GOLEM, SHULKER,
        EVOKER, GHAST, BREEZE, WITHER, DRAGON, SNOW_GOLEM, LLAMA, WITCH, DROWNED, GUARDIAN, WARDEN
    }

    private static final Map<EntityType<?>, Skill> SKILLS = Map.ofEntries(
            Map.entry(EntityType.CREEPER, Skill.CREEPER),
            Map.entry(EntityType.SKELETON, Skill.SKELETON),
            Map.entry(EntityType.STRAY, Skill.SKELETON),
            Map.entry(EntityType.BOGGED, Skill.SKELETON),
            Map.entry(EntityType.ILLUSIONER, Skill.SKELETON),
            Map.entry(EntityType.PILLAGER, Skill.SKELETON),
            Map.entry(EntityType.ENDERMAN, Skill.ENDERMAN),
            Map.entry(EntityType.BLAZE, Skill.BLAZE),
            Map.entry(EntityType.IRON_GOLEM, Skill.IRON_GOLEM),
            Map.entry(EntityType.SHULKER, Skill.SHULKER),
            Map.entry(EntityType.EVOKER, Skill.EVOKER),
            Map.entry(EntityType.GHAST, Skill.GHAST),
            Map.entry(EntityType.BREEZE, Skill.BREEZE),
            Map.entry(EntityType.WITHER, Skill.WITHER),
            Map.entry(EntityType.ENDER_DRAGON, Skill.DRAGON),
            Map.entry(EntityType.SNOW_GOLEM, Skill.SNOW_GOLEM),
            Map.entry(EntityType.LLAMA, Skill.LLAMA),
            Map.entry(EntityType.TRADER_LLAMA, Skill.LLAMA),
            Map.entry(EntityType.WITCH, Skill.WITCH),
            Map.entry(EntityType.DROWNED, Skill.DROWNED),
            Map.entry(EntityType.GUARDIAN, Skill.GUARDIAN),
            Map.entry(EntityType.ELDER_GUARDIAN, Skill.GUARDIAN),
            Map.entry(EntityType.WARDEN, Skill.WARDEN));

    private static final Map<Skill, Integer> COOLDOWN = Map.ofEntries(
            Map.entry(Skill.CREEPER, 100),
            Map.entry(Skill.SKELETON, 20),
            Map.entry(Skill.ENDERMAN, 40),
            Map.entry(Skill.BLAZE, 20),
            Map.entry(Skill.IRON_GOLEM, 20),
            Map.entry(Skill.SHULKER, 20),
            Map.entry(Skill.EVOKER, 40),
            Map.entry(Skill.GHAST, 20),
            Map.entry(Skill.BREEZE, 20),
            Map.entry(Skill.WITHER, 20),
            Map.entry(Skill.DRAGON, 60),
            Map.entry(Skill.SNOW_GOLEM, 20),
            Map.entry(Skill.LLAMA, 20),
            Map.entry(Skill.WITCH, 20),
            Map.entry(Skill.DROWNED, 20),
            Map.entry(Skill.GUARDIAN, 20),
            Map.entry(Skill.WARDEN, 20));

    /** 各技能释放一次额外消耗的法杖耐久（按技能强度与收益区分）。 */
    private static final Map<Skill, Integer> COST = Map.ofEntries(
            Map.entry(Skill.CREEPER, 20),
            Map.entry(Skill.SKELETON, 5),
            Map.entry(Skill.ENDERMAN, 5),
            Map.entry(Skill.BLAZE, 10),
            Map.entry(Skill.IRON_GOLEM, 5),
            Map.entry(Skill.SHULKER, 5),
            Map.entry(Skill.EVOKER, 20),
            Map.entry(Skill.GHAST, 10),
            Map.entry(Skill.BREEZE, 5),
            Map.entry(Skill.WITHER, 20),
            Map.entry(Skill.DRAGON, 30),
            Map.entry(Skill.SNOW_GOLEM, 1),
            Map.entry(Skill.LLAMA, 5),
            Map.entry(Skill.WITCH, 10),
            Map.entry(Skill.DROWNED, 5),
            Map.entry(Skill.GUARDIAN, 10),
            Map.entry(Skill.WARDEN, 20));

    private static final ResourceLocation FLIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "transformation_flight");
    private static final String COOLDOWN_PREFIX = "skill_cd_";
    /** 上一次对齐冷却条时的形态技能键，用于在切换形态时刷新法杖上的冷却显示。 */
    private static final String COOLDOWN_FORM = "skill_cd_form";
    /** 变身状态下法杖的耐久流失间隔（刻）：每 20 刻（1 秒）流失 1 点。 */
    private static final int WAND_DRAIN_INTERVAL = 20;
    private static final int WAND_DRAIN_AMOUNT = 1;
    /** 法杖耐久保留阈值：剩余 ≤ 该值时停止消耗该法杖，改用其他耐久更高的法杖。 */
    private static final int WAND_RESERVE = 60;
    /** 攀爬上报表的到期游戏刻；客户端每刻续期，断线后 4 刻自动失效。 */
    private static final String CLIMB_UNTIL = "transformation_climb_until";

    private SkillSystem() {}

    /** 该生物形态对应的技能；没有技能时返回 null。 */
    public static Skill skillFor(EntityType<?> type) {
        return type == null ? null : SKILLS.get(type);
    }

    /** 该变身形态是否拥有可释放的技能（原版技能或 yuzusoft 专属技能）。 */
    public static boolean hasAnySkill(EntityType<?> type) {
        return skillFor(type) != null || YuzusoftSkills.skillFor(type) != null;
    }

    /** 客户端判定用：该变身 id 是否拥有可释放的技能。 */
    public static boolean hasSkill(String id) {
        return id != null && !id.isEmpty() && hasAnySkill(EntityType.byString(id).orElse(null));
    }

    /** 服务端处理一次"释放技能"请求（含手持法杖与冷却校验）。 */
    public static void cast(ServerPlayer player) {
        if (!holdsWand(player)) return;
        EntityType<?> type = TransformationSystem.currentType(player);
        Skill skill = skillFor(type);
        if (skill == null) {
            // 非原版技能：交给 yuzusoft 专属技能表（自带冷却校验）
            YuzusoftSkills.cast(player, type);
            return;
        }
        long now = player.level().getGameTime();
        String key = COOLDOWN_PREFIX + skill.name();
        if (now < player.getPersistentData().getLong(key)) return;
        player.getPersistentData().putLong(key, now + COOLDOWN.get(skill));
        player.getCooldowns().addCooldown(ModItems.TRANSFORMATION_WAND.get(), COOLDOWN.get(skill));
        consumeWand(player, COST.get(skill));
        switch (skill) {
            case CREEPER -> creeper(player);
            case SKELETON -> skeleton(player);
            case ENDERMAN -> enderman(player);
            case BLAZE -> blaze(player);
            case IRON_GOLEM -> golemSmash(player);
            case SHULKER -> shulker(player);
            case EVOKER -> evoker(player);
            case GHAST -> ghast(player);
            case BREEZE -> breeze(player);
            case WITHER -> wither(player);
            case DRAGON -> dragon(player);
            case SNOW_GOLEM -> snowGolem(player);
            case LLAMA -> llama(player);
            case WITCH -> witch(player);
            case DROWNED -> drowned(player);
            case GUARDIAN -> guardian(player);
            case WARDEN -> warden(player);
        }
    }

    /** 技能冷却键前缀，供 yuzusoft 技能表复用同一套冷却存储。 */
    static String cooldownKey(String skillName) {
        return COOLDOWN_PREFIX + skillName;
    }

    /** 当前形态技能的冷却键（没有技能时为空串）。 */
    static String currentCooldownKey(EntityType<?> type) {
        Skill skill = skillFor(type);
        if (skill != null) return COOLDOWN_PREFIX + skill.name();
        YuzusoftSkills.YuzusoftSkill yuzusoft = YuzusoftSkills.skillFor(type);
        return yuzusoft == null ? "" : cooldownKey("yz_" + yuzusoft.name());
    }

    /**
     * 把法杖上的可视化冷却条对齐到"当前形态技能"的剩余冷却。
     * <p>
     * 与投掷末影珍珠一致，冷却条复用原版 {@link net.minecraft.world.item.ItemCooldowns}
     * （物品栏里那层白色扫描遮罩）；冷却本身仍以持久化数据为准，这里只在
     * "切换形态 / 重新登录"时校正显示，平时由原版客户端自行倒数，无需每刻同步。
     */
    static void refreshCooldownDisplay(ServerPlayer player) {
        String key = currentCooldownKey(TransformationSystem.currentType(player));
        long remaining = key.isEmpty() ? 0L
                : player.getPersistentData().getLong(key) - player.level().getGameTime();
        Item wand = ModItems.TRANSFORMATION_WAND.get();
        if (remaining > 0L) {
            player.getCooldowns().addCooldown(wand, (int) Math.min(remaining, Integer.MAX_VALUE));
        } else {
            player.getCooldowns().removeCooldown(wand);
        }
    }

    /** 形态发生变化时刷新法杖冷却条，避免残留上一个形态技能的条。 */
    private static void syncCooldownDisplay(ServerPlayer player, EntityType<?> type) {
        String key = currentCooldownKey(type);
        if (key.equals(player.getPersistentData().getString(COOLDOWN_FORM))) return;
        player.getPersistentData().putString(COOLDOWN_FORM, key);
        refreshCooldownDisplay(player);
    }

    /**
     * 每刻维护形态带来的移动能力（均无需左键）：
     * <ul>
     *     <li>天生会飞的形态（蝙蝠/蜜蜂/鹦鹉/烈焰人等）持续保有飞行能力，脱离形态时收回；</li>
     *     <li>蜘蛛类形态贴着墙壁移动时向上攀爬。</li>
     * </ul>
     */
    public static void tick(ServerPlayer player) {
        EntityType<?> type = TransformationSystem.currentType(player);
        syncCooldownDisplay(player, type);
        // 变身状态下每 1 秒消耗背包内法杖的耐久（无需手持）
        if (type != null && player.tickCount % WAND_DRAIN_INTERVAL == 0) {
            consumeWand(player, WAND_DRAIN_AMOUNT);
        }
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        boolean shouldFly = TransformationTraits.canFly(type);
        if (shouldFly != hasFlight(attribute)) setFlight(player, shouldFly);
        if (TransformationTraits.canClimb(type) && isClimbing(player)) tickClimb(player);
    }

    /** 接收客户端上报的攀爬状态，刷新攀爬窗口。 */
    public static void setClimbing(ServerPlayer player, boolean climbing) {
        long until = climbing ? player.level().getGameTime() + 4L : 0L;
        player.getPersistentData().putLong(CLIMB_UNTIL, until);
    }

    private static boolean isClimbing(Player player) {
        return player.level().getGameTime() < player.getPersistentData().getLong(CLIMB_UNTIL);
    }

    /**
     * 爬墙：攀爬期间每刻续期一段短暂的隐藏悬浮。
     * <p>
     * 玩家移动由客户端权威，服务端直接改速度会被 moved-wrongly 校验拉回；
     * 悬浮（Levitation）在客户端与服务端同时生效，位置一致，可稳定实现攀爬。
     * 效果被隐藏（不显示图标与粒子），仅在贴墙移动期间续期。
     * <p>
     * 悬浮同时让服务端的"长时间悬空"踢出判定放行（该判定会检查 LEVITATION）。
     */
    private static void tickClimb(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 4, 1, true, false, false));
    }

    private static boolean holdsWand(Player player) {
        return wandSlot(player) != null;
    }

    /** 手持法杖所在的槽位（主手优先、其次副手）；未手持时返回 null。 */
    private static EquipmentSlot wandSlot(Player player) {
        if (player.getMainHandItem().is(ModItems.TRANSFORMATION_WAND.get())) return EquipmentSlot.MAINHAND;
        if (player.getOffhandItem().is(ModItems.TRANSFORMATION_WAND.get())) return EquipmentSlot.OFFHAND;
        return null;
    }

    /** 消耗背包内一根法杖的耐久（无需手持）：
     * <ul>
     *     <li>剩余耐久大于 {@link #WAND_RESERVE} 的法杖中，优先消耗剩余耐久最少的那根；</li>
     *     <li>剩余耐久已 ≤ {@link #WAND_RESERVE} 的法杖停止消耗，改用其他更高耐久的；</li>
     *     <li>所有法杖都 ≤ {@link #WAND_RESERVE} 时，继续消耗其中剩余耐久最少的一根。</li>
     * </ul>
     * 被消耗的法杖耐久耗尽时立即解除变身。创造模式由原版 {@code hasInfiniteMaterials} 跳过。 */
    static void consumeWand(ServerPlayer player, int amount) {
        ItemStack wand = selectWand(player);
        if (wand.isEmpty()) return;
        wand.hurtAndBreak(amount, player, EquipmentSlot.MAINHAND);
        if (wand.isEmpty()) TransformationSystem.revert(player);
    }

    /** 选出本轮要消耗耐久的那根法杖；背包内没有法杖时返回 {@link ItemStack#EMPTY}。 */
    private static ItemStack selectWand(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack best = ItemStack.EMPTY;      // 剩余耐久 > WAND_RESERVE 中剩余最少者
        ItemStack fallback = ItemStack.EMPTY;  // 全部法杖中剩余最少者
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(ModItems.TRANSFORMATION_WAND.get())) continue;
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            if (fallback.isEmpty() || remaining < fallback.getMaxDamage() - fallback.getDamageValue()) {
                fallback = stack;
            }
            if (remaining > WAND_RESERVE
                    && (best.isEmpty() || remaining < best.getMaxDamage() - best.getDamageValue())) {
                best = stack;
            }
        }
        return best.isEmpty() ? fallback : best;
    }

    /** 苦力怕：以玩家为爆心的爆炸。爆炸源为玩家自身，原版会排除爆心实体，故玩家不掉血也不被击退。 */
    private static void creeper(ServerPlayer player) {
        player.serverLevel().explode(player, player.getX(), player.getY(), player.getZ(),
                3.0F, Level.ExplosionInteraction.MOB);
    }

    private static boolean hasFlight(AttributeInstance attribute) {
        return attribute != null && attribute.hasModifier(FLIGHT_ID);
    }

    private static void setFlight(ServerPlayer player, boolean enabled) {
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute == null) return;
        if (enabled) {
            attribute.addOrUpdateTransientModifier(
                    new AttributeModifier(FLIGHT_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
            player.getAbilities().flying = true;
        } else {
            attribute.removeModifier(FLIGHT_ID);
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    /** 骷髅（及流浪者/沼骸/幻术师/掠夺者）：射出箭矢（无限）。专武弓由 {@link TransformationCombat} 统一发放。 */
    private static void skeleton(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);

        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
        arrow.setPos(eye.x + look.x, eye.y - 0.1, eye.z + look.z);
        // 与原版骷髅一致：出膛速度 1.6、基础伤害 2.0，命中约 4 点（伤害 = 速度 × baseDamage）
        arrow.shoot(look.x, look.y, look.z, 1.6F, 1.0F);
        arrow.setBaseDamage(2.0);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 末影人：沿视线方向随机瞬移，落点需为安全可站立处。 */
    private static void enderman(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();
        Vec3 look = player.getViewVector(1.0F);

        for (int attempt = 0; attempt < 24; attempt++) {
            double dx = look.x * 0.7 + (random.nextDouble() - 0.5) * 1.4;
            double dz = look.z * 0.7 + (random.nextDouble() - 0.5) * 1.4;
            double dy = (random.nextDouble() - 0.5) * 0.8;
            Vec3 offset = new Vec3(dx, dy, dz);
            if (offset.lengthSqr() < 1.0E-4) continue;
            offset = offset.normalize().scale(6.0 + random.nextDouble() * 24.0);

            BlockPos probe = BlockPos.containing(
                    player.getX() + offset.x, player.getY() + offset.y, player.getZ() + offset.z);
            BlockPos ground = null;
            for (int i = 0; i < 8; i++) {
                BlockPos candidate = probe.below(i);
                if (!level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()) {
                    ground = candidate;
                    break;
                }
            }
            if (ground == null) continue;

            double standY = ground.getY() + 1.0;
            AABB box = player.getDimensions(Pose.STANDING)
                    .makeBoundingBox(ground.getX() + 0.5, standY, ground.getZ() + 0.5);
            if (!level.noCollision(player, box)) continue;

            Vec3 from = player.position();
            player.teleportTo(level, ground.getX() + 0.5, standY, ground.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
            player.resetFallDistance();
            level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 32, 0.3, 0.8, 0.3, 0.1);
            level.playSound(null, from.x, from.y, from.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
    }

    /** 烈焰人：一次释放连射 3 发小火球（同时具备常驻飞行能力）。 */
    private static void blaze(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        RandomSource random = player.getRandom();

        for (int i = 0; i < 3; i++) {
            Vec3 dir = new Vec3(
                    look.x + (random.nextDouble() - 0.5) * 0.24,
                    look.y + (random.nextDouble() - 0.5) * 0.24,
                    look.z + (random.nextDouble() - 0.5) * 0.24).normalize();
            SmallFireball fireball = new SmallFireball(level, player, dir);
            // 在视线前方生成，避免火球与自身碰撞立即消散
            fireball.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);
            level.addFreshEntity(fireball);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 铁傀儡：对周围 4 格内生物造成伤害并强力击飞。 */
    private static void golemSmash(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(4.0, 2.5, 4.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive());
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), 8.0F);
            Vec3 push = target.position().subtract(player.position());
            push = new Vec3(push.x, 0.0, push.z);
            if (push.lengthSqr() < 1.0E-4) {
                Vec3 look = player.getLookAngle();
                push = new Vec3(look.x, 0.0, look.z);
            }
            if (push.lengthSqr() < 1.0E-4) continue;
            Vec3 knock = push.normalize().scale(2.2).add(0.0, 0.55, 0.0);
            target.setDeltaMovement(target.getDeltaMovement().add(knock));
            target.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(),
                24, 1.5, 0.5, 1.5, 0.2);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * 潜影贝：向 24 格内最近的生物发射一枚潜影弹（命中造成 4 点伤害并附加 10 秒飘浮）。
     * 与潜影贝一致，没有目标时不开火。
     */
    private static void shulker(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(24.0D),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator());
        LivingEntity target = null;
        double nearest = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = candidate.distanceToSqr(player);
            if (distance < nearest) {
                nearest = distance;
                target = candidate;
            }
        }
        if (target == null) return;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        ShulkerBullet bullet = new ShulkerBullet(level, player, target, Direction.Axis.Y);
        // 生成在视线前方，避免潜影弹落在自身碰撞箱内立即命中自己
        bullet.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);
        level.addFreshEntity(bullet);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 2.0F, 1.0F);
    }

    /** 唤魔者：沿视线方向打出一排尖牙（16 枚、间距 1.25 格，越远冒出越晚）。 */
    private static void evoker(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        // 与原版尖牙一致的角度约定：方向 = (cos f, sin f)，yRot 传弧度 f
        float f = (float) (Math.toRadians(player.getYRot()) + Math.PI / 2.0);
        double dirX = Math.cos(f);
        double dirZ = Math.sin(f);
        for (int i = 0; i < 16; i++) {
            double dist = 1.25D * (i + 1);
            double x = player.getX() + dirX * dist;
            double z = player.getZ() + dirZ * dist;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(x, player.getY(), z));
            level.addFreshEntity(new EvokerFangs(level, x, ground.getY(), z, f, i, player));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 恶魂：发射一枚大火球。爆炸源换成玩家自身，故玩家不会炸到自己。 */
    private static void ghast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        GhastFireball fireball = new GhastFireball(level, player, look, 1);
        fireball.setPos(eye.x + look.x * 1.5, eye.y - 0.2, eye.z + look.z * 1.5);
        level.addFreshEntity(fireball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 2.0F, 1.0F);
    }

    /** 旋风人：发射一枚风弹（命中产生风爆，击退周围生物）。 */
    private static void breeze(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WindCharge charge = new WindCharge(player, level,
                player.getX(), player.getEyeY(), player.getZ());
        charge.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(charge);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 凋灵：发射一枚凋灵之首（命中时附加凋零）。 */
    private static void wither(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        WitherSkull skull = new WitherSkull(level, player, look);
        skull.setPos(eye.x + look.x * 1.5, eye.y - 0.2, eye.z + look.z * 1.5);
        level.addFreshEntity(skull);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 末影龙：发射一枚龙息弹（落点留下持续伤害的龙息云）。 */
    private static void dragon(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        DragonFireball fireball = new DragonFireball(level, player, look);
        fireball.setPos(eye.x + look.x * 1.5, eye.y - 0.2, eye.z + look.z * 1.5);
        level.addFreshEntity(fireball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 雪傀儡：抛出一枚雪球。 */
    private static void snowGolem(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Snowball snowball = new Snowball(level, player);
        snowball.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);
        snowball.shoot(look.x, look.y, look.z, 1.5F, 1.0F);
        level.addFreshEntity(snowball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 羊驼 / 行商羊驼：吐出一口口水。 */
    private static void llama(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        LlamaSpit spit = new LlamaSpit(EntityType.LLAMA_SPIT, level);
        spit.setOwner(player);
        spit.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);
        spit.shoot(look.x, look.y, look.z, 1.5F, 1.0F);
        level.addFreshEntity(spit);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 女巫：向 16 格内最近的生物投掷一瓶瞬间伤害喷溅药水；无目标则沿视线抛出。 */
    private static void witch(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        ThrownPotion potion = new ThrownPotion(level, player);
        potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HARMING));
        potion.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);

        LivingEntity target = nearest(player, 16.0D);
        if (target != null) {
            Vec3 diff = target.getEyePosition().subtract(potion.position());
            potion.shoot(diff.x, diff.y + diff.horizontalDistance() * 0.2D, diff.z, 0.75F, 8.0F);
        } else {
            potion.shoot(look.x, look.y, look.z, 0.75F, 8.0F);
        }
        level.addFreshEntity(potion);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.0F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }

    /** 溺尸：投掷一把三叉戟。 */
    private static void drowned(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        ThrownTrident trident = new ThrownTrident(level, player, new ItemStack(Items.TRIDENT));
        // 技能凭空产出的三叉戟不可回收，避免无限刷取；不可拾取的投掷物会在 60 秒后自动消失
        trident.pickup = AbstractArrow.Pickup.DISALLOWED;
        trident.setPos(eye.x + look.x * 1.2, eye.y - 0.2, eye.z + look.z * 1.2);
        trident.shoot(look.x, look.y, look.z, 1.6F, 1.0F);
        level.addFreshEntity(trident);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 守卫者 / 远古守卫者：向视线方向发射一道激光（远古守卫者伤害更高）。 */
    private static void guardian(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double range = 24.0D;
        Vec3 to = from.add(look.scale(range));

        LivingEntity target = rayTarget(player, from, to);
        for (int i = 1; i <= (int) range; i++) {
            Vec3 point = from.add(look.scale(i));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (target != null) {
            float damage = level.getDifficulty() == Difficulty.HARD ? 3.0F : 1.0F;
            if (TransformationSystem.currentType(player) == EntityType.ELDER_GUARDIAN) damage += 2.0F;
            target.hurt(player.damageSources().indirectMagic(player, player), damage);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 监守者：释放音爆，命中造成 10 点伤害并按击退抗性强力击退。 */
    private static void warden(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F).normalize();
        double range = 15.0D;
        Vec3 to = from.add(dir.scale(range));

        LivingEntity target = rayTarget(player, from, to);
        for (int i = 1; i <= (int) range + 7; i++) {
            Vec3 point = from.add(dir.scale(i));
            level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);
        if (target != null && target.hurt(player.damageSources().sonicBoom(player), 10.0F)) {
            double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double horizontal = 2.5D * (1.0D - resistance);
            double vertical = 0.5D * (1.0D - resistance);
            target.push(dir.x * horizontal, dir.y * vertical, dir.z * horizontal);
        }
    }

    /** 沿视线取首个命中的生物；被方块挡住则视为未命中。 */
    static LivingEntity rayTarget(ServerPlayer player, Vec3 from, Vec3 to) {
        ServerLevel level = player.serverLevel();
        BlockHitResult blockHit = level.clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double limit = blockHit.getType() == HitResult.Type.MISS
                ? from.distanceToSqr(to) : from.distanceToSqr(blockHit.getLocation());
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, from, to,
                new AABB(from, to).inflate(1.0D),
                entity -> entity instanceof LivingEntity && entity != player
                        && entity.isAlive() && !entity.isSpectator());
        if (entityHit == null) return null;
        if (from.distanceToSqr(entityHit.getLocation()) > limit) return null;
        return entityHit.getEntity() instanceof LivingEntity living ? living : null;
    }

    /** 范围内最近的生物（排除玩家自身）。 */
    static LivingEntity nearest(ServerPlayer player, double range) {
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {
            double distance = candidate.distanceToSqr(player);
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /**
     * 恶魂火球：与原版大火球一致，但把爆炸源换成玩家自身。
     * <p>
     * 原版以火球为爆心实体，爆炸不会排除持有者，贴脸开火会炸伤自己；
     * 改用玩家作爆心后，原版爆炸天然排除爆心实体，玩家对自己的火球免疫。
     */
    private static final class GhastFireball extends LargeFireball {
        private final int power;

        GhastFireball(Level level, LivingEntity owner, Vec3 direction, int power) {
            super(level, owner, direction, power);
            this.power = power;
        }

        @Override
        protected void onHit(HitResult hit) {
            if (hit.getType() == HitResult.Type.ENTITY) {
                this.onHitEntity((EntityHitResult) hit);
            } else if (hit.getType() == HitResult.Type.BLOCK) {
                this.onHitBlock((BlockHitResult) hit);
            }
            if (!this.level().isClientSide) {
                this.level().explode(this.getOwner(), this.getX(), this.getY(), this.getZ(),
                        (float) this.power, false, Level.ExplosionInteraction.MOB);
                this.discard();
            }
        }
    }
}