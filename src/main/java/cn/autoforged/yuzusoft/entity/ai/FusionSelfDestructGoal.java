package cn.autoforged.yuzusoft.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * 苦力怕式自爆：融合生物接上自爆型头部（苦力怕、铃音）后，靠到目标身边蓄力，30 刻后爆炸。
 * <p>
 * 行为与原版苦力怕（以及 {@link cn.autoforged.yuzusoft.entity.custom.SuzuneEntity}）一致：
 * 目标进入 3 格内开始蓄力并播放引信音效，目标消失、离开 7 格或失去视线则取消蓄力。
 * 爆炸半径由头部档案决定（苦力怕 3 格、铃音 8 格）。
 */
public class FusionSelfDestructGoal extends Goal {
    private static final int MAX_SWELL = 30;
    private static final double TRIGGER_DISTANCE_SQR = 9.0D;   // 3 格内开始蓄力
    private static final double ABANDON_DISTANCE_SQR = 49.0D;  // 7 格外放弃蓄力

    private final Mob mob;
    private final float explosionRadius;
    /** -1 = 空闲，>0 = 蓄力中。 */
    private int swell = -1;

    public FusionSelfDestructGoal(Mob mob, float explosionRadius) {
        this.mob = mob;
        this.explosionRadius = explosionRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return this.swell > 0
                || (target != null && this.mob.distanceToSqr(target) < TRIGGER_DISTANCE_SQR);
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        setSwell(1);
    }

    @Override
    public void stop() {
        setSwell(-1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            setSwell(-1);
            return;
        }
        if (this.mob.distanceToSqr(target) > ABANDON_DISTANCE_SQR
                || !this.mob.getSensing().hasLineOfSight(target)) {
            setSwell(-1);
            return;
        }
        this.swell++;
        if (this.swell >= MAX_SWELL) {
            explode();
        }
    }

    private void setSwell(int dir) {
        if (dir > 0 && this.swell <= 0) {
            this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    SoundEvents.CREEPER_PRIMED, this.mob.getSoundSource(), 1.0F, 0.5F);
        }
        this.swell = dir;
    }

    private void explode() {
        Level level = this.mob.level();
        if (level.isClientSide) {
            return;
        }
        // 等价于原版苦力怕内部的 dead = true（protected，外部改不了）：
        // 血量归零后 isAlive() 为 false，爆炸反噬走到 hurt() 会直接返回 false，
        // 不会触发 die()，因此自爆不掉落头颅/躯体（与原版苦力怕一致）。
        this.mob.setHealth(0.0F);
        level.explode(this.mob, this.mob.getX(), this.mob.getY() + this.mob.getEyeHeight() / 2.0, this.mob.getZ(),
                this.explosionRadius, Level.ExplosionInteraction.MOB);
        this.mob.discard();
    }
}