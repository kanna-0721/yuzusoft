package cn.autoforged.yuzusoft.transformation;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.network.payload.SyncTransformationPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

/**
 * 变身系统的事件接入（击败解锁、拟态锁敌、免伤特性、状态效果免疫、持续特性、躲避目标等）。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖），由静态注册改为 {@link EventBusSubscriber}。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public final class TransformationEvents {
    private static final String UNLOCKED = "transformation_unlocked";
    private static final String LAND_AIR = "transformation_land_air";
    /** 原版 {@link HurtByTargetGoal} 的群体警报开关（{@code setAlertOthers} 会置真）。 */
    private static final Field ALERT_SAME_TYPE = findField(HurtByTargetGoal.class, "alertSameType");

    private TransformationEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity killed = event.getEntity();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType());
        ListTag list = player.getPersistentData().getList(UNLOCKED, Tag.TAG_STRING);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(id.toString())) { found = true; break; }
        }
        if (!found) {
            list.add(StringTag.valueOf(id.toString()));
            player.getPersistentData().put(UNLOCKED, list);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.yuzusoft.unlocked", killed.getType().getDescription()), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().merge(event.getOriginal().getPersistentData().copy());
        TransformationSystem.revert(event.getEntity());
    }

    /** 玩家重新登录时，把自身的变身形态同步给自己（其他玩家由 StartTracking 负责）。 */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TransformationSystem.sync(player);
            // 原版物品冷却不随存档保存，重新登录后按持久化的技能冷却把法杖冷却条补回来
            SkillSystem.refreshCooldownDisplay(player);
        }
    }

    /** 其他玩家开始跟踪某玩家（进入视野/切换维度）时，把被跟踪者的变身形态同步过去。 */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof Player tracked) || !(event.getEntity() instanceof ServerPlayer observer)) return;
        String id = TransformationSystem.current(tracked);
        PacketDistributor.sendToPlayer(observer, new SyncTransformationPayload(tracked.getUUID(), id.isEmpty() ? "minecraft:player" : id));
    }

    /**
     * 拟态锁敌：怪物锁定的目标是"已变身的玩家"时，按目标生物的原始锁敌表判定。
     * 若该怪物本来就不会攻击这种生物，则取消本次锁定；被该玩家打过（反击）时照常锁定。
     */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
        String id = TransformationSystem.current(player);
        if (id.isEmpty()) return; // 未变身，保持原版行为
        if (player.isCreative() || player.isSpectator()) {
            event.setCanceled(true);
            return;
        }
        if (mob.getLastHurtByMob() == player) return; // 主动攻击后照常反击
        // 群体仇恨（HurtByTargetGoal.alertOthers）会给"未被打的同伴"设置攻击者，这些同伴的
        // lastHurtByMob 为空：仅当附近确有"被该玩家打过的同族"时才放行，避免波及无关生物。
        if (isGroupRetaliation(mob, player)) return;
        EntityType<?> disguised = EntityType.byString(id).orElse(null);
        if (disguised == null) return;
        if (!TransformationTargeting.wouldTarget(mob, disguised, mob.level())) event.setCanceled(true);
    }

    /**
     * 该生物是否正被"同族被玩家攻击"引发的群体仇恨波及。
     * <p>
     * 仅对**本身就有群体仇恨机制**的生物放行，避免给无关生物凭空加上群体仇恨：
     * <ul>
     *   <li>原版 {@link HurtByTargetGoal} 且开启了 {@code setAlertOthers}（僵尸猪灵、猪灵、僵尸、灾厄村民等）：同族支援；</li>
     *   <li>yuzusoft 的 {@link GroupSupportTargetGoal}（0721 族群）：族员支援。</li>
     * </ul>
     * 判定条件是"范围内存在被此玩家打过、且正在追击此玩家的同伴"，随同伴脱战自然失效。
     */
    private static boolean isGroupRetaliation(Mob mob, Player player) {
        if (hasVanillaAlerter(mob)) {
            return hasHurtAlly(mob, player, e -> e.getClass() == mob.getClass());
        }
        if (hasGroupSupport(mob)) {
            return hasHurtAlly(mob, player, Group0721Helper::isGroup0721);
        }
        return false;
    }

    /** 生物是否带有开启了群体警报的原版 {@link HurtByTargetGoal}。 */
    private static boolean hasVanillaAlerter(Mob mob) {
        if (ALERT_SAME_TYPE == null) return false;
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            if (!(goal instanceof HurtByTargetGoal)) continue;
            try {
                if (ALERT_SAME_TYPE.getBoolean(goal)) return true;
            } catch (Throwable ignored) {
                // 反射失败时按"无群体仇恨"处理，保持保守
            }
        }
        return false;
    }

    /** 生物是否使用 0721 族群的支援 goal。 */
    private static boolean hasGroupSupport(Mob mob) {
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof GroupSupportTargetGoal) return true;
        }
        return false;
    }

    /** 范围内是否存在"属于同一群体、被此玩家打过并正在追击此玩家"的同伴。 */
    private static boolean hasHurtAlly(Mob mob, Player player, Predicate<Mob> sameGroup) {
        double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (followRange <= 0.0D) followRange = 16.0D;
        AABB box = AABB.unitCubeFromLowerCorner(mob.position()).inflate(followRange, 10.0D, followRange);
        List<Mob> allies = mob.level().getEntitiesOfClass(Mob.class, box,
                e -> e != mob && sameGroup.test(e)
                        && e.getLastHurtByMob() == player && e.getTarget() == player);
        return !allies.isEmpty();
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            return null;
        }
    }

    /** 变身形态的原版免伤特性：火焰/岩浆、摔落、冰冻、溺水、甜浆果丛等。 */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;
        EntityType<?> type = TransformationSystem.currentType(player);
        if (type != null && TransformationTraits.blocksDamage(type, event.getSource(), player)) event.setCanceled(true);
    }

    /**
     * 变身形态的攻击附加效果：命中后按形态施加状态。
     * 近战（洞穴蜘蛛中毒、尸壳饥饿、凋灵骷髅凋零）与远程（流浪者迟缓、沼骸中毒）分表判定，
     * 用 {@code IS_PROJECTILE} 区分弹射物伤害，避免远程生物靠贴脸近战也能挂 debuff。
     */
    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        EntityType<?> type = TransformationSystem.currentType(player);
        if (type == null) return;
        boolean ranged = event.getSource().is(DamageTypeTags.IS_PROJECTILE);
        TransformationCombat.applyAttackEffect(type, player, event.getEntity(), ranged);
    }

    /** 形态的状态效果免疫：亡灵免疫中毒与生命恢复、蜘蛛类免疫中毒、凋零骷髅与凋灵免疫凋零、凋灵与末影龙拒绝一切效果。 */
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;
        EntityType<?> type = TransformationSystem.currentType(player);
        if (type == null) return;
        if (TransformationTraits.resistsEffect(type, event.getEffectInstance().getEffect())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /** 每刻持续特性：沾水掉血、火焰免疫顺带灭火、水生形态离水窒息。 */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SkillSystem.tick(player);
        EntityType<?> type = TransformationSystem.currentType(player);
        if (type == null) return;
        if (TransformationTraits.isWaterSensitive(type) && player.isInWaterRainOrBubble()) {
            player.hurt(player.damageSources().drown(), 1.0F);
        }
        if (TransformationTraits.isFireImmune(type) && player.isOnFire()) player.clearFire();
        if (TransformationTraits.suffocatesOnLand(type)) tickLandAir(player);
    }

    /** 水生形态离水窒息：复刻原版 WaterAnimal.handleAirSupply（约 16 秒后开始掉 2 点）。 */
    private static void tickLandAir(ServerPlayer player) {
        if (player.isInWaterOrBubble()) {
            player.getPersistentData().putInt(LAND_AIR, 300);
            return;
        }
        int air = player.getPersistentData().getInt(LAND_AIR) - 1;
        if (air <= -20) {
            air = 0;
            player.hurt(player.damageSources().drown(), 2.0F);
        }
        player.getPersistentData().putInt(LAND_AIR, air);
    }

    /** 法杖不可丢弃：拦截 GUI 拖出等丢弃路径（官方文档：取消后物品已被移出背包，需自行回填）。 */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!event.getEntity().getItem().is(ModItems.TRANSFORMATION_WAND.get())) return;
        event.setCanceled(true);
        Player player = event.getPlayer();
        if (!player.level().isClientSide()) {
            player.getInventory().add(event.getEntity().getItem().copy());
        }
    }

    /** 生物加入世界时，追加"躲避对应变身形态"的目标（苦力怕躲猫/豹猫等）与"拟态过滤"目标条件。 */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        TransformationReactions.patchTargeting(mob);
        if (mob instanceof PathfinderMob pathfinder) TransformationReactions.patchAvoid(pathfinder);
    }
}