package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.FrostGuardianEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class HealOwnerGoal extends Goal {
    private final FrostGuardianEntity mob;
    public HealOwnerGoal(FrostGuardianEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTame()) return false;
        LivingEntity owner = this.mob.getOwner();
        if (owner == null || !owner.isAlive()) return false;
        if (owner.getHealth() >= owner.getMaxHealth()) return false;
        if (this.mob.tickCount - this.mob.lastHealTime < FrostGuardianEntity.HEAL_COOLDOWN_TICKS) return false;
        return this.mob.distanceToSqr(owner) <= FrostGuardianEntity.HEAL_RANGE_SQ;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.mob.lastHealTime = this.mob.tickCount;
        this.mob.fireHealProjectile();
    }
}

