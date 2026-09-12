package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import java.util.List;
import cn.autoforged.yuzusoft.effect.ModEffects;

public class DetonatorProjectileEntity extends ThrowableItemProjectile {

    public DetonatorProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DetonatorProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.DETONATOR_PROJECTILE.get(), shooter, level);
    }

    public DetonatorProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntities.DETONATOR_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DETONATOR.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide()) return;

        // 执行爆炸
        LivingEntity shooter;
        if (getOwner() instanceof LivingEntity living) {
            shooter = living;
        } else {
            shooter = null;
        }
        double radius = 2.0D;

        ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator() {
            @Override
            public float getEntityDamageAmount(Explosion explosion, Entity entity) {
                // 如果是投掷者，直接0伤害，免疫爆炸
                if (shooter != null && entity == shooter) {
                    return 0F;
                }
                // 其余实体使用原版爆炸伤害计算逻辑
                return super.getEntityDamageAmount(explosion, entity);
            }
        };
        level().explode(
                (Entity) this,
                (DamageSource) null,
                damageCalculator,
                getX(), getY(), getZ(),
                (float) radius,
                false,
                Level.ExplosionInteraction.MOB
        );

        List<LivingEntity> hitEntities = level().getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(this.position(), radius * 2, radius * 2, radius * 2)
        );

        for (LivingEntity entity : hitEntities) {
            if (entity == this.getOwner()) continue;
            MobEffectInstance debuff = new MobEffectInstance(
                    ModEffects.EFFECT_0721.getDelegate(), // 你的自定义效果注册实例
                    200,                          // 持续帧数 20tick=1秒
                    0                             // amplifier 等级 0=I级、1=II级
            );
            entity.addEffect(debuff, this.getOwner()); // 第二个参数为效果来源
        }
        // ==========================================================

        discard();
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {

    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {

    }
}
