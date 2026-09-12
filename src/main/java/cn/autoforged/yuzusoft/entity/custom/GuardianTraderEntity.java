package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GuardianTraderEntity extends PathfinderMob implements Npc, Merchant {
    private static final EntityDataAccessor<Boolean> DATA_ANGRY =
            SynchedEntityData.defineId(GuardianTraderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int ANGER_DURATION = 300;
    private static final float ANGER_RADIUS = 16.0f;
    private int angerTimer = 0;
    private int golemSpawnCooldown = 0;
    private static final int GOLEM_SPAWN_COOLDOWN = 600;
    @Nullable
    private Player tradingPlayer;
    private final MerchantOffers offers = new MerchantOffers();
    public GuardianTraderEntity(EntityType<? extends GuardianTraderEntity> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CYCLONE_SWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.TAMAGOYAKI.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGRY, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ShadowAssassinEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Slime.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MagmaCube.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Hoglin.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Shulker.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Phantom.class, true));
    }

    public boolean isAngry() {
        return this.entityData.get(DATA_ANGRY);
    }

    public void setAngry(boolean angry) {
        boolean prev = this.entityData.get(DATA_ANGRY);
        if (prev != angry) {
            this.entityData.set(DATA_ANGRY, angry);
            if (angry && !this.level().isClientSide) {
                this.playSound(ModSounds.GUARDIANS_ANGRY.get(), 1.0f, 1.0f);
            }
        }
    }
    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            if (target instanceof Monster || this.isHostileWithinRadius()) {
                this.setAngry(true);
                this.angerTimer = ANGER_DURATION;
            } else if (this.angerTimer > 0) {
                this.angerTimer--;
                this.setAngry(true);
            } else {
                this.setAngry(false);
            }

            if (this.isAngry() && this.golemSpawnCooldown <= 0 && !this.level().isClientSide) {
                GuardianEntity guardian = ModEntities.GUARDIAN.get().create(this.level());
                if (guardian != null) {
                    guardian.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    this.level().addFreshEntity(guardian);
                }
                this.golemSpawnCooldown = GOLEM_SPAWN_COOLDOWN;
            }
            if (this.golemSpawnCooldown > 0) {
                this.golemSpawnCooldown--;
            }
        }
    }

    private boolean isHostileWithinRadius() {
        return !this.level().getEntitiesOfClass(Monster.class,
                this.getBoundingBox().inflate(ANGER_RADIUS), m -> m.isAlive() && this.canAttack(m)).isEmpty();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            this.setAngry(true);
            this.angerTimer = ANGER_DURATION;
            if (this.getTarget() == null) {
                this.setTarget(attacker);
            }
        }
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAngry()) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
            this.playSound(ModSounds.GUARDIANS_IDLE.get(), 1.0f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }
    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }
    public boolean isTrading() {
        return this.tradingPlayer != null;
    }
    @Override
    public MerchantOffers getOffers() {
        if (this.offers.isEmpty()) {
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.EGG, 4),
                    new ItemStack(Items.EMERALD, 1),
                    16, 2, 0.0f));
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.TAMAGOYAKI.get(), 4),
                    16, 4, 0.0f));
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.SHADOW_DART.get(), 16),
                    16, 4, 0.0f));
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.DIAMOND_SWORD, 1),
                    Optional.of(new ItemCost(Items.EMERALD, 32)),
                    new ItemStack(ModItems.CYCLONE_SWORD.get(), 1),
                    1,
                    16,
                    0.0f
            ));
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 4),
                    new ItemStack(ModItems.GUARDIAN_SPAWN_EGG.get(), 1),
                    16, 0, 0.0f));
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.playSound(ModSounds.GUARDIANS_TRADE.get(), 1.0f, 1.0f);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (!this.level().isClientSide) {
            this.playSound(ModSounds.GUARDIANS_IDLE.get(), 1.0f, 1.0f);
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.GUARDIANS_TRADE.get();
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.setTradingPlayer(null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? ModSounds.GUARDIANS_ANGRY.get() : ModSounds.GUARDIANS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.GUARDIANS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GUARDIANS_DEATH.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.yuzusoft.guardian_trader_trade");
    }
}

