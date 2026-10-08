package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.head.HeadAbilityTable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * 融合生物的近战 Goal：与 {@link MeleeAttackGoal} 行为一致，区别有三：
 * <ul>
 *   <li>不走 {@code Mob.doHurtTarget}，而是用装配时算好的伤害直接结算
 *       （{@code doHurtTarget} 会读 {@code ATTACK_DAMAGE}，而动物类身体的属性表里没有这一项，
 *       1.21.1 又没法在运行时补注册，会直接抛异常）；</li>
 *   <li>照抄头部原生物 {@code doHurtTarget} 的命中附加：附加效果 + 吸血；</li>
 *   <li>可选的「近距离阈值」：大于 0 时只在目标进入该距离内才启用，
 *       用来复现「惠 6 格内切近战 / 绫濑 2.55 格内切近战」这类混合近远战。</li>
 * </ul>
 */
public class FusionMeleeAttackGoal extends MeleeAttackGoal {
    private final float damage;
    private final HeadAbilityTable.OnHit onHit;
    /** 大于 0 表示只在目标进入该距离内才启用（格）；0 = 不限制。 */
    private final float hybridRange;

    public FusionMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen,
                                 float damage, HeadAbilityTable.OnHit onHit, float hybridRange) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.damage = damage;
        this.onHit = onHit;
        this.hybridRange = hybridRange;
    }

    @Override
    public boolean canUse() {
        return withinHybridRange() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return withinHybridRange() && super.canContinueToUse();
    }

    private boolean withinHybridRange() {
        if (this.hybridRange <= 0.0F) {
            return true;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive()
                && this.mob.distanceToSqr(target) <= (double) this.hybridRange * this.hybridRange;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (!this.canPerformAttack(target)) {
            return;
        }
        this.resetAttackCooldown();
        this.mob.swing(InteractionHand.MAIN_HAND);
        applyHit(this.mob, target, this.damage, this.onHit);
    }

    /**
     * 结算一次近战：直接扣血（绕开 {@code ATTACK_DAMAGE} 属性读取）+
     * 命中附加效果 + 吸血（命中成功才结算，与原生物一致）。
     */
    public static void applyHit(Mob attacker, LivingEntity target, float damage, HeadAbilityTable.OnHit onHit) {
        boolean hurt = target.hurt(attacker.damageSources().mobAttack(attacker), damage);
        if (!hurt || onHit == null || onHit.isEmpty()) {
            return;
        }
        if (onHit.effect() != null) {
            target.addEffect(new MobEffectInstance(onHit.effect(), onHit.duration(), onHit.amplifier(),
                    false, false, true));
        }
        if (onHit.lifesteal() > 0.0F) {
            attacker.heal(damage * onHit.lifesteal());
        }
    }
}