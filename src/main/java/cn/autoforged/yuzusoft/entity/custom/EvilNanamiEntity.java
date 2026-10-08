package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 邪恶七海：0721 袭击专属支援型袭击者（替换女巫，仅在 0721 袭击中生成）。
 *
 * <p>贴图 / 音效与"在原七海"（arihara_nanami）共用；无专属装备增益。
 * 每 20 tick 扫描半径 16 格内的 0721 生物：
 * 8 格内施加 生命恢复 II，8–16 格施加 生命恢复 I。
 */
public class EvilNanamiEntity extends Raider {
    /** 光环扫描间隔（tick）。 */
    private static final int AURA_INTERVAL = 20;
    /** 生命恢复 II 半径。 */
    private static final double AURA_RADIUS_INNER = 8.0;
    /** 生命恢复 I 半径。 */
    private static final double AURA_RADIUS_OUTER = 16.0;
    private static final int EFFECT_DURATION = 100;
    private int auraTickCounter = 0;

    public EvilNanamiEntity(EntityType<? extends EvilNanamiEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
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
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // 索敌：族群支援目标，并重排：玩家(2) > 铁傀儡(3) > 村民(4)。
        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        // 族群支援：优先级在玩家/铁傀儡/村民之后，未锁定更高优先级目标时才前往支援
        this.targetSelector.addGoal(5, new GroupSupportTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            auraTickCounter++;
            if (auraTickCounter >= AURA_INTERVAL) {
                auraTickCounter = 0;
                applyAuraEffects(serverLevel);
            }
        }
    }

    /** 为半径 16 格内所有 0721 生物施加生命恢复：≤8 格 II，8–16 格 I。 */
    private void applyAuraEffects(ServerLevel serverLevel) {
        AABB box = this.getBoundingBox().inflate(AURA_RADIUS_OUTER);
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class, box, entity ->
                        entity != this && entity.isAlive() && !entity.isRemoved()
                                && Group0721Helper.isGroup0721(entity));

        for (LivingEntity target : nearby) {
            double dist = Math.sqrt(this.distanceToSqr(target));
            if (dist <= AURA_RADIUS_INNER) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, EFFECT_DURATION, 1, false, false, true), this);
            } else if (dist <= AURA_RADIUS_OUTER) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, EFFECT_DURATION, 0, false, false, true), this);
            }
        }
    }

    /** 纯支援单位：无专属装备/属性增益。 */
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean p_37844_) {
    }

    @Override
    public boolean canBeLeader() {
        return false;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
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
    public int getAmbientSoundInterval() {
        return 240;
    }
}
