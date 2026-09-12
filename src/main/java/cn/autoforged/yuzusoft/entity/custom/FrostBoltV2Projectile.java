package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 冰霜守卫 V2 发射的寒冰弹（投掷物风格弹体）。
 * - 无重力（projectile_gravity=false）
 * - 命中目标造成伤害 + 减速 + 击退
 * - 视觉上复用雪球的投掷物外观（无额外贴图依赖）
 */
public class FrostBoltV2Projectile extends ThrowableItemProjectile {
    private float damage = 5.0F;
    private int slowDurationTicks = 100;
    private int slowAmplifier = 1;
    private double knockbackStrength = 2.0;
    // 悬浮寿命：射出后最多存活约 5 秒(100 tick)。无重力弹体若未命中会一直飘，
    // 短寿命可避免大量寒冰弹长期悬浮堆积。14 格射程在弹速 1.2 下约 12 tick 就能到达，余量充足。
    private int maxLifetime = 100;

    public FrostBoltV2Projectile(EntityType<? extends FrostBoltV2Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    public FrostBoltV2Projectile(Level level, LivingEntity shooter) {
        super(ModEntities.FROST_BOLT_V2.get(), shooter, level);
        this.setNoGravity(true);
    }

    public FrostBoltV2Projectile(Level level, double x, double y, double z) {
        super(ModEntities.FROST_BOLT_V2.get(), x, y, z, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) {
            return;
        }
        Entity target = result.getEntity();
        if (target instanceof LivingEntity living) {
            // 伤害
            Entity owner = this.getOwner();
            boolean hurt = living.hurt(this.damageSources().mobProjectile(this,
                owner instanceof LivingEntity livingOwner ? livingOwner : null), this.damage);
            if (hurt) {
                // 减速效果
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    this.slowDurationTicks, this.slowAmplifier), this.getOwner());
                // 0721 效果（攻击伤害降低），与减速同时长连携
                living.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721,
                    this.slowDurationTicks, 1), this.getOwner());
                // 击退
                Vec3 dir = this.getDeltaMovement();
                if (dir.lengthSqr() > 1.0E-4) {
                    living.setDeltaMovement(living.getDeltaMovement().add(
                        dir.x * this.knockbackStrength * 0.3, 0.2, dir.z * this.knockbackStrength * 0.3));
                    living.hurtMarked = true;
                }
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 交由 onHitEntity / onHitBlock 处理，这里仅做通用命中即消失的兜底
        super.onHit(result);
    }

    @Override
    public void tick() {
        super.tick();
        // 短寿命：超过存活上限未命中则自动消散，避免无重力弹体长期悬浮堆积
        if (this.tickCount > this.maxLifetime) {
            this.discard();
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(), 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("FrostDamage")) {
            this.damage = compound.getFloat("FrostDamage");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("FrostDamage", this.damage);
    }
}