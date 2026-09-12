package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ai.SentinelHybridAttackGoal;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class FloatingSentinelEntity extends Monster implements RangedAttackMob {

    public static final double MELEE_RANGE = 2.0;
    public static final double RANGED_RANGE = 16.0;
    private static final int LEVITATION_DURATION = 100;
    private static final int LEVITATION_AMPLIFIER = 0;

    public FloatingSentinelEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SentinelHybridAttackGoal(this, 1.0));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new AvoidEntityGoal(this, Ocelot.class, 6.0F, (double)1.0F, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal(this, Cat.class, 6.0F, (double)1.0F, 1.2));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (level().isClientSide()) {
            return;
        }
        LevitationBulletEntity bullet = new LevitationBulletEntity(level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        bullet.shoot(dx, dy, dz, 1.0F, 0.2F);
        level().addFreshEntity(bullet);
        this.playSound(SoundEvents.SHULKER_SHOOT, 1.0F, 0.8F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION, LEVITATION_AMPLIFIER));
        }
        return hurt;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FLOATING_SENTINEL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.FLOATING_SENTINEL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FLOATING_SENTINEL_DEATH.get();
    }

    public static boolean checkFloatingSentinelSpawnRules(
            EntityType<FloatingSentinelEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }
}

