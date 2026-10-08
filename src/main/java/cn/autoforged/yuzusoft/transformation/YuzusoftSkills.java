package cn.autoforged.yuzusoft.transformation;

import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.DetonatorProjectileEntity;
import cn.autoforged.yuzusoft.entity.custom.FlashbangProjectile;
import cn.autoforged.yuzusoft.entity.custom.FrostBoltV2Projectile;
import cn.autoforged.yuzusoft.entity.custom.FrostProjectileEntity;
import cn.autoforged.yuzusoft.entity.custom.GuardianEntity;
import cn.autoforged.yuzusoft.entity.custom.LevitationBulletEntity;
import cn.autoforged.yuzusoft.entity.custom.ShadowDartEntity;
import cn.autoforged.yuzusoft.entity.custom.WaterBallEntity;
import cn.autoforged.yuzusoft.entity.custom.WaterOrbEntity;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.util.NoteUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * yuzusoft 生物的专属「变身法杖」技能。
 * <p>
 * 玩家击败某只 yuzusoft 生物解锁其形态后，手持变身法杖左键即可释放该生物的招牌能力：
 * 复刻它自身的近战武器、专属弹射物或光环效果（参数取自对应实体/弹射物的源码）。
 * 与原版技能共用同一套冷却存储（{@link SkillSystem#cooldownKey}），互不冲突。
 */
public final class YuzusoftSkills {

    /** yuzusoft 生物各自的招牌技能。 */
    public enum YuzusoftSkill {
        /** 芳乃（tomotake_yoshino）：召唤护卫。 */
        YOSHINO,
        /** 茉子（hitachi_mako）：暗影飞镖。 */
        MAKO,
        /** 宁宁（ayachi_nene）：引爆器投掷。 */
        NENE,
        /** 巡（inaba_meguru）：冰霜弹。 */
        MEGURU,
        /** 麻由（shikibe_mayu）：寒冰矢。 */
        MAYU,
        /** 绫濑（mitsukasa_ayase）：飘浮弹。 */
        AYASE,
        /** 七海（arihara_nanami）：治愈光环。 */
        NANAMI,
        /** 栞那（akizuki_kanna）：水球。 */
        KANNA,
        /** 绘奈（harumi_ena）：夺命声波。 */
        ENA,
        /** 里子（futamihara_ririko）：催眠曲。 */
        RIRIKO,
        /** 杏珠（nabari_anju）：闪光弹。 */
        ANJU,
        /** 纺（shiiba_tsumugi）：重锤震荡。 */
        TSUMUGI,
        /** 美羽（yarai_miu）：吸血斩。 */
        MIU,
        /** 孝宪（mikado_takanori，猫）：猛扑。 */
        TAKANORI,
        /** 铃音（shioyama_suzune）：自爆。 */
        SUZUNE,
        /** 来海（kohibari_kurumi）：矿物探测。 */
        KURUMI,
        /** 邪恶七海（evil_nanami）：邪恶光环。 */
        EVIL_NANAMI,
        /** 叶月（nijouin_hazuki）：水弹。 */
        HAZUKI,
        /** 月望（shimagoe_tsukumi）：琴声领域。 */
        TSUKUMI
    }

    /** 各技能冷却（游戏刻）。 */
    private static final Map<YuzusoftSkill, Integer> COOLDOWN = Map.ofEntries(
            Map.entry(YuzusoftSkill.YOSHINO, 100),
            Map.entry(YuzusoftSkill.MAKO, 20),
            Map.entry(YuzusoftSkill.NENE, 20),
            Map.entry(YuzusoftSkill.MEGURU, 20),
            Map.entry(YuzusoftSkill.MAYU, 20),
            Map.entry(YuzusoftSkill.AYASE, 20),
            Map.entry(YuzusoftSkill.NANAMI, 200),
            Map.entry(YuzusoftSkill.KANNA, 20),
            Map.entry(YuzusoftSkill.ENA, 20),
            Map.entry(YuzusoftSkill.RIRIKO, 20),
            Map.entry(YuzusoftSkill.ANJU, 20),
            Map.entry(YuzusoftSkill.TSUMUGI, 20),
            Map.entry(YuzusoftSkill.MIU, 20),
            Map.entry(YuzusoftSkill.TAKANORI, 60),
            Map.entry(YuzusoftSkill.SUZUNE, 300),
            Map.entry(YuzusoftSkill.KURUMI, 200),
            Map.entry(YuzusoftSkill.EVIL_NANAMI, 200),
            Map.entry(YuzusoftSkill.HAZUKI, 20),
            Map.entry(YuzusoftSkill.TSUKUMI, 40));

    /** 各技能释放一次额外消耗的法杖耐久（按技能强度与收益区分）。 */
    private static final Map<YuzusoftSkill, Integer> COST = Map.ofEntries(
            Map.entry(YuzusoftSkill.YOSHINO, 100),
            Map.entry(YuzusoftSkill.MAKO, 5),
            Map.entry(YuzusoftSkill.NENE, 20),
            Map.entry(YuzusoftSkill.MEGURU, 5),
            Map.entry(YuzusoftSkill.MAYU, 5),
            Map.entry(YuzusoftSkill.AYASE, 5),
            Map.entry(YuzusoftSkill.NANAMI, 0),
            Map.entry(YuzusoftSkill.KANNA, 10),
            Map.entry(YuzusoftSkill.ENA, 20),
            Map.entry(YuzusoftSkill.RIRIKO, 0),
            Map.entry(YuzusoftSkill.ANJU, 5),
            Map.entry(YuzusoftSkill.TSUMUGI, 10),
            Map.entry(YuzusoftSkill.MIU, 10),
            Map.entry(YuzusoftSkill.TAKANORI, 0),
            Map.entry(YuzusoftSkill.SUZUNE, 50),
            Map.entry(YuzusoftSkill.KURUMI, 0),
            Map.entry(YuzusoftSkill.EVIL_NANAMI, 0),
            Map.entry(YuzusoftSkill.HAZUKI, 5),
            Map.entry(YuzusoftSkill.TSUKUMI, 10));

    /** 探测半径（格，按坐标轴取切比雪夫距离），与来海本体一致。 */
    private static final int SCAN_RADIUS = 6;

    /** 月望琴声领域：内圈/外圈半径（切比雪夫，格）与各自伤害，与月望本体一致。 */
    private static final int INNER_RADIUS = 6;
    private static final int OUTER_RADIUS = 12;
    private static final float INNER_DAMAGE = 6.0F;
    private static final float OUTER_DAMAGE = 4.0F;

    /** 月望琴声领域附加的眩晕时长：1 秒，与月望本体、钢琴方块一致。 */
    private static final int STUN_TICKS = 20;

    private YuzusoftSkills() {}

    /** 该生物形态对应的 yuzusoft 技能；没有则返回 null。 */
    public static YuzusoftSkill skillFor(EntityType<?> type) {
        return type == null ? null : table().get(type);
    }

    /**
     * 懒绑定：实体类型 DeferredHolder 在注册表填充前 {@code get()} 会抛异常，
     * 故每次调用时重建映射（18 条，开销可忽略），只收录已绑定的项。
     */
    private static Map<EntityType<?>, YuzusoftSkill> table() {
        Map<EntityType<?>, YuzusoftSkill> map = new HashMap<>();
        bind(map, ModEntities.GUARDIAN_TRADER, YuzusoftSkill.YOSHINO);
        bind(map, ModEntities.SHADOW_ASSASSIN, YuzusoftSkill.MAKO);
        bind(map, ModEntities.DETONATOR_THROWING_MONSTER, YuzusoftSkill.NENE);
        bind(map, ModEntities.FROST_GUARDIAN, YuzusoftSkill.MEGURU);
        bind(map, ModEntities.FROST_GUARDIAN_V2, YuzusoftSkill.MAYU);
        bind(map, ModEntities.FLOATING_SENTINEL, YuzusoftSkill.AYASE);
        bind(map, ModEntities.VILLAGE_GUARDIAN, YuzusoftSkill.NANAMI);
        bind(map, ModEntities.SPRINKLER_CREEP, YuzusoftSkill.KANNA);
        bind(map, ModEntities.GUITAR_MONSTER, YuzusoftSkill.ENA);
        bind(map, ModEntities.SLEEPY_SPIRIT, YuzusoftSkill.RIRIKO);
        bind(map, ModEntities.FLASHBANG_MONSTER, YuzusoftSkill.ANJU);
        bind(map, ModEntities.HAMMER_WIELDER, YuzusoftSkill.TSUMUGI);
        bind(map, ModEntities.BLOOD_SUCKER_ZOMBIE, YuzusoftSkill.MIU);
        bind(map, ModEntities.CAT_0721, YuzusoftSkill.TAKANORI);
        bind(map, ModEntities.SUZUNE, YuzusoftSkill.SUZUNE);
        bind(map, ModEntities.DIAMOND_GUARDIAN, YuzusoftSkill.KURUMI);
        bind(map, ModEntities.EVIL_NANAMI, YuzusoftSkill.EVIL_NANAMI);
        bind(map, ModEntities.WATER_SPIRIT, YuzusoftSkill.HAZUKI);
        bind(map, ModEntities.SHIMAGOE_TSUKUMI, YuzusoftSkill.TSUKUMI);
        return map;
    }

    private static void bind(Map<EntityType<?>, YuzusoftSkill> map,
                             DeferredHolder<EntityType<?>, ?> holder, YuzusoftSkill skill) {
        if (holder != null && holder.isBound()) map.put(holder.get(), skill);
    }

    /** 服务端处理一次 yuzusoft 技能释放（自带冷却校验；手持法杖由 {@link SkillSystem} 校验）。 */
    public static void cast(ServerPlayer player, EntityType<?> type) {
        YuzusoftSkill skill = skillFor(type);
        if (skill == null) return;
        long now = player.level().getGameTime();
        String key = SkillSystem.cooldownKey("yz_" + skill.name());
        if (now < player.getPersistentData().getLong(key)) return;
        player.getPersistentData().putLong(key, now + COOLDOWN.get(skill));
        player.getCooldowns().addCooldown(ModItems.TRANSFORMATION_WAND.get(), COOLDOWN.get(skill));
        SkillSystem.consumeWand(player, COST.get(skill));
        switch (skill) {
            case YOSHINO -> yoshino(player);
            case MAKO -> mako(player);
            case NENE -> nene(player);
            case MEGURU -> meguru(player);
            case MAYU -> mayu(player);
            case AYASE -> ayase(player);
            case NANAMI -> nanami(player);
            case KANNA -> kanna(player);
            case ENA -> ena(player);
            case RIRIKO -> ririko(player);
            case ANJU -> anju(player);
            case TSUMUGI -> tsumugi(player);
            case MIU -> miu(player);
            case TAKANORI -> takanori(player);
            case SUZUNE -> suzune(player);
            case KURUMI -> kurumi(player);
            case EVIL_NANAMI -> evilNanami(player);
            case HAZUKI -> hazuki(player);
            case TSUKUMI -> tsukumi(player);
        }
    }

    // ===== 各技能实现 =====

    /** 芳乃：召唤一名护卫（丛雨形态）随行，生命值满、不会消失。 */
    private static void yoshino(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        GuardianEntity guardian = ModEntities.GUARDIAN.get().create(level);
        if (guardian == null) return;
        guardian.moveTo(player.getX() + 1.0D, player.getY(), player.getZ() + 1.0D,
                player.getYRot(), 0.0F);
        guardian.setPersistenceRequired();
        level.addFreshEntity(guardian);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0D, player.getZ(),
                24, 0.6D, 0.8D, 0.6D, 0.05D);
        play(player, SoundEvents.EVOKER_CAST_SPELL, 1.0F);
    }

    /** 茉子：暗影飞镖——射出命中后造成伤害并附加 0721 效果的飞镖。 */
    private static void mako(ServerPlayer player) {
        launch(player, new ShadowDartEntity(player.serverLevel(), player), 2.0F, 1.0F);
        play(player, SoundEvents.ARROW_SHOOT, 1.0F);
    }

    /** 宁宁：引爆器投掷——投出命中即爆炸（半径 2，投掷者免伤）并附加 0721 的引爆器。 */
    private static void nene(ServerPlayer player) {
        launch(player, new DetonatorProjectileEntity(player.serverLevel(), player), 1.2F, 2.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 0.8F);
    }

    /** 巡：冰霜弹——射出命中造成 8 点伤害并减速的冰霜弹。 */
    private static void meguru(ServerPlayer player) {
        launch(player, new FrostProjectileEntity(player.serverLevel(), player), 1.4F, 1.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 1.0F);
    }

    /** 麻由：寒冰矢——射出命中造成伤害、减速、附加 0721 并击退的寒冰矢。 */
    private static void mayu(ServerPlayer player) {
        launch(player, new FrostBoltV2Projectile(player.serverLevel(), player), 1.2F, 0.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 0.9F);
    }

    /** 绫濑：飘浮弹——射出命中造成伤害并附加飘浮的子弹。 */
    private static void ayase(ServerPlayer player) {
        launch(player, new LevitationBulletEntity(player.serverLevel(), player), 1.0F, 0.2F);
        play(player, SoundEvents.SHULKER_SHOOT, 1.0F);
    }

    /** 七海：治愈光环——8 格内所有生物获得生命恢复，4 格内提升一级。 */
    private static void nanami(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(8.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            int amplifier = entity.distanceTo(player) <= 4.0D ? 1 : 0;
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, amplifier));
        }
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0D, player.getZ(),
                12, 0.8D, 0.6D, 0.8D, 0.02D);
        play(player, SoundEvents.PLAYER_LEVELUP, 1.2F);
    }

    /** 栞那：水球——射出命中造成伤害并附加凋零与 0721、落地生成水源的水球。 */
    private static void kanna(ServerPlayer player) {
        launch(player, new WaterBallEntity(player.serverLevel(), player), 1.5F, 0.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 0.7F);
    }

    /** 绘奈：夺命声波——沿视线发射音爆，命中造成伤害并按击退抗性击退。 */
    private static void ena(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F).normalize();
        double range = 15.0D;
        Vec3 to = from.add(dir.scale(range));

        LivingEntity target = SkillSystem.rayTarget(player, from, to);
        for (int i = 1; i <= (int) range; i++) {
            Vec3 point = from.add(dir.scale(i));
            level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        play(player, SoundEvents.WARDEN_SONIC_BOOM, 1.0F);
        if (target != null) {
            float damage = switch (level.getDifficulty()) {
                case EASY -> 3.0F;
                case HARD -> 6.0F;
                default -> 4.0F;
            };
            if (target.hurt(player.damageSources().sonicBoom(player), damage)) {
                double resistance = target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
                double horizontal = 2.5D * (1.0D - resistance);
                double vertical = 0.5D * (1.0D - resistance);
                target.push(dir.x * horizontal, dir.y * vertical, dir.z * horizontal);
            }
        }
    }

    /** 里子：催眠曲——6 格内的生物陷入迟缓与虚弱。 */
    private static void ririko(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(6.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive())) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.0D, player.getZ(),
                16, 0.8D, 0.6D, 0.8D, 0.02D);
        play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F);
    }

    /** 杏珠：闪光弹——射出命中造成伤害并施加失明的闪光弹。 */
    private static void anju(ServerPlayer player) {
        launch(player, new FlashbangProjectile(player.serverLevel(), player), 1.5F, 0.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 1.1F);
    }

    /** 纺：重锤震荡——对身前 3.5 格内的生物造成 8 点伤害并施加眩晕。 */
    private static void tsumugi(ServerPlayer player) {
        for (LivingEntity target : frontTargets(player, 3.5D, 0.25D)) {
            if (target.hurt(player.damageSources().playerAttack(player), 8.0F)) {
                target.addEffect(new MobEffectInstance(ModEffects.STUN, 40, 0));
            }
        }
        player.serverLevel().sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY() + 1.0D, player.getZ(), 16, 1.0D, 0.4D, 1.0D, 0.2D);
        play(player, SoundEvents.PLAYER_ATTACK_STRONG, 0.9F);
    }

    /** 美羽：吸血斩——对身前 3.0 格内的生物造成 6 点伤害，吸血一半并附加 0721。 */
    private static void miu(ServerPlayer player) {
        float damage = 6.0F;
        for (LivingEntity target : frontTargets(player, 3.0D, 0.35D)) {
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                player.heal(damage * 0.5F);
                target.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721, 100, 0));
            }
        }
        play(player, SoundEvents.PLAYER_ATTACK_SWEEP, 0.9F);
    }

    /** 孝宪（猫）：猛扑——向视线方向跃出，落地周围 3 格内生物受击并击退。 */
    private static void takanori(ServerPlayer player) {
        Vec3 look = player.getViewVector(1.0F);
        player.setDeltaMovement(look.x * 1.2D, 0.45D, look.z * 1.2D);
        player.hurtMarked = true;
        for (LivingEntity target : frontTargets(player, 3.0D, 0.1D)) {
            target.hurt(player.damageSources().playerAttack(player), 5.0F);
            target.push(look.x * 0.8D, 0.3D, look.z * 0.8D);
            target.hurtMarked = true;
        }
        play(player, SoundEvents.CAT_HISS, 1.0F);
    }

    /** 铃音：自爆——以玩家为爆心的 8 格爆炸（爆炸源为玩家自身，故玩家免疫）。 */
    private static void suzune(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.explode(player, player.getX(), player.getY() + player.getEyeHeight() / 2.0D, player.getZ(),
                8.0F, false, Level.ExplosionInteraction.MOB);
    }

    /** 来海：矿物探测——扫描 6 格内的钻石/深层钻石矿石并播报方位。 */
    private static void kurumi(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    cursor.setWithOffset(center, dx, dy, dz);
                    if (!level.isLoaded(cursor)) continue;
                    var state = level.getBlockState(cursor);
                    if (!state.is(Blocks.DIAMOND_ORE) && !state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) continue;
                    double distSq = cursor.distSqr(center);
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = cursor.immutable();
                    }
                }
            }
        }
        if (best == null) {
            player.displayClientMessage(Component.translatable(
                    "message.yuzusoft.kohibari_kurumi.no_diamond"), false);
        } else {
            String directionKey = directionKeyOf(Vec3.atCenterOf(best).subtract(player.position()));
            player.displayClientMessage(Component.translatable(
                    "message.yuzusoft.kohibari_kurumi.diamond_nearby", Component.translatable(directionKey)), false);
        }
        play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F);
    }

    /** 邪恶七海：邪恶光环——为自身、附近玩家与已驯服宠物提供高级生命恢复。 */
    private static void evilNanami(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(8.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            boolean ally = entity == player || entity instanceof Player
                    || (entity instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isTame());
            if (ally) entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        }
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0D, player.getZ(),
                12, 0.8D, 0.6D, 0.8D, 0.02D);
        play(player, SoundEvents.PLAYER_LEVELUP, 1.0F);
    }

    /** 叶月：水弹——射出无重力直线水弹，命中造成伤害并击退。 */
    private static void hazuki(ServerPlayer player) {
        launch(player, new WaterOrbEntity(player.serverLevel(), player), 1.5F, 0.0F);
        play(player, SoundEvents.SNOWBALL_THROW, 0.9F);
    }

    /** 月望：琴声领域——以自身为心，6 格内（切比雪夫）造成 6 点伤害，7–12 格造成 4 点。 */
    private static void tsukumi(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        AABB area = player.getBoundingBox().inflate(OUTER_RADIUS + 1.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !e.isSpectator())) {
            int distance = chebyshev(center, target.blockPosition());
            if (distance <= INNER_RADIUS) {
                target.hurt(player.damageSources().playerAttack(player), INNER_DAMAGE);
            } else if (distance <= OUTER_RADIUS) {
                target.hurt(player.damageSources().playerAttack(player), OUTER_DAMAGE);
            } else {
                continue;
            }
            // 琴声领域附带 1 秒眩晕
            target.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_TICKS, 0));
        }
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.0D, player.getZ(),
                24, 1.2D, 0.8D, 1.2D, 0.02D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 2.0F,
                NoteUtil.pitchFromId(player.getRandom().nextInt(NoteUtil.KEY_COUNT)));
    }

    // ===== 通用工具 =====

    /** 在视线前方生成一枚弹射物并沿视线方向射出，避免落在自身碰撞箱内。 */
    private static void launch(ServerPlayer player, Projectile projectile, float speed, float inaccuracy) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        projectile.setPos(eye.x + look.x * 1.3D, eye.y - 0.15D, eye.z + look.z * 1.3D);
        projectile.shoot(look.x, look.y, look.z, speed, inaccuracy);
        player.serverLevel().addFreshEntity(projectile);
    }

    /** 身前范围内的生物（以视线方向点积过滤，minDot 越大越要求正对）。 */
    private static List<LivingEntity> frontTargets(ServerPlayer player, double range, double minDot) {
        Vec3 look = player.getViewVector(1.0F);
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range),
                e -> e != player && e.isAlive() && !e.isSpectator()
                        && e.position().subtract(player.position()).normalize().dot(look) >= minDot);
    }

    /** 把相对位置向量换算成方位翻译 key（取绝对值最大的坐标轴），与来海本体一致。 */
    private static String directionKeyOf(Vec3 diff) {
        double ax = Math.abs(diff.x);
        double ay = Math.abs(diff.y);
        double az = Math.abs(diff.z);
        if (ax >= ay && ax >= az) {
            return diff.x >= 0 ? "direction.yuzusoft.east" : "direction.yuzusoft.west";
        } else if (az >= ax && az >= ay) {
            return diff.z >= 0 ? "direction.yuzusoft.south" : "direction.yuzusoft.north";
        } else {
            return diff.y >= 0 ? "direction.yuzusoft.up" : "direction.yuzusoft.down";
        }
    }

    private static void play(ServerPlayer player, SoundEvent sound, float pitch) {
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.0F, pitch);
    }

    /** 方块整数距离（切比雪夫），与月望本体一致。 */
    private static int chebyshev(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }
}