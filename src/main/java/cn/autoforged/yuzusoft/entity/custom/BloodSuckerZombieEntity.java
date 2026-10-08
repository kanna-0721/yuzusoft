package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class BloodSuckerZombieEntity extends Raider {
    public static final float HEAL_ON_HIT_PERCENT = 0.5f;
    /** 0721 袭击移速倍率：自然生成（野生）吸血僵尸不受影响。 */
    private static final double RAID_SPEED_MULTIPLIER = 1.5D;
    private static final ResourceLocation OTHER_EFFECT_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "effect_0721");
    private static final int EFFECT_DURATION  = 100;
    private static final int EFFECT_AMPLIFIER = 0;

    public BloodSuckerZombieEntity(EntityType<? extends BloodSuckerZombieEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0);
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
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && !this.level().isClientSide) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            this.heal(damage * HEAL_ON_HIT_PERCENT);
            if (target instanceof LivingEntity living) {
                BuiltInRegistries.MOB_EFFECT.getOptional(OTHER_EFFECT_ID)
                        .map(BuiltInRegistries.MOB_EFFECT::wrapAsHolder)
                        .ifPresent(holder -> living.addEffect(
                                new MobEffectInstance(holder, EFFECT_DURATION, EFFECT_AMPLIFIER),
                                this));
            }
        }
        return flag;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals(); // Raider：袭击旗帜/寻路/穿村/庆祝
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
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

    /**
     * 袭击增益：必定穿戴铁头盔，且 0721 袭击专属强化——手持铁斧 + 移速翻倍。
     * 注：本方法仅在加入袭击时调用；野生吸血僵尸（非 EVENT 生成）被
     * {@link #finalizeSpawn} 设为 {@code setCanJoinRaid(false)} 永不入袭击，
     * 因此自然生成的吸血僵尸不受影响。
     */
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean p_37844_) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(speed.getBaseValue() * RAID_SPEED_MULTIPLIER);
        }
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
        return ModSounds.BLOOD_SUCKER_ZOMBIE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.BLOOD_SUCKER_ZOMBIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BLOOD_SUCKER_ZOMBIE_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval(){
        return 240;
    }

    public static boolean checkBloodSuckerSpawnRules(
            EntityType<BloodSuckerZombieEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }
}