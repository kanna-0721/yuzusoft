package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.FrostGuardianEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * 近战攻击目标：仅当目标位于 8 格以内时使用（偏近战），远处交给远程目标。
 */
public class FrostMeleeAttackGoal extends MeleeAttackGoal {

    public FrostMeleeAttackGoal(FrostGuardianEntity mob) {
        super(mob, 1.1D, true);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null) return false;
        // 目标在 8 格以内才用近战
        return this.mob.distanceToSqr(target) <= FrostGuardianEntity.RANGED_SWITCH_DIST_SQ;
    }
}
