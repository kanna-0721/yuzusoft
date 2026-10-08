package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 水弹：水灵愤怒时与雨伞主手右键发射。带水花粒子，命中造成可配置伤害并击退。
 * 命中实体与落地时向反弹系统提供“被反弹后可折返”的行为（见 ProjectileReflectHandler）。
 */
public class WaterOrbEntity extends ThrowableProjectile {

    /** 最大飞行寿命（tick）：无重力直线弹道未命中时也不会永久悬浮，超时自动消散。 */
    private static final int MAX_LIFE_TICKS = 100;

    public WaterOrbEntity(EntityType<? extends WaterOrbEntity> type, Level level) {
        super(type, level);
    }

    public WaterOrbEntity(Level level, LivingEntity shooter) {
        super(ModEntities.WATER_ORB.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        // 无重力直线弹道（负值会导致每 tick 向上加速、打不中目标）
        return 0.0D;
    }

    @Override
    public void tick() {
        super.tick();
        // 未命中超时自动消散（约 5 秒，无重力直线飞行约 135 格）
        if (this.tickCount > MAX_LIFE_TICKS) {
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            // 飞行水花尾迹
            double jitter = 0.12;
            this.level().addParticle(ParticleTypes.SPLASH,
                    this.getX() + (this.random.nextDouble() - 0.5) * jitter,
                    this.getY() + (this.random.nextDouble() - 0.5) * jitter,
                    this.getZ() + (this.random.nextDouble() - 0.5) * jitter,
                    0.0D, -0.02D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        LivingEntity shooter = this.getOwner() instanceof LivingEntity le ? le : null;
        float damage = WaterSpiritConfig.SHOT_DAMAGE.get().floatValue();
        DamageSource source = this.damageSources().mobProjectile(this, shooter);
        if (target.hurt(source, damage)) {
            double kb = WaterSpiritConfig.SHOT_KNOCKBACK.get();
            Vec3 dir = target.position().subtract(this.position());
            Vec3 flat = new Vec3(dir.x, 0.0D, dir.z);
            double len = flat.length();
            if (len > 1.0E-3D) {
                target.setDeltaMovement(target.getDeltaMovement()
                        .add(flat.x / len * kb, kb * 0.25D, flat.z / len * kb));
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY(), this.getZ(), 15, 0.15, 0.15, 0.15, 0.1);
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // 水灵与水弹本身互不命中（水与同族），防止反弹后自伤与连环命中
        return !(entity instanceof WaterSpiritEntity)
                && !(entity instanceof WaterOrbEntity)
                && super.canHitEntity(entity);
    }
}
