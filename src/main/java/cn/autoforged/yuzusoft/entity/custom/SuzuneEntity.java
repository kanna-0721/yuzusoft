package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.block.ModBlocks;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Suzune（凉音）—— 自爆人形敌对生物。
 *
 * 行为状态机：
 * 1. 游荡：随机游荡，播放空闲语音；看到玩家（16 格 + 视线）时改播愤怒语音。
 * 2. 追击自爆：锁定玩家后靠近，进入苦力怕式蓄力（闪光 + 膨胀），30 tick 后爆炸（半径 3，MOB 交互）。
 * 3. 钻地：10 秒未锁定目标时随机钻入一个「上方为空气」的实心方块内，
 *    并在该方块正上方安放警报器；埋地时不可见、无碰撞、免疫窒息。
 * 4. 破坏警报器钻出：只有警报器被破坏（或被替换为其他方块）时才会钻出并追击玩家自爆；
 *    受伤也会钻出。不再监听玩家发出的声音振动。
 */
public class SuzuneEntity extends Monster {
    private static final int MAX_SWELL = 30;
    private static final float EXPLOSION_RADIUS = 8.0F;
    private static final int SWELL_ATTACK_DISTANCE_SQR = 9; // 3 格内开始蓄力
    private static final int SWELL_ABANDON_DISTANCE_SQR = 49; // 7 格外放弃蓄力
    private static final int DIG_COOLDOWN_TICKS = 200; // 30 秒
    private static final double VISIBLE_PLAYER_RANGE = 16.0;
    /** 抗性 ≥ 此值的方块禁止钻入（黑曜石 1200 / 末地石 45 / 铁砧 1200；石头 6 仍可钻）。 */
    private static final float MAX_DIG_EXPLOSION_RESISTANCE = 30.0F;
    /** 埋地时通过 EntityEvent.Size 事件缩小到极小碰撞箱。 */
    public static final EntityDimensions HIDDEN_DIMENSIONS = EntityDimensions.scalable(0.05F, 0.05F);

    // -1 = 空闲，1 = 蓄力中（与苦力怕一致的同步数据）
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
        SynchedEntityData.defineId(SuzuneEntity.class, EntityDataSerializers.INT);
    // 是否已钻地（客户端据此切换碰撞箱尺寸）
    private static final EntityDataAccessor<Boolean> DATA_HIDDEN =
        SynchedEntityData.defineId(SuzuneEntity.class, EntityDataSerializers.BOOLEAN);

    private int oldSwell;
    private int swell;

    @Nullable
    private BlockPos hiddenBlockPos;
    @Nullable
    private BlockPos alarmPos;
    private long lastTargetTime;

