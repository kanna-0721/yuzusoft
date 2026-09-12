package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LevitationBulletEntity extends Projectile {
    private static final float DAMAGE = 3.0F;
    private static final int EFFECT_DURATION = 100;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final int MAX_LIFE = 120;

    public LevitationBulletEntity(EntityType<? extends LevitationBulletEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public LevitationBulletEntity(Level level, LivingEntity shooter) {
        this(ModEntities.LEVITATION_BULLET.get(), level);
        this.setOwner(shooter);
        Vec3 center = shooter.getBoundingBox().getCenter();
        this.moveTo(center.x, center.y, center.z, shooter.getYRot(), shooter.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();

        if (!level().isClientSide()) {
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.hitTargetOrDeflectSelf(hitResult);
            }
        }

        this.checkInsideBlocks();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        if (level().isClientSide()) {
            // 飞行轨迹发光粒子
            level().addParticle(ParticleTypes.END_ROD,
                    this.getX() - movement.x, this.getY() - movement.y + 0.15, this.getZ() - movement.z,
                    0.0, 0.0, 0.0);
        }

        if (this.tickCount > MAX_LIFE) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide()) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            LivingEntity shooter = owner instanceof LivingEntity living ? living : null;
            if (target.hurt(this.damageSources().mobProjectile(this, shooter), DAMAGE)) {
                if (target instanceof LivingEntity living) {
                    // effect_on_hit: 飘浮 100 ticks, amplifier 0
                    living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, EFFECT_DURATION, EFFECT_AMPLIFIER));
                }
            }
            spawnHitParticles();
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            spawnHitParticles();
        }
        this.discard();
    }

    private void spawnHitParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.55F, 0.2F, 0.8F),
                    this.getX(), this.getY(), this.getZ(), 24, 0.35, 0.35, 0.35, 0.1);
            serverLevel.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    this.getX(), this.getY(), this.getZ(), 16, 0.4, 0.4, 0.4, 0.05);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !target.noPhysics;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setDeltaMovement(packet.getXa(), packet.getYa(), packet.getZa());
    }
}

