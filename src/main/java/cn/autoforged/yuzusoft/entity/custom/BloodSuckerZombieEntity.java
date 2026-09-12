package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class BloodSuckerZombieEntity extends Zombie {
    public static final float HEAL_ON_HIT_PERCENT = 0.5f;
    private static final ResourceLocation OTHER_EFFECT_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "effect_0721");
    private static final int EFFECT_DURATION  = 100;
    private static final int EFFECT_AMPLIFIER = 0;

    public BloodSuckerZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0);
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