    public SuzuneEntity(EntityType<? extends SuzuneEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
        this.lastTargetTime = level.getGameTime();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
        builder.define(DATA_HIDDEN, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SuzuneSwellGoal());
        // 靠近目标（接近到 3 格内由 SuzuneSwellGoal 接管自爆）
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // 埋地期间禁止主动锁定玩家（只能通过破坏警报器/受伤钻出后再锁定）
        // 过滤旁观者与创造模式玩家：原版 NearestAttackableTargetGoal 不区分模式，创造玩家也会被锁定
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true,
            player -> !(player instanceof Player p) || (!p.isSpectator() && !p.getAbilities().instabuild)) {
            @Override
            public boolean canUse() {
                return !SuzuneEntity.this.isHidden() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !SuzuneEntity.this.isHidden() && super.canContinueToUse();
            }
        });
    }

    // ---- 苦力怕式自爆 ----

    public boolean isSwelling() {
        return this.getSwellDir() > 0;
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int state) {
        this.entityData.set(DATA_SWELL_DIR, state);
    }

    /** 供渲染器使用的蓄力进度（0..1），与苦力怕完全一致。 */
    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, (float) this.oldSwell, (float) this.swell) / (float) (MAX_SWELL - 2);
    }

    private int getSwell() {
        return this.swell;
    }

    private void setSwell(int i) {
        this.swell = i;
    }

    /** 蓄力 AI：目标进入 3 格内开始蓄力；目标消失/离远/不可见则取消。 */
    private class SuzuneSwellGoal extends Goal {
        SuzuneSwellGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = SuzuneEntity.this.getTarget();
            return SuzuneEntity.this.getSwellDir() > 0
                || (target != null && SuzuneEntity.this.distanceToSqr(target) < (double) SWELL_ATTACK_DISTANCE_SQR);
        }

        @Override
        public void start() {
            SuzuneEntity.this.getNavigation().stop();
            SuzuneEntity.this.setSwellDir(1);
        }

        @Override
        public void stop() {
            SuzuneEntity.this.setSwellDir(-1);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = SuzuneEntity.this.getTarget();
            if (target == null) {
                SuzuneEntity.this.setSwellDir(-1);
            } else if (SuzuneEntity.this.distanceToSqr(target) > (double) SWELL_ABANDON_DISTANCE_SQR
                || !SuzuneEntity.this.getSensing().hasLineOfSight(target)) {
                SuzuneEntity.this.setSwellDir(-1);
            } else {
                SuzuneEntity.this.setSwellDir(1);
            }
        }
    }

    private void explode() {
        if (this.level().isClientSide) {
            return;
        }
        this.dead = true;
        this.level().explode(this, this.getX(), this.getY() + this.getEyeHeight() / 2.0, this.getZ(),
            EXPLOSION_RADIUS, Level.ExplosionInteraction.MOB);
        this.discard();
    }

    /** 与苦力怕一致：玩家用打火石右键立即点燃（进入蓄力），并消耗 1 点打火石耐久。 */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 埋地期间不可被点燃（不可见、无碰撞）
        if (!this.isHidden() && stack.is(Items.FLINT_AND_STEEL)) {
            this.setSwellDir(1);
            this.level().playSound(player, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
            if (!this.level().isClientSide) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** 纯自爆生物：不造成近战伤害（与苦力怕一致）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        return true;
    }

    /** 仅在钻出（非埋地）状态被玩家击败时，固定掉落 1 个警报器。 */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean lastDamageByPlayer) {
        super.dropCustomDeathLoot(level, source, lastDamageByPlayer);
        if (lastDamageByPlayer && !this.isHidden()) {
            this.spawnAtLocation(ModItems.SUZUNE_ALARM_ITEM.get());
        }
    }

    // ---- 钻地 / 钻出 ----

    public boolean isHidden() {
        return this.entityData.get(DATA_HIDDEN);
    }

    private void setHidden(boolean hidden) {
        this.entityData.set(DATA_HIDDEN, hidden);
    }

    /** 30 秒无目标时钻入当前脚下的方块，并在原位安放警报器（不随机）。 */
    private void tryDigIntoBlock() {
        if (this.level().isClientSide || this.isHidden() || this.isRemoved()) {
            return;
        }
        BlockPos digPos = this.getOnPos();
        if (isValidDigTarget(digPos)) {
            this.digInto(digPos);
        }
    }

    private boolean isValidDigTarget(BlockPos pos) {
        if (pos.getY() <= this.level().getMinBuildHeight() + 1) {
            return false;
        }
        BlockState state = this.level().getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.getCollisionShape(this.level(), pos).isEmpty()) {
            return false;
        }
        if (this.level().getBlockEntity(pos) != null) {
            return false;
        }
        // 禁止钻入黑曜石等高爆炸抗性方块：爆炸也炸不坏它们，避免警报器藏在无敌方块上
        if (state.getBlock().getExplosionResistance() >= MAX_DIG_EXPLOSION_RESISTANCE) {
            return false;
        }
        return this.level().getBlockState(pos.above()).isAir() && this.level().getBlockState(pos.above().above()).isAir();
    }

    private void digInto(BlockPos blockPos) {
        this.setHidden(true);
        this.setInvisible(true);
        this.setNoGravity(true);
        this.getNavigation().stop();
        this.setTarget(null);
        this.setSwell(0);
        this.setSwellDir(-1);
        this.setDeltaMovement(Vec3.ZERO);
        this.hiddenBlockPos = blockPos;
        this.alarmPos = blockPos; // 警报器位置 = 钻入位置（原位安放）
        this.moveTo(blockPos.getX() + 0.5, blockPos.getY() + 0.02, blockPos.getZ() + 0.5, this.getYRot(), this.getXRot());
        this.refreshDimensions();
        BlockState dugState = this.level().getBlockState(blockPos);
        // 钻入方块原位被警报器替换，警报器与钻入位置相同
        this.level().setBlock(blockPos, ModBlocks.SUZUNE_ALARM.get().defaultBlockState(), 3);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SILVERFISH_HURT, SoundSource.HOSTILE, 1.0F, 1.0F);
            // 监守者同款粒子：钻入方块碎块迸射
            if (dugState.getRenderShape() != RenderShape.INVISIBLE) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, dugState),
                    blockPos.getX() + 0.5, blockPos.getY() + 0.02, blockPos.getZ() + 0.5,
                    30, 0.7, 0.7, 0.7, 0.0);
            }
        }
    }

    private void emergeFromBlock() {
        if (this.level().isClientSide || !this.isHidden()) {
            return;
        }
        this.setHidden(false);
        this.setInvisible(false);
        this.setNoGravity(false);
        this.setDeltaMovement(Vec3.ZERO);
        // 钻出后重置 30 秒无目标计时：不会立刻再次钻入，先尝试追击玩家
        this.lastTargetTime = this.level().getGameTime();
        BlockPos emergePos = this.alarmPos != null ? this.alarmPos
            : (this.hiddenBlockPos != null ? this.hiddenBlockPos : this.blockPosition());
        if (this.alarmPos != null && this.level().getBlockState(this.alarmPos).is(ModBlocks.SUZUNE_ALARM.get())) {
            this.level().setBlock(this.alarmPos, Blocks.AIR.defaultBlockState(), 3);
        }
        // 钻出位置 = 警报器位置（与钻入位置相同）
        this.moveTo(emergePos.getX() + 0.5, emergePos.getY(), emergePos.getZ() + 0.5, this.getYRot(), this.getXRot());
        this.refreshDimensions();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SILVERFISH_STEP, SoundSource.HOSTILE, 1.0F, 1.2F);
            // 监守者同款粒子：脚下地面方块碎块迸射
            BlockState groundState = this.level().getBlockState(emergePos.below());
            if (groundState.getRenderShape() != RenderShape.INVISIBLE) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                    emergePos.getX() + 0.5, emergePos.getY() + 0.02, emergePos.getZ() + 0.5,
                    30, 0.7, 0.7, 0.7, 0.0);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        // 埋地时开启无物理：免疫方块碰撞与窒息，位置由服务端钉住
        this.noPhysics = this.isHidden();
        // 蓄力倒计时（客户端与服务端通过同步的 swellDir 各自计数，与苦力怕一致）
        // 与苦力怕相同：只有存活时才累加，死亡后不再继续蓄力，防止被击败后仍爆炸
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int i = this.getSwellDir();
            if (i > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
                this.gameEvent(GameEvent.PRIME_FUSE);
            }
            this.swell += i;
            if (this.swell < 0) {
                this.swell = 0;
            }
            if (this.swell >= MAX_SWELL) {
                this.swell = MAX_SWELL;
                this.explode();
                return;
            }
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.isHidden()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (this.hiddenBlockPos != null) {
                this.setPos(this.hiddenBlockPos.getX() + 0.5, this.hiddenBlockPos.getY() + 0.02, this.hiddenBlockPos.getZ() + 0.5);
                if (this.level().getBlockState(this.hiddenBlockPos).isAir()) {
                    // 所在方块变空气：立马钻出并锁定附近玩家追击自爆
                    this.emergeFromBlock();
                    this.lockNearestPlayer();
                } else if (this.alarmPos != null && !this.level().getBlockState(this.alarmPos).is(ModBlocks.SUZUNE_ALARM.get())) {
                    // 警报器被破坏/替换：立马钻出并锁定附近玩家追击自爆
                    this.emergeFromBlock();
                    this.lockNearestPlayer();
                }
            }
        } else {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.lastTargetTime = this.level().getGameTime();
            } else if (this.getSwellDir() < 0 && this.level().getGameTime() - this.lastTargetTime >= DIG_COOLDOWN_TICKS) {
                this.tryDigIntoBlock();
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive() && this.isHidden()) {
            this.getNavigation().stop();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && this.isHidden()) {
            this.emergeFromBlock();
            if (source.getEntity() instanceof LivingEntity attacker) {
                this.setTarget(attacker);
            }
        }
        return hurt;
    }

    // ---- 埋地相关覆写 ----

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_HIDDEN.equals(key)) {
            // 客户端随同步数据刷新碰撞箱尺寸
            this.refreshDimensions();
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isHidden() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    // ---- 音效 ----

    private boolean hasVisiblePlayer() {
        Player closest = this.level().getNearestPlayer(this, VISIBLE_PLAYER_RANGE);
        // 过滤旁观者与创造模式玩家，避免对非战斗玩家播放愤怒音效
        return closest != null && !closest.isSpectator() && !closest.getAbilities().instabuild
            && this.getSensing().hasLineOfSight(closest);
    }

    /** 钻出后主动锁定附近玩家（忽略视野要求），确保起身即追击自爆。 */
    private void lockNearestPlayer() {
        Player nearest = this.level().getNearestPlayer(this, VISIBLE_PLAYER_RANGE);
        if (nearest != null && !nearest.isSpectator() && !nearest.getAbilities().instabuild) {
            this.setTarget(nearest);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasVisiblePlayer() ? ModSounds.SUZUNE_ANGRY.get() : ModSounds.SUZUNE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SUZUNE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SUZUNE_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 60;
    }

    // ---- NBT ----

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Hidden", this.isHidden());
        if (this.hiddenBlockPos != null) {
            compound.putInt("HiddenBlockX", this.hiddenBlockPos.getX());
            compound.putInt("HiddenBlockY", this.hiddenBlockPos.getY());
            compound.putInt("HiddenBlockZ", this.hiddenBlockPos.getZ());
        }
        if (this.alarmPos != null) {
            compound.putInt("AlarmX", this.alarmPos.getX());
            compound.putInt("AlarmY", this.alarmPos.getY());
            compound.putInt("AlarmZ", this.alarmPos.getZ());
        }
        compound.putLong("LastTargetTime", this.lastTargetTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lastTargetTime = compound.getLong("LastTargetTime");
        if (compound.contains("HiddenBlockX")) {
            this.hiddenBlockPos = new BlockPos(
                compound.getInt("HiddenBlockX"), compound.getInt("HiddenBlockY"), compound.getInt("HiddenBlockZ"));
        }
        if (compound.contains("AlarmX")) {
            this.alarmPos = new BlockPos(
                compound.getInt("AlarmX"), compound.getInt("AlarmY"), compound.getInt("AlarmZ"));
        }
        boolean hidden = compound.getBoolean("Hidden");
        this.setHidden(hidden);
        this.setInvisible(hidden);
        this.setNoGravity(hidden);
        if (hidden) {
            this.setSwell(0);
            this.setSwellDir(-1);
            this.refreshDimensions();
        }
    }
}
