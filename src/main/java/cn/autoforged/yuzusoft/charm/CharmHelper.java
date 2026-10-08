package cn.autoforged.yuzusoft.charm;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 魅惑统一入口与判定工具。
 *
 * 设计：
 * - "魅惑生效" = 生物身上存在 {@link CharmRegistry#CHARM_EFFECT} 效果实例；
 *   永久契约用 INFINITE_DURATION(-1) 实例表达，同时把主人写进实体 NBT（CharmState）。
 * - 所有来源（瓶子右键 / 箭命中 / 喷溅 / 滞留云 / 喝药）都走 LivingEntity#addEffect(instance, source)，
 *   由 {@link CharmEvents#onEffectAdded} 统一解析来源玩家并落契约数据。
 */
public final class CharmHelper {
    private CharmHelper() {}

    /** 玩家被攻击/反击目标的记忆窗口（刻）。 */
    public static final int TARGET_MEMORY_TICKS = 200;

    /** 粘性锁定的最大追击距离（格）。已锁定的目标在此距离内持续攻击，不因记忆窗口过期脱锁。 */
    public static final float STICKY_RANGE = 32.0F;
    private static final float STICKY_RANGE_SQ = STICKY_RANGE * STICKY_RANGE;

    // ------------------------------------------------------------------
    //  施加与解除
    // ------------------------------------------------------------------

    /**
     * 施加魅惑。
     *
     * @param durationTicks 时长；传 {@link MobEffectInstance#INFINITE_DURATION} 表示永久契约
     * @param source        来源实体（玩家，或箭/喷溅药水/滞留云等，可为 null）
     */
    public static void applyCharm(Mob mob, @Nullable LivingEntity source, int durationTicks) {
        mob.addEffect(new MobEffectInstance(CharmRegistry.CHARM_EFFECT, durationTicks, 0, false, true), source);
    }

    /** 解除：移除效果与契约数据（效果移除事件与兜底检查会清理状态）。 */
    public static void releaseCharm(Mob mob) {
        mob.removeEffect(CharmRegistry.CHARM_EFFECT);
        clearState(mob);
    }

    /** 魅惑生物统一移速（格/刻）。 */
    public static final double CHARM_SPEED = 0.32;

    /** 状态消失后的统一清理（时限到期、奶桶、/effect clear、玩家死亡都会走到这里）。 */
    public static void clearState(Mob mob) {
        CharmState state = getState(mob);
        if (state == null) {
            return;
        }
        restoreCharmSpeed(mob);
        mob.removeData(CharmRegistry.CHARM_STATE.get());
        mob.setTarget(null);
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF,
                    mob.getRandomX(0.6), mob.getY(mob.getBbHeight() * 0.7), mob.getRandomZ(0.6),
                    6, 0.15, 0.15, 0.15, 0.02);
            mob.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8F, 0.7F);
        }
    }

    // ------------------------------------------------------------------
    //  统一移速
    // ------------------------------------------------------------------

    /**
     * 魅惑生效时把 MOVEMENT_SPEED 基础值压到 {@link #CHARM_SPEED}，
     * 并返回"需要恢复的原值"（首次记录；重复施加沿用旧值）。
     * 无该属性的生物返回 -1（不改动）。
     */
    public static double applyCharmSpeed(Mob mob, @Nullable CharmState old) {
        AttributeInstance attr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return -1.0;
        }
        double original = (old != null && old.originalSpeed() >= 0)
                ? old.originalSpeed()
                : attr.getBaseValue();
        if (attr.getBaseValue() != CHARM_SPEED) {
            attr.setBaseValue(CHARM_SPEED);
        }
        return original;
    }

    /** 魅惑解除时恢复契约前的基础移速。 */
    public static void restoreCharmSpeed(Mob mob) {
        CharmState state = getState(mob);
        if (state == null || state.originalSpeed() < 0) {
            return;
        }
        AttributeInstance attr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(state.originalSpeed());
        }
    }

    /** 契约存在但效果已不在（奶桶 /effect clear 到期等）时释放契约；并保护永久契约不被时限版合并降级。 */
    public static void checkRelease(Mob mob) {
        CharmState state = getState(mob);
        if (state == null) {
            return;
        }
        if (!hasCharmEffect(mob)) {
            clearState(mob);
            return;
        }
        if (state.permanent()) {
            MobEffectInstance instance = mob.getEffect(CharmRegistry.CHARM_EFFECT);
            if (instance != null && !instance.isInfiniteDuration()) {
                // 永久契约被时限版药水/箭合并降级：重新压回无限时长
                mob.addEffect(new MobEffectInstance(CharmRegistry.CHARM_EFFECT,
                        MobEffectInstance.INFINITE_DURATION, 0, false, true), null);
            }
        }
    }

    // ------------------------------------------------------------------
    //  查询
    // ------------------------------------------------------------------

    public static boolean hasCharmEffect(Mob mob) {
        return mob.hasEffect(CharmRegistry.CHARM_EFFECT);
    }

    public static boolean isCharmActive(Mob mob) {
        return hasCharmEffect(mob) || getState(mob) != null;
    }

    @Nullable
    public static CharmState getState(Mob mob) {
        return mob.getData(CharmRegistry.CHARM_STATE.get());
    }

    /** 是否玩家驯服的生物：原版已驯服动物，或带可解析主人的 OwnableEntity。 */
    public static boolean isPlayerTamed(LivingEntity target) {
        if (target instanceof TamableAnimal tameable && tameable.isTame()) {
            return true;
        }
        return target instanceof OwnableEntity ownable && ownable.getOwner() != null;
    }

    /**
     * 是否"玩家阵营"：玩家本人、玩家驯服的宠物、或其他魅惑生物。
     * 魅惑的远程/范围/近战伤害一律不得落在这些目标上（互不攻击）。
     */
    public static boolean isPlayerSide(LivingEntity target) {
        if (target instanceof Player) {
            return true;
        }
        if (isPlayerTamed(target)) {
            return true;
        }
        return target instanceof Mob mob && isCharmActive(mob);
    }

    /**
     * 免疫魅惑的目标：玩家本人，以及 BOSS 类生物
     * （末影龙、凋灵、循声守卫）。
     */
    public static boolean isCharmImmune(LivingEntity target) {
        if (target instanceof Player) {
            return true;
        }
        if (target instanceof EnderDragon || target instanceof WitherBoss || target instanceof Warden) {
            return true;
        }
        return false;
    }

    /** 当前契约主人（服务端解析；离线玩家未加载时返回 null）。 */
    @Nullable
    public static Player resolveOwner(Mob mob) {
        CharmState state = getState(mob);
        if (state == null || state.owner() == null) {
            return null;
        }
        if (mob.level() instanceof ServerLevel level
                && level.getEntity(state.owner()) instanceof ServerPlayer player
                && player.isAlive()) {
            return player;
        }
        return null;
    }

    /** 从效果来源实体解析主人玩家（瓶子=玩家、箭/喷溅=射手、滞留云=云主人）。 */
    @Nullable
    public static Player resolveOwnerFromSource(@Nullable LivingEntity source) {
        if (source instanceof Player player) {
            return player;
        }
        return null;
    }

    @Nullable
    public static Player resolveOwnerFromEntitySource(@Nullable net.minecraft.world.entity.Entity source) {
        if (source instanceof Player player) {
            return player;
        }
        if (source instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            return player;
        }
        if (source instanceof AreaEffectCloud cloud && cloud.getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }

    // ------------------------------------------------------------------
    //  可魅惑判定："任意有攻击能力的生物"
    // ------------------------------------------------------------------

    public static boolean hasOffensiveCapability(Mob mob) {
        // Enemy 是敌对标记接口：僵尸/苦力怕（extends Monster）与史莱姆/岩浆怪（implements Enemy）
        // 都命中；Monster 在本映射是抽象类，直接实现 Enemy 的生物会被漏掉。
        if (mob instanceof Enemy) {
            return true;
        }
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            if (goal instanceof MeleeAttackGoal || goal instanceof RangedAttackGoal) {
                return true;
            }
        }
        return false;
    }

    /** 瓶子右键时是否接受该目标（调用方保证目标不是玩家）。 */
    public static boolean canCharmTarget(Mob mob, Player usingPlayer) {
        if (mob.isRemoved()) {
            return false;
        }
        if (isCharmImmune(mob)) {
            return false; // 玩家与 BOSS 类生物免疫，不吞瓶子
        }
        if (!hasOffensiveCapability(mob)) {
            return false;
        }
        CharmState state = getState(mob);
        if (state != null && state.permanent()
                && state.owner() != null && state.owner().equals(usingPlayer.getUUID())
                && hasCharmEffect(mob)) {
            // 已经与该玩家缔结永久契约，不再吞瓶子
            return false;
        }
        return true;
    }

    /**
     * 魅惑期间合法的攻击目标：只允许"攻击过主人的、主人攻击过的、攻击过自己的"，
     * 且不能是玩家。跟随与反击 AI 以及 setTarget 拦截共用这一条判定。
     * 已锁定的目标享受"粘性锁定"：射程内持续追击，不因记忆窗口过期脱锁，
     * 保证远程/范围生物攻击连续（不间歇性停火）。
     */
    public static boolean isAllowedCharmTarget(Mob mob, @Nullable LivingEntity target) {
        if (target == null || target instanceof Player || target == mob) {
            return false;
        }
        // 魅惑生物之间互不攻击（无论契约主人是谁，全体结为盟友）
        if (target instanceof Mob otherMarked && isCharmActive(otherMarked)) {
            return false;
        }
        // 魅惑生物不攻击玩家驯服的生物
        if (isPlayerTamed(target)) {
            return false;
        }
        // 粘性锁定：已锁定的目标在射程内持续追击（初始锁定仍需下面的事件触发）
        if (mob.getTarget() == target && target.isAlive()
                && mob.distanceToSqr(target) <= STICKY_RANGE_SQ) {
            return true;
        }
        Player owner = resolveOwner(mob);
        if (owner != null && withinMemory(owner, owner.getLastHurtByMob(), target)) {
            return true; // 正在攻击主人的生物
        }
        if (owner != null && withinMemory(owner, owner.getLastHurtMob(), owner.getLastHurtMobTimestamp(), target)) {
            return true; // 主人正在攻击的生物
        }
        return withinMemory(mob, mob.getLastHurtByMob(), target); // 自卫
    }

    private static boolean withinMemory(LivingEntity witness, @Nullable LivingEntity candidate, LivingEntity target) {
        return witness.tickCount - witness.getLastHurtByMobTimestamp() < TARGET_MEMORY_TICKS
                && candidate == target;
    }

    private static boolean withinMemory(LivingEntity witness, @Nullable LivingEntity candidate, int timestamp, LivingEntity target) {
        return witness.tickCount - timestamp < TARGET_MEMORY_TICKS && candidate == target;
    }

    public static void celebrate(Mob mob) {
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART,
                    mob.getRandomX(0.7), mob.getY(mob.getBbHeight() * 0.9), mob.getRandomZ(0.7),
                    4, 0.2, 0.2, 0.2, 0.0);
            level.playSound(null, mob, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.0F);
        }
    }

    /** 主人 UUID 是否匹配（用于玩家死亡批量解除）。 */
    public static boolean ownedBy(CharmState state, UUID owner) {
        return state != null && owner.equals(state.owner());
    }
}
