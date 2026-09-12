package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * 冰霜守卫 V2（式部茉优）—— 人形敌对生物，由独立工程 mayu 并入 Yuzusoft。
 *
 * 两种模式（攻击 / 防御）通过距离触发的状态机切换：
 * - 攻击模式（默认）：面向目标缓慢移动，每隔 40 tick 朝目标发射一枚无重力的寒冰弹。
 * - 防御模式：当已锁定目标且目标在 8 格内时进入，持续 200 tick，期间激活不可见的冰霜屏障
 *   （半径 4，推开范围内所有实体），并将自身攻击速度降低 50%；随后进入 300 tick 的屏障冷却。
 *
 * 数值：max_health=20 / movement_speed=0.23 / attack_damage=5 / attack_speed=4 / follow_range=24。
 * 寒冰弹无重力、速度 1.2、命中造成减速与击退。
 */
public class FrostGuardianV2Entity extends Monster {
    public enum GuardianMode { ATTACK, DEFENSE }

    private static final double DEFENSE_TRIGGER_RANGE = 8.0;
    private static final int DEFENSE_DURATION_TICKS = 200;
    private static final int DEFENSE_COOLDOWN_TICKS = 300;
    private static final int BOLT_COOLDOWN_TICKS = 40;
    private static final float BOLT_SPEED = 1.2F;
    // 射击距离：进入该范围内才停下放弹；超出则追击目标，保持远程但不站桩。
    private static final double FIRE_RANGE = 14.0;
    private static final double MAX_FIRE_RANGE = 24.0;
    private static final ResourceLocation DEFENSE_ATTACK_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "defense_attack_speed_reduction");

    // 模式作为实体同步数据，使客户端渲染能拿到当前姿态（防御姿态的双臂动画等）。
    private static final EntityDataAccessor<Integer> DATA_MODE =
        SynchedEntityData.defineId(FrostGuardianV2Entity.class, EntityDataSerializers.INT);

    private int modeTimer = 0;
    private int defenseCooldown = 0;
    private int boltCooldown = 0;

    public FrostGuardianV2Entity(EntityType<? extends FrostGuardianV2Entity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.ATTACK_SPEED, 4.0)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODE, GuardianMode.ATTACK.ordinal());
    }

    public GuardianMode getGuardianMode() {
        return this.getMode();
    }

    private GuardianMode getMode() {
        int id = this.entityData.get(DATA_MODE);
        return GuardianMode.values()[Mth.clamp(id, 0, GuardianMode.values().length - 1)];
    }

    private void setMode(GuardianMode newMode) {
        this.entityData.set(DATA_MODE, newMode.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FrostBoltAttackGoal());
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 寒冰弹攻击：蓄力 40 tick，然后朝目标抛射寒冰弹。防御模式下冷却翻倍（攻速减半）。 */
    private class FrostBoltAttackGoal extends net.minecraft.world.entity.ai.goal.Goal {
        FrostBoltAttackGoal() {
            this.setFlags(EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = FrostGuardianV2Entity.this.getTarget();
            return target != null && target.isAlive()
                && FrostGuardianV2Entity.this.distanceToSqr(target) <= MAX_FIRE_RANGE * MAX_FIRE_RANGE;
        }

        @Override
        public void start() {
            FrostGuardianV2Entity.this.boltCooldown = 10;
        }

        @Override
        public void tick() {
            LivingEntity target = FrostGuardianV2Entity.this.getTarget();
            if (target == null) {
                return;
            }
            FrostGuardianV2Entity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = FrostGuardianV2Entity.this.distanceToSqr(target);
            if (distSqr <= FIRE_RANGE * FIRE_RANGE) {
                // 进入射程：停下放弹
                FrostGuardianV2Entity.this.navigation.stop();
                FrostGuardianV2Entity.this.boltCooldown--;
                if (FrostGuardianV2Entity.this.boltCooldown <= 0) {
                    FrostGuardianV2Entity.this.shootFrostBolt(target);
                    // 防御模式下攻速减半：寒冰弹冷却翻倍。
                    boolean defense = FrostGuardianV2Entity.this.getMode() == GuardianMode.DEFENSE;
                    FrostGuardianV2Entity.this.boltCooldown = BOLT_COOLDOWN_TICKS * (defense ? 2 : 1);
                }
            } else {
                // 超出射程：继续追击目标（保持远程，不站桩）
                FrostGuardianV2Entity.this.navigation.moveTo(target, 1.0);
                // 追击途中重置冷却，重进射程后能立刻开炮
                FrostGuardianV2Entity.this.boltCooldown = 10;
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    /** 向目标发射寒冰弹。 */
    private void shootFrostBolt(LivingEntity target) {
        Level world = this.level();
        if (world.isClientSide()) {
            return;
        }
        FrostBoltV2Projectile bolt = new FrostBoltV2Projectile(world, this);
        Vec3 start = this.getEyePosition();
        bolt.moveTo(start.x, start.y - 0.1, start.z, this.getYRot(), this.getXRot());

        double dx = target.getX() - start.x;
        double dy = target.getY(0.5) - start.y;
        double dz = target.getZ() - start.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // 无重力直线弹：不做抛物线补偿(禁止 +0.1)。
        // 加入目标移动提前量预判，避免直线弹打不中走动中的目标。
        double travelTime = dist / BOLT_SPEED;
        Vec3 vel = target.getDeltaMovement();
        double predictX = target.getX() + vel.x * travelTime;
        double predictY = target.getY(0.5);
        double predictZ = target.getZ() + vel.z * travelTime;
        double mx = predictX - start.x;
        double my = predictY - start.y;
        double mz = predictZ - start.z;
        double mdist = Math.sqrt(mx * mx + my * my + mz * mz);

        bolt.shoot(mx / mdist, my / mdist, mz / mdist, BOLT_SPEED, 0.0F);
        bolt.setDamage((float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        world.addFreshEntity(bolt);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        this.updateModeMachine();
    }

    /** 防御/攻击状态机：目标在 8 格内触发防御，持续后回到攻击并进入冷却。 */
    private void updateModeMachine() {
        if (this.defenseCooldown > 0) {
            this.defenseCooldown--;
        }

        switch (this.getMode()) {
            case ATTACK -> {
                if (this.defenseCooldown == 0 && this.isAnyEnemyWithin(DEFENSE_TRIGGER_RANGE)) {
                    this.switchMode(GuardianMode.DEFENSE);
                }
            }
            case DEFENSE -> {
                this.modeTimer--;
                if (this.modeTimer <= 0) {
                    this.switchMode(GuardianMode.ATTACK);
                    this.defenseCooldown = DEFENSE_COOLDOWN_TICKS;
                }
            }
        }
    }

    private void switchMode(GuardianMode newMode) {
        if (this.getMode() == newMode) {
            return;
        }
        this.setMode(newMode);
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getMode() == GuardianMode.DEFENSE) {
            this.modeTimer = DEFENSE_DURATION_TICKS;
            // 防御模式：激活不可见冰霜屏障（推开范围内所有实体）
            this.addEffect(new MobEffectInstance(ModEffects.FROST_BARRIER,
                DEFENSE_DURATION_TICKS, 0, false, false, true));
            AttributeInstance attackSpeed = this.getAttribute(Attributes.ATTACK_SPEED);
            if (attackSpeed != null && !attackSpeed.hasModifier(DEFENSE_ATTACK_SPEED_ID)) {
                attackSpeed.addTransientModifier(new AttributeModifier(
                    DEFENSE_ATTACK_SPEED_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.FROST_GUARDIAN_V2_MODE_SWITCH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0, this.getZ(),
                    30, 3.0, 1.0, 3.0, 0.0);
            }
        } else {
            this.removeEffect(ModEffects.FROST_BARRIER);
            AttributeInstance attackSpeed = this.getAttribute(Attributes.ATTACK_SPEED);
            if (attackSpeed != null) {
                attackSpeed.removeModifier(DEFENSE_ATTACK_SPEED_ID);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.FROST_GUARDIAN_V2_MODE_SWITCH.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
            }
        }
    }

    private boolean isAnyEnemyWithin(double range) {
        // 仅在"正在攻击"时才会开启防御：必须有已锁定的存活目标且目标在范围内。
        // 空闲（无目标）时不触发，避免路过/贴近的其他实体把守卫吓进防御模式。
        LivingEntity target = this.getTarget();
        return target != null && target.isAlive() && this.distanceToSqr(target) <= range * range;
    }

    // ---- 音效 ----
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FROST_GUARDIAN_V2_AMBIENT.get() ;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.FROST_GUARDIAN_V2_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FROST_GUARDIAN_V2_DEATH.get();
    }

    // ---- 掉落：由战利品表处理，这里不额外调用 dropItem ----

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("GuardianMode", this.getMode().name());
        compound.putInt("ModeTimer", this.modeTimer);
        compound.putInt("DefenseCooldown", this.defenseCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("GuardianMode")) {
            try {
                this.setMode(GuardianMode.valueOf(compound.getString("GuardianMode")));
            } catch (IllegalArgumentException ignored) {
                this.setMode(GuardianMode.ATTACK);
            }
        }
        this.modeTimer = compound.getInt("ModeTimer");
        this.defenseCooldown = compound.getInt("DefenseCooldown");
    }

    public boolean isDefenseMode() {
        return this.getMode() == GuardianMode.DEFENSE;
    }
}