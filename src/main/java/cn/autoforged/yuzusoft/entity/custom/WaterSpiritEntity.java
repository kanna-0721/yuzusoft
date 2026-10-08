package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.ai.AcquireAngerTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.WaterSpiritMeleeGoal;
import cn.autoforged.yuzusoft.entity.ai.WaterSpiritShootGoal;
import cn.autoforged.yuzusoft.event.ProjectileReflectHandler;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

/**
 * 人形水灵：中立水生生物。
 * - 头顶 5 格水位条（名称牌实现，随水位实时变化）；
 * - 空桶取水扣 1.0、玻璃瓶取水扣 0.1，水位不足时拒绝并使水灵愤怒 10 秒；
 * - 空闲每 60 秒（愤怒 30 秒）扫描 25x5x25 内最近的水源方块并吸收（+1 水位，水源消失）；
 * - 愤怒后每 1.5 秒消耗 0.2 水位发射水弹；水位不足 0.2 时改为近战；
 * - 免疫火焰与溺水伤害，水中按陆地物理移动（无阻力，可自由行走）；
 * - 来袭弹射物自动镜像反弹（见 ProjectileReflectHandler）；
 * - 死亡 20% 掉落一把随机耐久的雨伞。
 */
public class WaterSpiritEntity extends PathfinderMob {
    private static final EntityDataAccessor<Float> DATA_WATER_LEVEL =
            SynchedEntityData.defineId(WaterSpiritEntity.class, EntityDataSerializers.FLOAT);

    /** 距离下一次水源吸收扫描的 tick 数 */
    private int absorbCooldown = 60;
    /** 愤怒到期时的游戏时间 */
    private long angerUntilGameTime = Long.MIN_VALUE;
    /** 愤怒音效节流 */
    private int angrySoundCooldown = 0;

