package cn.autoforged.yuzusoft.entity.custom;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ShadowDartEntity extends ThrowableItemProjectile {
    public ShadowDartEntity(EntityType<? extends ShadowDartEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ShadowDartEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SHADOW_DART.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHADOW_DART.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        boolean damageSuccess = entity.hurt(this.damageSources().thrown(this, this.getOwner()), 6.0F);
        if (damageSuccess && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721, 100, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}

