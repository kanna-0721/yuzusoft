package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
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

public class FlashbangProjectile extends ThrowableItemProjectile {
    public FlashbangProjectile(EntityType<? extends FlashbangProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FlashbangProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.FLASHBANG_PROJECTILE.get(), shooter, level);
    }

    public FlashbangProjectile(Level level, double x, double y, double z) {
        super(ModEntities.FLASHBANG_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FLASHBANG.get();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            if (result.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) result).getEntity();
                boolean damaged = entity.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
                if (damaged && entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), this.getOwner());
                }
            }
            this.discard();
        }
    }
}

