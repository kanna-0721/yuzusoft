package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.LimelightHelper;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import cn.autoforged.yuzusoft.util.NoteUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * 岛越月望——手持三角钢琴主动追击玩家的敌对生物。
 *
 * <p>只有在锁定战斗目标（主动锁定玩家与铁傀儡，被谁打就还手锁定谁）、且目标进入外圈
 * （以自身为中心的 13x13x13 方块）时才奏琴攻击，
 * 每 2 秒一次，空闲时不出手：内圈 {@value #INNER_RADIUS} 格以内（即 7x7x7 方块）造成
 * {@value #INNER_DAMAGE} 点伤害，外圈环带（13x13x13 减去内圈）造成 {@value #OUTER_DAMAGE} 点，
 * 内圈不重复计算。范围按方块整数距离（切比雪夫）判定，无视墙与遮挡——琴声是领域效果。
 * 不伤害自己与 limelight 同族。</p>
 *
 * <p>移动上「够得着就不走」：目标进入外圈后停止寻路、只转身面向目标；目标退出外圈才重新
 * 追上去。因此她没有近战，见 {@link PianoApproachGoal}。</p>
 *
 * <p>并入自独立模组 {@code PianoNeoForge}。</p>
 */
public class ShimagoeTsukumi extends Monster {

    /** 钢琴掉落表：2% 基础概率 + 抢夺每级 1%。 */
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entities/shimagoe_tsukumi"));

    /** 内圈半径（方块）：7x7x7 => ±3。 */
    private static final int INNER_RADIUS = 6;
    /** 外圈半径（方块）：13x13x13 => ±6。 */
    private static final int OUTER_RADIUS = 12;
    private static final float INNER_DAMAGE = 6.0F;
    private static final float OUTER_DAMAGE = 4.0F;

    /** 琴声领域附带的眩晕时长：1 秒。 */
    private static final int STUN_TICKS = 20;
    private static final int CAST_INTERVAL_TICKS = 40;

    private int castCooldown;

    public ShimagoeTsukumi(EntityType<? extends ShimagoeTsukumi> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GRAND_PIANO.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PianoApproachGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this, LimelightHelper::isLimelight));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new GroupSupportTargetGoal(this, LimelightHelper::isLimelight));
    }

    @Override
    protected void customServerAiStep() {
        if (this.castCooldown > 0) {
            this.castCooldown--;
        } else if (shouldStrike()) {
            playPianoStrike();
            this.castCooldown = CAST_INTERVAL_TICKS;
        }
    }

    /** 只在已经锁定玩家/铁傀儡、且目标进了外圈时才奏琴；没有目标就完全不攻击。 */
    private boolean shouldStrike() {
        return hasStrikeTarget() && isWithinOuterCube(this.getTarget());
    }

    /**
     * 有活着的战斗目标就算「察觉到了敌人」。目标种类的限制交给 targetSelector：
     * 主动锁定玩家与铁傀儡，被谁打就还手锁定谁（HurtByTargetGoal）。
     */
    private boolean hasStrikeTarget() {
        LivingEntity target = this.getTarget();
        return target != null && target.isAlive();
    }

    private boolean isWithinOuterCube(LivingEntity target) {
        return chebyshev(this.blockPosition(), target.blockPosition()) <= OUTER_RADIUS;
    }

    /** 判定按方块整数距离做，所以扫描盒要比外圈宽一格，避免块内不同位置漏筛。 */
    private AABB strikeScanBox() {
        return this.getBoundingBox().inflate(OUTER_RADIUS + 1.0D);
    }

    private void playPianoStrike() {
        Level level = this.level();
        this.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NOTE_BLOCK_HARP.value(),
                SoundSource.HOSTILE, 2.0F, NoteUtil.pitchFromId(this.random.nextInt(NoteUtil.KEY_COUNT)));

        BlockPos center = this.blockPosition();
        List<Entity> targets = level.getEntities(this, strikeScanBox(), this::canBeHitByPiano);
        for (Entity target : targets) {
            int distance = chebyshev(center, target.blockPosition());
            float damage;
            if (distance <= INNER_RADIUS) {
                damage = INNER_DAMAGE;
            } else if (distance <= OUTER_RADIUS) {
                damage = OUTER_DAMAGE;
            } else {
                continue;
            }
            target.hurt(level.damageSources().mobAttack(this), damage);
            // 琴声领域附带 1 秒眩晕
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_TICKS, 0));
            }
        }
    }

    /** 玩家与其他生物都吃琴声；自己、limelight 同族、旁观者与掉落物不吃。 */
    private boolean canBeHitByPiano(Entity entity) {
        if (entity == this || !entity.isAlive() || !(entity instanceof LivingEntity living)) {
            return false;
        }
        if (LimelightHelper.isLimelight(living)) {
            return false;
        }
        return !(entity instanceof Player player && player.isSpectator());
    }

    private static int chebyshev(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GRAND_PIANO.get()));
        // 钢琴只从 LOOT_TABLE 出，别让原版装备掉落概率再掉一次
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return LOOT_TABLE;
    }

    /** 空闲低语：一旦锁定战斗目标就静音，改由愤怒音效表达状态。 */
    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getTarget() == null ? ModSounds.SHIMAGOE_IDLE.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHIMAGOE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHIMAGOE_DEATH.get();
    }

    /** 从「没有目标」变成「有目标」的那一 tick 叫一声，换目标不重复叫。 */
    @Override
    public void setTarget(LivingEntity target) {
        boolean wasIdle = this.getTarget() == null;
        super.setTarget(target);
        if (wasIdle && target != null) {
            this.makeSound(ModSounds.SHIMAGOE_ANGRY.get());
        }
    }

    /**
     * 追击与站桩二选一：目标一进外圈（琴声够得着）就停住脚步、只盯着目标，
     * 目标退出外圈才重新寻路靠近。它取代了原版 {@code MeleeAttackGoal}，
     * 所以这只生物不贴脸近战，伤害全部来自琴声。
     */
    private static class PianoApproachGoal extends Goal {
        private static final int PATH_RECALC_TICKS = 10;

        private final ShimagoeTsukumi mob;
        private int ticksUntilNextPathRecalculation;

        PianoApproachGoal(ShimagoeTsukumi mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.hasStrikeTarget();
        }

        @Override
        public void start() {
            this.ticksUntilNextPathRecalculation = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }
            if (this.mob.isWithinOuterCube(target)) {
                this.mob.getNavigation().stop();
            } else if (--this.ticksUntilNextPathRecalculation <= 0) {
                this.ticksUntilNextPathRecalculation = PATH_RECALC_TICKS;
                this.mob.getNavigation().moveTo(target, 1.0D);
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}