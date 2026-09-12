package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VillageGuardianEntity extends Animal {
    private static final int AURA_INTERVAL = 20;
    private static final double AURA_RADIUS_OUTER = 8.0;
    private static final double AURA_RADIUS_INNER = 4.0;
    private static final int EFFECT_DURATION = 100;
    private static final int PARTICLE_INTERVAL = 10;
    private int auraTickCounter = 0;
    private int particleTickCounter = 0;

    public VillageGuardianEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide()) {
            particleTickCounter++;
            if (particleTickCounter >= PARTICLE_INTERVAL) {
                particleTickCounter = 0;
                spawnAuraParticles();
            }
        } else if (level() instanceof ServerLevel serverLevel) {
            auraTickCounter++;
            if (auraTickCounter >= AURA_INTERVAL) {
                auraTickCounter = 0;
                applyAuraEffects(serverLevel);
            }
        }
    }

    private void applyAuraEffects(ServerLevel serverLevel) {
        AABB outerBox = getBoundingBox().inflate(AURA_RADIUS_OUTER);
        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                LivingEntity.class, outerBox, entity ->
                        entity != this && entity.isAlive() && !entity.isRemoved());

        for (LivingEntity target : nearbyEntities) {
            double dist = Math.sqrt(this.distanceToSqr(target));
            if (dist <= AURA_RADIUS_INNER) {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 1, false, false, true), this);
            } else if (dist <= AURA_RADIUS_OUTER) {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, false, false, true), this);
            }
        }
    }

    private void spawnAuraParticles() {
        RandomSource random = this.random;
        double angle = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < 3; i++) {
            double radius = 2.0 + random.nextDouble() * 6.0;
            double spiralAngle = angle + (double) i * Math.PI * 2 / 3;
            double px = getX() + Math.cos(spiralAngle) * radius;
            double pz = getZ() + Math.sin(spiralAngle) * radius;
            double py = getY() + 0.5 + random.nextDouble() * 1.5;
            level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02, 0);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.VILLAGE_GUARDIAN.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VILLAGE_GUARDIAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.VILLAGE_GUARDIAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VILLAGE_GUARDIAN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }


    public static boolean checkVillageGuardianSpawnRules(
            EntityType<? extends Animal> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (!Animal.checkAnimalSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            int existingCount = serverLevel.getEntitiesOfClass(
                    VillageGuardianEntity.class,
                    new AABB(pos).inflate(64.0),
                    entity -> entity.isAlive() && !entity.isRemoved()).size();
            if (existingCount > 0) {
                return false;
            }
        }
        return true;
    }
}

