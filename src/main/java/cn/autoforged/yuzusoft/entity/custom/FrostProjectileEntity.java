package cn.autoforged.yuzusoft.entity.custom;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class FrostProjectileEntity extends ThrowableItemProjectile {

    public static final float DAMAGE = 8.0F;
    public static final int SLOWNESS_DURATION = 100;
    public static final int SLOWNESS_LEVEL = 1;

    public FrostProjectileEntity(EntityType<? extends FrostProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FrostProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FROST_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();
    }
    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        net.minecraft.world.entity.Entity targetEntity = result.getEntity();
        if (targetEntity instanceof net.minecraft.world.entity.player.Player
                || targetEntity instanceof FrostGuardianEntity) {
            return;
        }
        // 友军误伤防护：不打主人旗下其它驯服宠物（避免引发它们反击）
        if (targetEntity instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isTame()
                && this.getOwner() instanceof net.minecraft.world.entity.TamableAnimal shooter && shooter.isTame()
                && shooter.getOwnerUUID() != null && shooter.getOwnerUUID().equals(pet.getOwnerUUID())) {
            return;
        }

        targetEntity.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);

        if (targetEntity instanceof LivingEntity living) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    SLOWNESS_DURATION, SLOWNESS_LEVEL - 1));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}
