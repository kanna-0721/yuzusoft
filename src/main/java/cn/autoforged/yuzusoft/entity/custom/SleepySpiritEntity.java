package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.ai.SleepInBedGoal;
import cn.autoforged.yuzusoft.sound.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

public class SleepySpiritEntity extends Animal {
    private static final SimpleWeightedRandomList<Item> GIFT_POOL = SimpleWeightedRandomList.<Item>builder()
            .add(Items.COAL,30)
            .add(Items.IRON_INGOT, 15)
            .add(Items.REDSTONE, 15)
            .add(Items.LAPIS_LAZULI, 15)
            .add(Items.GOLD_INGOT, 10)
            .add(Items.EMERALD, 14)
            .add(Items.DIAMOND, 1)
            .build();

    private static final String NBT_KEY_BOUND_BED = "BoundBed";
    private int wakeCooldown;
    @Nullable
    private BlockPos boundBed;
    public SleepySpiritEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, 0.0);
    }

    public static boolean checkSleepySpiritSpawnRules(
            EntityType<SleepySpiritEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SleepInBedGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.BREAD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.wakeCooldown > 0) {
            this.wakeCooldown--;
        }
        if (this.isSleeping() && this.level().isDay()) {
            this.wakeUp();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 14) {
            for (int i = 0; i < 5; i++) {
                double d0 = this.random.nextGaussian() * 0.02;
                double d1 = this.random.nextGaussian() * 0.02;
                double d2 = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d0, d1, d2);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    public int getWakeCooldown() {
        return this.wakeCooldown;
    }

    public void onSleepStarted(BlockPos bedHead) {
        this.boundBed = bedHead.immutable();
        this.bindBedLock(bedHead);
        this.playSound(ModSounds.SPIRIT_SLEEP.get(), 0.4f, 0.9f);
        this.level().broadcastEntityEvent(this, (byte) 14);
    }

    public void wakeUp() {
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.releaseBedLock();
        this.wakeCooldown = 200;
    }

    @Override
    public void stopSleeping() {
        super.stopSleeping();
        this.releaseBedLock();
    }

    public void wakeUpAndGift(ServerPlayer player) {
        if (!this.isSleeping()) {
            return;
        }
        this.wakeUp();
        if (this.level().isClientSide) {
            return;
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.SPIRIT_GIFT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    this.getX(), this.getY() + 1.4, this.getZ(),
                    10, 0.4, 0.4, 0.4, 0.0);
        }
        ItemStack gift = new ItemStack(GIFT_POOL.getRandomValue(this.random).orElse(Items.IRON_INGOT));
        if (!player.getInventory().add(gift)) {
            player.drop(gift, false);
        }
    }

    private void bindBedLock(BlockPos bedHead) {
        if (this.level() instanceof ServerLevel serverLevel) {
            PoiManager poiManager = serverLevel.getPoiManager();
            if (poiManager.existsAtPosition(PoiTypes.HOME, bedHead)) {
                poiManager.take(h -> h.is(PoiTypes.HOME), (type, pos) -> pos.equals(bedHead), bedHead, 1);
            }
        }
    }

    public void releaseBedLock() {
        BlockPos head = this.boundBed;
        if (head == null) {
            return;
        }
        this.boundBed = null;
        if (this.level() instanceof ServerLevel serverLevel) {
            PoiManager poiManager = serverLevel.getPoiManager();
            if (poiManager.existsAtPosition(PoiTypes.HOME, head)) {
                try {
                    poiManager.release(head);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onRemovedFromLevel() {
        this.releaseBedLock();
        super.onRemovedFromLevel();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BREAD);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.SLEEPY_SPIRIT.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? null : ModSounds.SLEEPY_SPIRIT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.SLEEPY_SPIRIT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SLEEPY_SPIRIT_DEATH.get();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean flag = super.hurt(source, amount);
        if (flag && !this.level().isClientSide) {
            this.wakeCooldown = 600;
        }
        return flag;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("WakeCooldown", this.wakeCooldown);
        if (this.boundBed != null) {
            compound.put(NBT_KEY_BOUND_BED, NbtUtils.writeBlockPos(this.boundBed));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.wakeCooldown = compound.getInt("WakeCooldown");
        this.boundBed = NbtUtils.readBlockPos(compound, NBT_KEY_BOUND_BED).orElse(null);
    }
}