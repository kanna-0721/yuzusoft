package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.event.ModKeyMappings;
import cn.autoforged.yuzusoft.sound.ModSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;

public class DualFormMobEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_ANGRY =
            SynchedEntityData.defineId(DualFormMobEntity.class, EntityDataSerializers.BOOLEAN);
    /** 竖直输入：1=上升，-1=下降，0=无。由客户端根据空格/X 键写入并同步到服务端。 */
    private static final EntityDataAccessor<Byte> DATA_VERTICAL_INPUT =
            SynchedEntityData.defineId(DualFormMobEntity.class, EntityDataSerializers.BYTE);

    private int healTimer = 0;

    public DualFormMobEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 54.0)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_SPEED, 1.0)
                .add(Attributes.FLYING_SPEED, 1.0)
                .add(Attributes.ARMOR, 2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGRY, false);
        builder.define(DATA_VERTICAL_INPUT, (byte) 0);
    }

    public byte getVerticalInput() {
        return this.entityData.get(DATA_VERTICAL_INPUT);
    }

    public void setVerticalInput(byte value) {
        this.entityData.set(DATA_VERTICAL_INPUT, value);
    }

    public boolean isAngry() {
        return this.entityData.get(DATA_ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(DATA_ANGRY, angry);
        if (angry && this.isVehicle()) {
            this.ejectPassengers();
        }
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ConditionalMeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(2, new FollowNearestPlayerGoal(this, 1.0, 50.0f, 3.0f));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new ConditionalNearestAttackableTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Slime.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MagmaCube.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Hoglin.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zoglin.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Shulker.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Phantom.class, true));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.isEmpty() && player.isShiftKeyDown() && !this.isAngry() && !this.isVehicle()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (stack.isEmpty()) {
            if (!this.level().isClientSide()) {
                this.setAngry(!this.isAngry());
                this.updateAttackAttribute();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    private void updateAttackAttribute() {
        if (this.isAngry()) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0);
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            healTimer++;
            if (healTimer >= 20) {
                healTimer = 0;
                if (this.getHealth() < this.getMaxHealth() && this.getHealth() > 0) {
                    this.heal(2.0f);
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }
    private boolean wasFlying = false;

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.5f);
            this.xRotO = this.getXRot();
            this.setYHeadRot(this.getYRot());

            float forward = player.zza;
            float strafe = player.xxa;
            float speed = 0.6f;

            // 空格上升 / 下降键下降：由客户端读真实按键写入竖直输入并同步到服务端。
            // 下降用 GLFW 物理键直读（InputConstants.isKeyDown），绕开 KeyMapping 冲突机制——
            // 环境里其他模组若默认占用同键，KeyMapping.isDown() 会被冲突检测强制为 false。
            if (this.level().isClientSide()) {
                long window = Minecraft.getInstance().getWindow().getWindow();
                int up = Minecraft.getInstance().options.keyJump.isDown() ? 1 : 0;
                InputConstants.Key descendKey = ModKeyMappings.DUALFORM_DESCEND_KEY.get().getKey();
                int down = (descendKey.getType() == InputConstants.Type.KEYSYM
                        && InputConstants.isKeyDown(window, descendKey.getValue())) ? 1 : 0;
                this.setVerticalInput((byte) (up - down));
            }
            float vertical = this.getVerticalInput() * speed;

            Vec3 movement = new Vec3(strafe * speed, vertical, forward * speed);
            movement = movement.yRot((float) Math.toRadians(-this.getYRot()));

            this.setDeltaMovement(movement);
            this.setNoGravity(true);

            this.fallDistance = 0.0f;
            player.fallDistance = 0.0f;

            this.move(MoverType.SELF, this.getDeltaMovement());

            this.fallDistance = 0.0f;
            player.fallDistance = 0.0f;
            this.wasFlying = true;

        } else {
            if (this.wasFlying) {
                this.fallDistance = 0.0f;
                this.wasFlying = false;
            }
            this.setNoGravity(false);
            super.travel(travelVector);
        }
    }


    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (!this.isAngry() && this.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player && !this.isAngry();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Angry", this.isAngry());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setAngry(compound.getBoolean("Angry"));
        this.updateAttackAttribute();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return ModSounds.DUALFORMMOB_ANGRY.get();
        }
        return ModSounds.DUALFORMMOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.DUALFORMMOB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUALFORMMOB_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
    }

    @Override
    public int getAmbientSoundInterval() {
        return 150;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    private static class FollowNearestPlayerGoal extends Goal {
        private final DualFormMobEntity entity;
        @Nullable
        private Player target;
        private final double speedModifier;
        private final double stopDistance;
        private final double startDistance;

        public FollowNearestPlayerGoal(DualFormMobEntity entity, double speedModifier, float startDistance, float stopDistance) {
            this.entity = entity;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // 攻击形态专注打怪，不跟随玩家，避免与近战目标抢控制权来回跑。
            if (this.entity.isAngry()) return false;
            Player nearest = this.entity.level().getNearestPlayer(this.entity, this.startDistance);
            if (nearest != null && !nearest.isSpectator() && nearest.isAlive()) {
                this.target = nearest;
                return this.entity.distanceToSqr(this.target) > this.stopDistance * this.stopDistance;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.entity.isAngry()) return false;
            if (this.target == null || !this.target.isAlive() || this.target.isSpectator()) return false;
            double dist = this.entity.distanceToSqr(this.target);
            return dist <= this.startDistance * this.startDistance && dist > this.stopDistance * this.stopDistance;
        }

        @Override
        public void start() {
            this.entity.getNavigation().moveTo(this.target, this.speedModifier);
        }

        @Override
        public void stop() {
            this.target = null;
            this.entity.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            this.entity.getNavigation().moveTo(this.target, this.speedModifier);
        }
    }

    private static class ConditionalMeleeAttackGoal extends MeleeAttackGoal {
        private final DualFormMobEntity entity;

        public ConditionalMeleeAttackGoal(DualFormMobEntity entity, double speedModifier, boolean followEvenIfNotSeen) {
            super(entity, speedModifier, followEvenIfNotSeen);
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return this.entity.isAngry() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.entity.isAngry() && super.canContinueToUse();
        }
    }

    private static class ConditionalNearestAttackableTargetGoal extends NearestAttackableTargetGoal<Monster> {
        private final DualFormMobEntity entity;

        @SuppressWarnings("unchecked")
        public ConditionalNearestAttackableTargetGoal(DualFormMobEntity entity) {
            super(entity, (Class<Monster>) (Class<?>) Monster.class, 10, true, true,
                    target -> entity.isAngry() && target instanceof Monster && target.isAlive());
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return this.entity.isAngry() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.entity.isAngry() && super.canContinueToUse();
        }
    }
}