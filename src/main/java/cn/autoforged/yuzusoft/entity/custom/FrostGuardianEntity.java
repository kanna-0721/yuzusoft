package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.ai.FrostMeleeAttackGoal;
import cn.autoforged.yuzusoft.entity.ai.FrostRangedAttackGoal;
import cn.autoforged.yuzusoft.entity.ai.HealOwnerGoal;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FrostGuardianEntity extends net.minecraft.world.entity.TamableAnimal implements RangedAttackMob {
    public static final double MAX_HEALTH = 40.0D;
    public static final double MOVEMENT_SPEED = 0.3D;
    public static final double FOLLOW_RANGE = 32.0D;
    public static final double ATTACK_DAMAGE = 8.0D;
    public static final int RANGED_COOLDOWN_TICKS = 40;
    public static final int MELEE_SLOWNESS_DURATION = 60;
    public static final int MELEE_SLOWNESS_LEVEL = 1;
    public static final int HEAL_COOLDOWN_TICKS = 400;
    public static final int HEAL_AMOUNT = 6;
    public static final int RANGED_SWITCH_DISTANCE = 6;
    public static final double RANGED_SWITCH_DIST_SQ = RANGED_SWITCH_DISTANCE * RANGED_SWITCH_DISTANCE;
    public static final double HEAL_RANGE_SQ = FOLLOW_RANGE * FOLLOW_RANGE;

    public int lastHealTime = 0;
    private boolean rangedMode = false;

    public FrostGuardianEntity(EntityType<? extends FrostGuardianEntity> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FrostMeleeAttackGoal(this));
        this.goalSelector.addGoal(2, new FrostRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new HealOwnerGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity target = this.mob.getLastHurtByMob();
                if (target instanceof Player player && FrostGuardianEntity.this.isOwnedBy(player)) {
                    return false;
                }
                return super.canUse();
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.updateCombatMode();
        }
    }

    private void updateCombatMode() {
        LivingEntity target = this.getTarget();
        boolean ranged = target != null && target.isAlive()
                && this.distanceToSqr(target) > RANGED_SWITCH_DIST_SQ;
        if (ranged != this.rangedMode) {
            this.rangedMode = ranged;
            this.spawnCombatSwitchFeedback();
        }
    }

    private void spawnCombatSwitchFeedback() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    12, 0.3D, 0.5D, 0.3D, 0.05D);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.level().isClientSide()) return;
        FrostProjectileEntity projectile = new FrostProjectileEntity(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - projectile.getY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.4F, 1.0F);
        this.level().addFreshEntity(projectile);
    }

    public void fireHealProjectile() {
        if (this.level().isClientSide()) return;
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) return;
        FrostHealProjectileEntity projectile = new FrostHealProjectileEntity(this.level(), this);
        double dx = owner.getX() - this.getX();
        double dy = owner.getEyeY() - projectile.getY();
        double dz = owner.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.2F, 0.0F);
        this.level().addFreshEntity(projectile);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        // 主人旗下其它驯服宠物视为盟友，不主动攻击（防止 OwnerHurtTargetGoal 追打主人自己的另一只宠物）
        if (target instanceof TamableAnimal other && other.isTame()
                && other.getOwnerUUID() != null && other.getOwnerUUID().equals(this.getOwnerUUID())) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean success = super.doHurtTarget(target);
        if (success && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    MELEE_SLOWNESS_DURATION, MELEE_SLOWNESS_LEVEL - 1));
        }
        return success;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide()) {
            Entity direct = source.getDirectEntity();
            Entity indirect = source.getEntity();
            if (indirect instanceof Player owner && this.isOwnedBy(owner)) {
                return false;
            }
            if (direct instanceof FrostGuardianEntity || indirect instanceof FrostGuardianEntity) {
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (stack.is(Items.BREAD)) {
                if (this.level().isClientSide()) {
                    return InteractionResult.CONSUME;
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    return InteractionResult.SUCCESS;
                }
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide()) {
                this.heal(HEAL_AMOUNT);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        FrostGuardianEntity baby = ModEntities.FROST_GUARDIAN.get().create(level);
        if (baby != null && this.isTame()) {
            baby.setOwnerUUID(this.getOwnerUUID());
            baby.setTame(true, true);
        }
        return baby;
    }
    @Override
    public int getAmbientSoundInterval(){
        return 200;
    }
    @Override
    public float getVoicePitch(){
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.0F + 1.6F
                : (this.random.nextFloat() - this.random.nextFloat()) * 0.0F + 1.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FROST_GUARDIAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.FROST_GUARDIAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FROST_GUARDIAN_DEATH.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BREAD);
    }
}

