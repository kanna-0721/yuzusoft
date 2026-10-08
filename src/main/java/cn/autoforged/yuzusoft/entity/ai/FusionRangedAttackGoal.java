package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.head.HeadAbilityTable;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 融合生物的远程攻击 Goal：使用 {@link HeadAbilityTable} 中头部生物原本的攻击方式。
 * <p>
 * 不依赖 {@code RangedAttackMob} 接口（融合生物是既有实体类型，无法实现新接口），
 * 行为与原生物一致：超出射程持续追击，进入射程后停下并以 {@code navigation.stop()}
 * 中断旧路径，原地保持面向目标攻击。
 * <p>
 * 目标被 {@code TargetGoal} 丢弃（超出 {@code FOLLOW_RANGE}）后不立刻站桩：
 * 仍会继续追向目标最后一次可见的位置，抵达后原地环顾搜索约 5 秒再放弃。
 * <p>
 * 两种攻击方式：投射物（其余远程头部），以及春的声波（{@link HeadAbilityTable.Ability#sonicBoom()}，
 * 无投射物，直接射线伤害 + 按击退抗性推开）。
 */
public class FusionRangedAttackGoal extends Goal {
    /** 进入射程后需要持续「看得见」多久才允许开火。 */
    private static final int MIN_LOCK_TICKS = 5;
    /** 进入射程后的蓄力刻数。 */
    private static final int CHARGE_TICKS = 10;
    /** 目标被丢弃后继续追向「最后已知位置」的记忆窗口（约 5 秒）。 */
    private static final int MEMORY_TICKS = 100;
    /** 与最后已知位置的距离平方小于该值即视为已抵达（2 格）。 */
    private static final double ARRIVE_DIST_SQR = 4.0D;

    private final Mob mob;
    private final HeadAbilityTable.Ability ability;
    private int seeTime;
    private int chargeTime;
    private int nextAttackTick;
    /** 目标最后一次可见时所处的位置；目标被丢弃后据此继续追击。 */
    private Vec3 lastKnownPos;
    /** 记录 lastKnownPos 的刻。 */
    private int lastKnownTick;
    /** 抵达最后已知位置后原地搜索已进行的刻数。 */
    private int searchTicks;

    public FusionRangedAttackGoal(Mob mob, HeadAbilityTable.Ability ability) {
        this.mob = mob;
        this.ability = ability;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            return target.isAlive();
        }
        // 目标已被 TargetGoal 丢弃（超出索敌范围）：只要记忆未过期就继续追向最后已知位置
        return this.lastKnownPos != null
                && this.mob.tickCount - this.lastKnownTick <= MEMORY_TICKS;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.seeTime = 0;
        this.chargeTime = 0;
        this.searchTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.seeTime = 0;
        this.chargeTime = 0;
        this.searchTicks = 0;
        this.lastKnownPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        // 与原版 RangedAttackGoal / MeleeAttackGoal 一致：每刻都更新追击与开火
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            this.searchTicks = 0;
            this.trackAndAttack(target);
            return;
        }
        this.searchLastKnownPos();
    }

    /** 目标仍在视野内：持续追击 / 开火，并刷新「最后已知位置」。 */
    private void trackAndAttack(LivingEntity target) {
        boolean canSee = this.mob.hasLineOfSight(target);
        if (canSee) {
            this.seeTime++;
            this.lastKnownPos = target.position();
            this.lastKnownTick = this.mob.tickCount;
        } else {
            this.seeTime = 0;
        }

        double rangeSq = (double) (this.ability.range() * this.ability.range());
        if (this.mob.distanceToSqr(target) > rangeSq || !canSee) {
            // 太远或看不见：持续推进
            this.mob.getNavigation().moveTo(target, 1.0D);
            this.chargeTime = 0;
            return;
        }

        // 进入射程：停下脚步，保持面向目标
        this.mob.getNavigation().stop();
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.chargeTime++;
        if (this.chargeTime >= CHARGE_TICKS
                && this.seeTime >= MIN_LOCK_TICKS
                && this.mob.tickCount >= this.nextAttackTick) {
            this.shoot(target);
            this.nextAttackTick = this.mob.tickCount + this.ability.cooldownTicks();
            this.chargeTime = 0;
        }
    }

    /**
     * 目标已被丢弃（超出索敌范围 / 暂时失去视线）：继续跑向最后已知位置，
     * 抵达后停下原地环顾搜索；记忆窗口耗尽则由 {@link #canUse()} 判定放弃。
     */
    private void searchLastKnownPos() {
        this.seeTime = 0;
        this.chargeTime = 0;
        Vec3 pos = this.lastKnownPos;
        if (pos == null) {
            return;
        }
        if (this.mob.position().distanceToSqr(pos) > ARRIVE_DIST_SQR) {
            // 还没到：继续追向最后已知位置
            this.searchTicks = 0;
            this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0D);
            return;
        }
        // 已抵达：停下脚步原地搜索（随机环顾四周），直到记忆过期
        this.mob.getNavigation().stop();
        this.searchTicks++;
        if (this.searchTicks % 20 == 0) {
            this.mob.getLookControl().setLookAt(
                    this.mob.getX() + (this.mob.getRandom().nextDouble() - 0.5D) * 8.0D,
                    this.mob.getY() + (this.mob.getRandom().nextDouble() - 0.5D) * 4.0D,
                    this.mob.getZ() + (this.mob.getRandom().nextDouble() - 0.5D) * 8.0D);
        }
    }

    private void shoot(LivingEntity target) {
        if (this.ability.sonicBoom()) {
            performSonicBoom(target);
            return;
        }
        HeadAbilityTable.ProjectileFactory factory = this.ability.projectile();
        if (factory == null) {
            return;
        }
        Projectile projectile = factory.create(this.mob.level(), this.mob);
        if (projectile == null) {
            return;
        }
        double dx = target.getX() - this.mob.getX();
        double dy = target.getEyeY() - projectile.getY();
        double dz = target.getZ() - this.mob.getZ();
        projectile.shoot(dx, dy, dz, this.ability.velocity(), this.ability.inaccuracy());
        this.mob.level().addFreshEntity(projectile);
    }

    /** 春的声波：沿视线方向铺 SONIC_BOOM 粒子，按难度结算伤害并按击退抗性推开。 */
    private void performSonicBoom(LivingEntity target) {
        Level level = this.mob.level();
        double startY = this.mob.getY() + this.mob.getEyeHeight() * 0.6D;
        Vec3 dir = new Vec3(target.getX() - this.mob.getX(),
                target.getEyeY() - startY,
                target.getZ() - this.mob.getZ()).normalize();
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 1; i <= 20; i++) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                        this.mob.getX() + dir.x * i, startY + dir.y * i, this.mob.getZ() + dir.z * i,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        float damage = switch (level.getDifficulty()) {
            case EASY -> 3.0F;
            case HARD -> 6.0F;
            default -> 4.0F;
        };
        target.hurt(this.mob.damageSources().sonicBoom(this.mob), damage);
        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double horizontal = 2.5D * (1.0D - resistance);
        double vertical = 0.5D * (1.0D - resistance);
        target.push(dir.x * horizontal, dir.y * vertical, dir.z * horizontal);
        this.mob.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                ModSounds.GUITAR_WEAPON_RANGED.get(), this.mob.getSoundSource(), 1.0F, 1.0F);
    }
}