    public WaterSpiritEntity(EntityType<? extends WaterSpiritEntity> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.WATER_SPIRIT_UMBRELLA.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setPathfindingMalus(PathType.WATER, 0.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 生成时满水（历史上限 50%，导致生成后短暂射击就切近战）
        builder.define(DATA_WATER_LEVEL, WaterSpiritConfig.maxWater());
    }

    // ---------------- 水位 ----------------

    public float getWaterLevel() {
        return this.entityData.get(DATA_WATER_LEVEL);
    }

    public void setWaterLevel(float value) {
        float max = WaterSpiritConfig.maxWater();
        float clamped = Math.max(0.0F, Math.min(max, value));
        this.entityData.set(DATA_WATER_LEVEL, clamped);
    }

    public boolean atFullWater() {
        return getWaterLevel() >= WaterSpiritConfig.maxWater() - 1.0E-3F;
    }

    public boolean canShootMore() {
        return WaterSpiritConfig.hasEnoughWater(getWaterLevel(), WaterSpiritConfig.SHOT_WATER_COST.get().doubleValue());
    }

    // ---------------- 愤怒 ----------------

    public boolean isAngry() {
        return this.level().getGameTime() < this.angerUntilGameTime;
    }

    public void getAngryAt(@Nullable LivingEntity attacker) {
        if (this.level().isClientSide) {
            return;
        }
        this.angerUntilGameTime = this.level().getGameTime()
                + WaterSpiritConfig.secondsToTicks(WaterSpiritConfig.ANGER_DURATION_SECONDS.get().doubleValue());
        if (attacker != null && attacker.isAlive()) {
            this.setTarget(attacker);
        }
        if (this.angrySoundCooldown <= 0) {
            this.angrySoundCooldown = 40;
            this.level().playSound(null, this, ModSounds.WATER_SPIRIT_ANGRY.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 免疫火焰与溺水
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            Entity causing = source.getEntity();
            if (causing instanceof LivingEntity living && living != this && !(living instanceof WaterSpiritEntity)) {
                getAngryAt(living);
            } else if (!isAngry()) {
                // 环境伤害：进入愤怒状态（会主动寻找最近的玩家作为目标）
                getAngryAt(null);
            }
        }
        return hurt;
    }

    // ---------------- 取水交互（由 SpiritInteractionHandler 事件调用） ----------------

    /**
     * 尝试从水灵身上取水。返回 null 表示未处理（不是桶/瓶）。
     * 成功：扣水位、给水桶/水瓶、播放取水音；失败：仅播放提示音（不触发愤怒）。
     */
    @Nullable
    public InteractionResult tryTakeWater(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean bucket = held.is(Items.BUCKET);
        boolean bottle = held.is(Items.GLASS_BOTTLE);
        if (!bucket && !bottle) {
            return null;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS; // 服务端结算并同步水位
        }
        double cost = bucket ? WaterSpiritConfig.BUCKET_COST.get() : WaterSpiritConfig.BOTTLE_COST.get();
        float wl = getWaterLevel();
        // 桶：水位小于 1 禁桶；瓶：水位归 0 禁瓶（不足一次用量时按剩余扣光）
        boolean allowed = bucket ? WaterSpiritConfig.hasEnoughWater(wl, cost) : wl > 1.0E-4F;
        if (allowed) {
            setWaterLevel((float) Math.max(0.0F, wl - cost));
            ItemStack result = bucket ? new ItemStack(Items.WATER_BUCKET)
                    : PotionContents.createItemStack(Items.POTION, Potions.WATER);
            if (!player.isCreative()) {
                held.shrink(1);
                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }
            }
            this.level().playSound(null, this, ModSounds.WATER_SPIRIT_TAKE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        this.getX(), this.getEyeY(), this.getZ(), 12, 0.2, 0.2, 0.2, 0.1);
            }
            return InteractionResult.SUCCESS;
        } else {
            this.level().playSound(null, this, ModSounds.WATER_SPIRIT_DENY.get(), SoundSource.NEUTRAL, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }
    }

    // ---------------- 水源吸收 ----------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.angrySoundCooldown > 0) {
            this.angrySoundCooldown--;
        }
        LivingEntity target = this.getTarget();
        double angerRange = WaterSpiritConfig.SHOT_MAX_RANGE.get();
        if (target != null && target.isAlive() && target.level() == this.level()
                && this.distanceToSqr(target) <= angerRange * angerRange
                && this.hasLineOfSight(target)) {
            // 射击范围内且看得见目标（芳乃式）：每 tick 续满愤怒，视线内一直保持愤怒
            this.angerUntilGameTime = this.level().getGameTime()
                    + WaterSpiritConfig.secondsToTicks(WaterSpiritConfig.ANGER_DURATION_SECONDS.get().doubleValue());
        } else {
            if (target != null) {
                setTarget(null);
            }
            // 无目标/超范围/不可见：愤怒计时自然倒计时，到点彻底消怒（消除窗口期重锁）
            if (this.level().getGameTime() >= this.angerUntilGameTime) {
                this.angerUntilGameTime = 0L;
            }
        }
        if (this.absorbCooldown > 0) {
            this.absorbCooldown--;
        }
        if (this.absorbCooldown <= 0) {
            if (atFullWater()) {
                // 水位已满时不吸收，继续等待
                this.absorbCooldown = WaterSpiritConfig.secondsToTicks(
                        WaterSpiritConfig.ABSORB_IDLE_COOLDOWN_SECONDS.get().doubleValue());
            } else {
                tryAbsorbNearbyWater();
                double cd = isAngry() ? WaterSpiritConfig.ABSORB_ANGRY_COOLDOWN_SECONDS.get()
                        : WaterSpiritConfig.ABSORB_IDLE_COOLDOWN_SECONDS.get();
                this.absorbCooldown = WaterSpiritConfig.secondsToTicks(cd);
            }
        }
    }

    private void tryAbsorbNearbyWater() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos center = this.blockPosition();
        int h = WaterSpiritConfig.ABSORB_RANGE_HORIZONTAL.get();
        int v = WaterSpiritConfig.ABSORB_RANGE_VERTICAL.get();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-h, -v, -h), center.offset(h, v, h))) {
            FluidState fluid = serverLevel.getFluidState(pos);
            if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                double d = pos.distSqr(center);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
        }
        if (best != null) {
            serverLevel.setBlock(best, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            setWaterLevel(getWaterLevel() + WaterSpiritConfig.ABSORB_GAIN.get().floatValue());
            this.level().playSound(null, this, ModSounds.WATER_SPIRIT_ABSORB.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    best.getX() + 0.5, best.getY() + 0.5, best.getZ() + 0.5, 20, 0.15, 0.15, 0.15, 0.1);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    best.getX() + 0.5, best.getY() + 0.5, best.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // ---------------- 死亡掉落雨伞 ----------------

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide
                && this.random.nextDouble() < WaterSpiritConfig.UMBRELLA_DROP_CHANCE.get()) {
            ItemStack umbrella = new ItemStack(ModItems.WATER_SPIRIT_UMBRELLA.get());
            int maxDamage = Math.max(1, umbrella.getMaxDamage());
            umbrella.setDamageValue(this.random.nextInt(maxDamage)); // 随机耐久
            ItemEntity item = new ItemEntity(this.level(),
                    this.getX(), this.getY() + 0.3, this.getZ(), umbrella);
            item.setPickUpDelay(20);
            this.level().addFreshEntity(item);
        }
    }

    // ---------------- 水中移动与呼吸 ----------------

    @Override
    public void tick() {
        super.tick();
        // 全弹射物镜像反弹：就地扫描来袭弹射物（速度大小不变、方向相反）
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel
                && WaterSpiritConfig.REFLECT_PROJECTILES.get()) {
            ProjectileReflectHandler.reflectInbound(serverLevel, this, null);
        }
    }

    /** 空气永不减少 => 永不溺水（配合 hurt 的 IS_DROWNING 免疫双保险） */
    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    /** 关键：让水不产生流体阻力，按陆地物理自由行走 */
    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }

    // ---------------- 音效占位（由用户上传 ogg 替换） ----------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WATER_SPIRIT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WATER_SPIRIT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WATER_SPIRIT_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 250;
    }

    // ---------------- AI ----------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new WaterSpiritMeleeGoal(this));
        this.goalSelector.addGoal(4, new WaterSpiritShootGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new AcquireAngerTargetGoal(this));
    }

    // ---------------- 头顶水位条（名称牌） ----------------

    @Override
    public Component getDisplayName() {
        Component base = super.getDisplayName();
        if (!WaterSpiritConfig.SHOW_WATER_BAR.get()) {
            return base;
        }
        float max = Math.max(0.001F, WaterSpiritConfig.maxWater());
        int filled = Math.max(0, Math.min(5, Math.round(getWaterLevel() / max * 5.0F)));
        MutableComponent out = base.copy();
        out.append(Component.literal(" "));
        for (int i = 0; i < 5; i++) {
            out.append(Component.literal("█")
                    .withStyle(i < filled ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        }
        if (WaterSpiritConfig.SHOW_WATER_NUMBER.get()) {
            out.append(Component.literal(String.format(" %.1f/%.1f", getWaterLevel(), max))
                    .withStyle(ChatFormatting.GRAY));
        }
        return out;
    }

    // ---------------- 生成规则：水源附近 ----------------

    public static boolean checkSpawnRules(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason,
                                          BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if (below.isAir() || below.is(Blocks.BEDROCK)) {
            return false;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return false; // 直接长在水里的不刷，刷在岸/河床上方
        }
        int r = 6;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    if (level.isWaterAt(pos.offset(dx, dy, dz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ---------------- NBT ----------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("WaterSpiritLevel", getWaterLevel());
        tag.putInt("WaterSpiritAbsorbCooldown", this.absorbCooldown);
        long remain = this.angerUntilGameTime - this.level().getGameTime();
        tag.putLong("WaterSpiritAngerTicks", Math.max(0L, remain));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WaterSpiritLevel")) {
            setWaterLevel(tag.getFloat("WaterSpiritLevel"));
        }
        this.absorbCooldown = tag.getInt("WaterSpiritAbsorbCooldown");
        this.angerUntilGameTime = this.level().getGameTime() + tag.getLong("WaterSpiritAngerTicks");
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
        this.absorbCooldown = 40 + this.random.nextInt(80);
        return data;
    }
}
