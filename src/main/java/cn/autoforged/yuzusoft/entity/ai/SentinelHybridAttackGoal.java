package cn.autoforged.yuzusoft.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import cn.autoforged.yuzusoft.entity.custom.FloatingSentinelEntity;

public class SentinelHybridAttackGoal extends Goal {
    private static final int MELEE_COOLDOWN = 20;
    private static final int RANGED_COOLDOWN = 40;
    private static final double MELEE_TRIGGER_SQR = 6.5;      // 约 2.55 格，贴近后切近战
    private static final double MELEE_HIT_SQR = 7.5;          // 实际出手范围

    private final FloatingSentinelEntity mob;
    private final double speedModifier;
    private int attackCooldown = 0;
    private int rangedCooldown = 0;

    public SentinelHybridAttackGoal(FloatingSentinelEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double followRange = mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        return mob.distanceToSqr(target) <= followRange * followRange;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        attackCooldown = 0;
        rangedCooldown = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distSqr = mob.distanceToSqr(target);

        // 移动：近距离停止，远距离靠近
        if (distSqr <= MELEE_TRIGGER_SQR) {
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(target, speedModifier);
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (rangedCooldown > 0) {
            rangedCooldown--;
        }

        if (distSqr <= MELEE_HIT_SQR) {
            // 近战
            mob.setAggressive(true);
            if (attackCooldown <= 0 && mob.getSensing().hasLineOfSight(target)) {
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(target);
                attackCooldown = MELEE_COOLDOWN;
            }
        } else if (distSqr <= FloatingSentinelEntity.RANGED_RANGE * FloatingSentinelEntity.RANGED_RANGE) {
            // 远程
            mob.setAggressive(false);
            if (rangedCooldown <= 0 && mob.getSensing().hasLineOfSight(target)) {
                mob.performRangedAttack(target, 1.0F);
                rangedCooldown = RANGED_COOLDOWN;
            }
        }
    }
}

