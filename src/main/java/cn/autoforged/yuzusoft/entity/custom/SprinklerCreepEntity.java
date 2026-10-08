package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.ai.MinerBreakBlockGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.function.Predicate;

public class SprinklerCreepEntity extends Raider implements RangedAttackMob {
    /** 是否矿工形态：生成时随机，50% 概率。矿工主手拿随机耐久的下界合金镐、能透墙索敌并同速破坏方块。 */
    private boolean minerForm;

    public SprinklerCreepEntity(EntityType<? extends SprinklerCreepEntity> entityType, Level level) {
        super(entityType, level);
        // 两种形态生成概率相同
        this.minerForm = this.random.nextFloat() < 0.5F;
        ItemStack held = this.minerForm
                ? new ItemStack(Items.NETHERITE_PICKAXE)
                : new ItemStack(ModItems.SPRINKLER.get());
        if (this.minerForm) {
            // 耐久随机：随机扣一部分耐久，保留至少 1 点
            held.setDamageValue(this.random.nextInt(held.getMaxDamage()));
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, held);
        // 掉落完全交给战利品表（下界合金镐仅 0.1% 掉落）
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public boolean isMiner() {
        return this.minerForm;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 20);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        // 野生等自主生成的 0721 生物不允许被任何袭击征召入波（避免波次总数被地下自然刷新的生物拉高）。
        // 袭击自身 EntityType.create(Level) 生成不走 finalizeSpawn，且 spawnGroup 会显式 setCanJoinRaid(true)，不受影响。
        if (spawnType != MobSpawnType.EVENT) {
            this.setCanJoinRaid(false);
        }
        return data;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals(); // Raider：袭击旗帜/寻路/穿村/庆祝
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 普通形态：远程水球；矿工形态：近战
        this.goalSelector.addGoal(1, new FormGatedRangedAttackGoal(this, 1.0, 60, 12.0f));
        this.goalSelector.addGoal(1, new FormGatedMeleeAttackGoal(this, 1.2, true));
        // 矿工形态：卡住时同速（玩家下界合金镐）破块
        this.goalSelector.addGoal(2, new FormGatedGoal(new MinerBreakBlockGoal(this)));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // 攻击过程中召唤 0721 猫（逻辑与间隔同唤魔者 SummonVexGoal）
        this.goalSelector.addGoal(6, new SummonCatGoal(this));

        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this));
        // 普通形态必须看见目标；矿工形态透过方块索敌（mustSee = false）
        this.targetSelector.addGoal(2, new FormGatedTargetGoal<>(Player.class, true, false,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        this.targetSelector.addGoal(2, new FormGatedTargetGoal<>(Player.class, false, true,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        this.targetSelector.addGoal(3, new FormGatedTargetGoal<>(IronGolem.class, true, false));
        this.targetSelector.addGoal(3, new FormGatedTargetGoal<>(IronGolem.class, false, true));
        // 村民目标：优先级在玩家和铁傀儡之后
        this.targetSelector.addGoal(4, new FormGatedTargetGoal<>(Villager.class, true, false));
        this.targetSelector.addGoal(4, new FormGatedTargetGoal<>(Villager.class, false, true));
        // 族群支援：优先级在玩家/铁傀儡/村民之后，未锁定更高优先级目标时才前往支援
        this.targetSelector.addGoal(5, new GroupSupportTargetGoal(this));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        WaterBallEntity projectile = new WaterBallEntity(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - projectile.getY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.5f, 0f);
        this.level().addFreshEntity(projectile);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        // 矿工形态的近战攻击也能造成 0721 效果（与普通形态水球一致：300 tick / 0 级）
        if (this.minerForm && hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721, 300, 0));
        }
        return hurt;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Group0721Helper.areAllied(this, living)
                    || entity instanceof Raider // 女巫/劫掠兽等袭击者：视为盟友，不互攻、不反击
                    || super.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        // 独立于形态：50% 概率掉落不死图腾。
        if (this.random.nextFloat() < 0.5F) {
            this.spawnAtLocation(new ItemStack(Items.TOTEM_OF_UNDYING));
        }
        // getLootTable() 在 Mob 中是 final 无法覆写，形态差异化掉落放在这里：
        // 矿工形态 = 仅 0.1% 概率掉落下界合金镐（随机耐久）；普通形态 = 30% 概率掉落洒水器。
        if (this.minerForm) {
            if (this.random.nextFloat() < 0.001F) {
                ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
                pick.setDamageValue(this.random.nextInt(pick.getMaxDamage()));
                this.spawnAtLocation(pick);
            }
        } else if (this.random.nextFloat() < 0.2F) {
            this.spawnAtLocation(new ItemStack(ModItems.SPRINKLER.get()));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("MinerForm", this.minerForm);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("MinerForm")) {
            this.minerForm = compound.getBoolean("MinerForm");
            // 纯 NBT 直接生成（如 0721 试炼刷怪笼只写入 MinerForm 标志）时，
            // 构造器随机出的主手物品可能与矿工形态不一致，这里补齐下界合金镐；
            // 正常存档/读取（带 Equipment）路径不受影响。
            if (this.minerForm && !compound.contains("Equipment")) {
                ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
                pick.setDamageValue(this.random.nextInt(pick.getMaxDamage()));
                this.setItemSlot(EquipmentSlot.MAINHAND, pick);
            }
        }
    }

    public static boolean checkSprinklerCreepSpawnRules(
            EntityType<? extends Monster> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SPRINKLER_CREEP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SPRINKLER_CREEP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SPRINKLER_CREEP_DEATH.get();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        // 如果是凋零效果，返回false，无法被施加
        if (effect.getEffect() == MobEffects.WITHER) {
            return false;
        }
        // 其他效果走原版判定
        return super.canBeAffected(effect);
    }

    /** 袭击增益：与唤魔者一致，空实现。 */
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean p_37844_) {
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.EVOKER_CELEBRATE;
    }

    /** 按形态门控一个普通 Goal 的执行。 */
    private class FormGatedGoal extends Goal {
        private final Goal delegate;

        FormGatedGoal(Goal delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean canUse() {
            return SprinklerCreepEntity.this.minerForm && this.delegate.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return SprinklerCreepEntity.this.minerForm && this.delegate.canContinueToUse();
        }

        @Override
        public void start() {
            this.delegate.start();
        }

        @Override
        public void stop() {
            this.delegate.stop();
        }

        @Override
        public void tick() {
            this.delegate.tick();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.delegate.requiresUpdateEveryTick();
        }

        @Override
        public EnumSet<Goal.Flag> getFlags() {
            return this.delegate.getFlags();
        }

        @Override
        public boolean isInterruptable() {
            return this.delegate.isInterruptable();
        }
    }

    /** 远程攻击：仅在普通形态（非矿工）生效。 */
    private class FormGatedRangedAttackGoal extends RangedAttackGoal {
        FormGatedRangedAttackGoal(SprinklerCreepEntity mob, double speed, int interval, float radius) {
            super(mob, speed, interval, radius);
        }

        @Override
        public boolean canUse() {
            return !SprinklerCreepEntity.this.minerForm && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !SprinklerCreepEntity.this.minerForm && super.canContinueToUse();
        }
    }

    /** 近战攻击：仅在矿工形态生效。 */
    private class FormGatedMeleeAttackGoal extends MeleeAttackGoal {
        FormGatedMeleeAttackGoal(SprinklerCreepEntity mob, double speed, boolean followNotSeen) {
            super(mob, speed, followNotSeen);
        }

        @Override
        public boolean canUse() {
            return SprinklerCreepEntity.this.minerForm && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return SprinklerCreepEntity.this.minerForm && super.canContinueToUse();
        }
    }

    /** 索敌目标：普通形态须可见，矿工形态可透过方块索敌（mustSee 区分）。 */
    private class FormGatedTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final boolean forMiner;

        FormGatedTargetGoal(Class<T> targetType, boolean mustSee, boolean forMiner) {
            this(targetType, mustSee, forMiner, null);
        }

        FormGatedTargetGoal(Class<T> targetType, boolean mustSee, boolean forMiner, Predicate<LivingEntity> targetPredicate) {
            super(SprinklerCreepEntity.this, targetType, mustSee, targetPredicate);
            this.forMiner = forMiner;
        }

        @Override
        public boolean canUse() {
            return SprinklerCreepEntity.this.minerForm == this.forMiner && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return SprinklerCreepEntity.this.minerForm == this.forMiner && super.canContinueToUse();
        }
    }

    /** 攻击过程中不时召唤 3 只 0721 猫。召唤机制与 1.21.1 唤魔者 summon 完全一致：
     *  340 tick 施法冷却 + 20 tick 前摇（前摇结束才真正召唤）+ 16 格内数量门槛。 */
    private class SummonCatGoal extends Goal {
        private final SprinklerCreepEntity mob;
        /** 下一次可开始召唤的 tick（与唤魔者 getCastingInterval = 340 一致）。 */
        private int nextSpawnTick;
        /** 施法前摇倒计时（与唤魔者 base getCastWarmupTime = 20 一致）。 */
        private int warmupTicks;

        SummonCatGoal(SprinklerCreepEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            // 冷却未到：与唤魔者一致（tickCount >= nextAttackTickCount）
            if (this.mob.tickCount < this.nextSpawnTick) {
                return false;
            }
            // 每只 SprinklerCreep 召唤的 0721 猫最多同时存在 10 只，超过则停止召唤
            int catCount = this.mob.level().getEntitiesOfClass(Cat0721Entity.class,
                    this.mob.getBoundingBox().inflate(128.0),
                    cat -> this.mob.getUUID().equals(cat.getSummonerId())).size();
            return catCount < 10;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.warmupTicks > 0;
        }

        @Override
        public void start() {
            this.nextSpawnTick = this.mob.tickCount + 340;
            this.warmupTicks = 20;
        }

        @Override
        public void tick() {
            // 前摇结束才真正召唤（与唤魔者 performSpellCasting 在前摇结束时触发一致）
            if (--this.warmupTicks != 0) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                BlockPos blockpos = this.findCatSpawnPos();
                Cat0721Entity cat = ModEntities.CAT_0721.get().create(this.mob.level());
                if (cat != null) {
                    cat.setSummoner(this.mob.getUUID());
                    cat.moveTo(blockpos, 0.0F, 0.0F);
                    cat.finalizeSpawn((ServerLevelAccessor) this.mob.level(),
                            this.mob.level().getCurrentDifficultyAt(blockpos),
                            MobSpawnType.MOB_SUMMONED, null);
                    this.mob.level().addFreshEntity(cat);
                }
            }
        }

        /**
         * 在召唤者附近挑一个不卡墙的格点：格内方块可替换（空气/草等）且脚下有支撑面。
         * 随机位置直接 moveTo 会把猫卡进墙里；最多试 8 次，兜底用召唤者头顶。
         */
        private BlockPos findCatSpawnPos() {
            Level level = this.mob.level();
            BlockPos mobPos = this.mob.blockPosition();
            for (int attempt = 0; attempt < 8; attempt++) {
                BlockPos candidate = mobPos.offset(
                        -2 + this.mob.getRandom().nextInt(5), 1, -2 + this.mob.getRandom().nextInt(5));
                if (level.getBlockState(candidate).canBeReplaced()
                        && !level.getBlockState(candidate.below()).isAir()) {
                    return candidate;
                }
            }
            return mobPos.above();
        }

        @Override
        public void stop() {
            this.warmupTicks = 0;
        }
    }
}