package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Monster;

import java.util.EnumSet;

public class GuitarRangedAttackGoal extends Goal {
    private final Monster mob;
    private final RangedAttackMob rangedMob;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRadius;
    private int seeTime;
    private int attackDelay;
    private int nextAttackTick;

    public GuitarRangedAttackGoal(Monster mob, double speedModifier, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.rangedMob = (RangedAttackMob) mob;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadius = attackRadius;
        this.attackDelay = 0;
        this.nextAttackTick = 0;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.seeTime = 0;
        this.attackDelay = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.seeTime = 0;
        this.attackDelay = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distSq = this.mob.distanceToSqr(target);
        boolean canSee = this.mob.hasLineOfSight(target);
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }

        double radiusSq = (double) (this.attackRadius * this.attackRadius);
        if (distSq > radiusSq) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.attackDelay = 0;
        } else {
            this.mob.getNavigation().stop();
            this.attackDelay++;
            if (this.attackDelay == 1) {
                this.mob.playSound(ModSounds.GUITAR_MONSTER_CHARGE.get(), 3.0F, 1.0F);
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.attackDelay >= 10 && this.seeTime >= 5) {
                if (this.mob.tickCount >= this.nextAttackTick) {
                    this.rangedMob.performRangedAttack(target, 1.0F);
                    this.nextAttackTick = this.mob.tickCount + this.attackInterval;
                    this.attackDelay = 0;
                }
            }
        }
    }
}