package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.custom.WaterOrbEntity;
import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 愤怒射击：每 SHOT_INTERVAL_SECONDS（默认 1.5 秒）消耗 SHOT_WATER_COST（默认 0.2）
 * 水位，向当前目标发射一枚带水花粒子的水弹。水位不足 0.2 时停止射击（交给近战）。
 */
public class WaterSpiritShootGoal extends Goal {
    /** 停止前进的射击距离：与目标距离 ≤ 12 格时原地射击，不再靠近。 */
    private static final double STOP_DISTANCE = 12.0D;
    private final WaterSpiritEntity spirit;
    private int shotTimer;

    public WaterSpiritShootGoal(WaterSpiritEntity spirit) {
        this.spirit = spirit;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.spirit.getTarget();
        if (target == null || !target.isAlive() || !this.spirit.isAngry()) {
            return false;
        }
        if (!this.spirit.canShootMore()) {
            return false;
        }
        double range = WaterSpiritConfig.SHOT_MAX_RANGE.get();
        return this.spirit.distanceToSqr(target) <= range * range && !target.isInvisible();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.shotTimer = WaterSpiritConfig.secondsToTicks(WaterSpiritConfig.SHOT_INTERVAL_SECONDS.get().doubleValue()) / 2;
    }

    @Override
    public void tick() {
        LivingEntity target = this.spirit.getTarget();
        if (target == null) {
            return;
        }
        this.spirit.getLookControl().setLookAt(target, 30.0F, 30.0F);
        // 12 格外持续靠近目标；进入 12 格内停下（保持面向并原地射击）
        double distSqr = this.spirit.distanceToSqr(target);
        if (distSqr > STOP_DISTANCE * STOP_DISTANCE) {
            this.spirit.getNavigation().moveTo(target, 1.0D);
        } else {
            // PathNavigation 不会因不再 moveTo 而自动停，须显式 stop 以中断旧路径
            this.spirit.getNavigation().stop();
        }
        if (--this.shotTimer <= 0) {
            this.shotTimer = WaterSpiritConfig.secondsToTicks(WaterSpiritConfig.SHOT_INTERVAL_SECONDS.get().doubleValue());
            if (this.spirit.canShootMore()) {
                fireOrb(target);
            }
        }
    }

    private void fireOrb(LivingEntity target) {
        if (!(this.spirit.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        WaterOrbEntity orb = new WaterOrbEntity(this.spirit.level(), this.spirit);
        Vec3 look = this.spirit.getLookAngle();
        Vec3 spawn = this.spirit.getEyePosition().add(look.scale(0.6D));
        orb.setPos(spawn.x, spawn.y, spawn.z);
        Vec3 dir = target.getEyePosition().subtract(spawn).normalize();
        // 直线弹道（水弹无重力），无需抬升补偿
        orb.shoot(dir.x, dir.y, dir.z, 1.35F, 0.1F);
        serverLevel.addFreshEntity(orb);
        this.spirit.setWaterLevel(this.spirit.getWaterLevel()
                - WaterSpiritConfig.SHOT_WATER_COST.get().floatValue());
        this.spirit.level().playSound(null, this.spirit, ModSounds.WATER_SPIRIT_SHOOT.get(),
                SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
