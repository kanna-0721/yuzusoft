package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.FrostGuardianEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;

public class FrostRangedAttackGoal extends RangedAttackGoal {

    private final FrostGuardianEntity mob;

    public FrostRangedAttackGoal(FrostGuardianEntity mob) {
        super(mob, 1.0D, FrostGuardianEntity.RANGED_COOLDOWN_TICKS, (float) FrostGuardianEntity.FOLLOW_RANGE);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null) return false;
        return this.mob.distanceToSqr(target) > FrostGuardianEntity.RANGED_SWITCH_DIST_SQ;
    }
}

