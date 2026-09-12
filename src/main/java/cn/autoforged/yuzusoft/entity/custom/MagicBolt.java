package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class MagicBolt extends ThrowableItemProjectile {
    private float damage = 6.0F;

    public MagicBolt(EntityType<? extends MagicBolt> entityType, Level level) {
        super(entityType, level);
    }

    public MagicBolt(Level level, LivingEntity shooter) {
        super(ModEntities.MAGIC_BOLT.get(), shooter, level);
    }

    public MagicBolt(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.MAGIC_BOLT.get(), shooter, level);
        this.damage = damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_CREAM;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().indirectMagic(this, owner);
        target.hurt(source, damage);
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
    public void tick() {
        super.tick();
        if (this.tickCount > 60) {
            this.discard();
        }
    }
}