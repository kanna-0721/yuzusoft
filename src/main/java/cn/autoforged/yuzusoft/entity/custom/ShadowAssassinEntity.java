package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShadowAssassinEntity extends Monster implements RangedAttackMob {
    public ShadowAssassinEntity(EntityType<? extends ShadowAssassinEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SHADOW_DART.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 50, 15.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ShadowDartEntity dart = new ShadowDartEntity(this.level(), this);
        double aimX = target.getX();
        double aimY = target.getY(0.5D);
        double aimZ = target.getZ();
        double dx = aimX - this.getX();
        double dy = aimY - this.getEyeY();
        double dz = aimZ - this.getZ();
        dart.shoot(dx, dy, dz, 2.0F, distanceFactor * 0.6F);
        this.playSound(ModSounds.SHADOW_DART_THROW.get(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(dart);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) return false;
        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity && this.random.nextFloat() < 0.6F) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D;
            double y = this.getY() + (double) (this.random.nextInt(8) - 4);
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D;
            if (this.randomTeleport(x, y, z, true)) {
                this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = switch (this.level().getDifficulty()) {
            case EASY -> 3.0F;
            case HARD -> 6.0F;
            default -> 4.0F;
        };
        boolean flag = target.hurt(this.damageSources().mobAttack(this), damage);
        if (flag && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        return flag;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHADOW_ASSASSIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SHADOW_ASSASSIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHADOW_ASSASSIN_DEATH.get();
    }

}

