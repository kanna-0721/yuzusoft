package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

public class WaterBallEntity extends ThrowableItemProjectile {
    private static final float DAMAGE = 4.0f;

    public WaterBallEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public WaterBallEntity(Level level, LivingEntity shooter) {
        super(ModEntities.WATER_BALL.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WATER_BALL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        boolean damaged = entity.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);

        if (entity instanceof LivingEntity living && damaged) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
            if (ModEffects.EFFECT_0721 != null) {
                living.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721, 300, 0));
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            BlockPos pos = result.getBlockPos().relative(result.getDirection());
            if (this.level().isEmptyBlock(pos)) {
                this.level().setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.WATER_HIT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            this.discard();
        }
    }
}
