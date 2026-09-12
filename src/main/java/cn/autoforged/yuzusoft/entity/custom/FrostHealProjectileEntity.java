package cn.autoforged.yuzusoft.entity.custom;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;


public class FrostHealProjectileEntity extends ThrowableItemProjectile {
    public static final int HEAL_EFFECT_DURATION = 1;
    public static final int HEAL_EFFECT_LEVEL = 0;
    public FrostHealProjectileEntity(EntityType<? extends FrostHealProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FrostHealProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FROST_HEAL_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.APPLE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        if (result.getEntity() instanceof Player || result.getEntity() instanceof FrostGuardianEntity) {
            if (result.getEntity() instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.HEAL, HEAL_EFFECT_DURATION, HEAL_EFFECT_LEVEL));
            }
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

