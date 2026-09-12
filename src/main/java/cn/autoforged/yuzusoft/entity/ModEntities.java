package cn.autoforged.yuzusoft.entity;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static cn.autoforged.yuzusoft.item.ModItems.registerItem;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CycloneSwordMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GuardianEntity>> GUARDIAN =
            ENTITY_TYPES.register("guardian",
                    () -> EntityType.Builder.<GuardianEntity>of(GuardianEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("guardian"));
    public static final DeferredHolder<EntityType<?>, EntityType<GuardianTraderEntity>> GUARDIAN_TRADER =
            ENTITY_TYPES.register("guardian_trader",
                    () -> EntityType.Builder.<GuardianTraderEntity>of(
                                    GuardianTraderEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(10)
                            .build("guardian_trader"));
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowAssassinEntity>> SHADOW_ASSASSIN =
            ENTITY_TYPES.register("shadow_assassin",
                    () -> EntityType.Builder.of(ShadowAssassinEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(8)
                            .build("shadow_assassin"));
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowDartEntity>> SHADOW_DART =
            ENTITY_TYPES.register("shadow_dart",
                    () -> EntityType.Builder.<ShadowDartEntity>of(ShadowDartEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("shadow_dart"));

    public static final DeferredHolder<EntityType<?>, EntityType<DetonatorProjectileEntity>> DETONATOR_PROJECTILE =
            ENTITY_TYPES.register("detonator_projectile",
                    () -> EntityType.Builder.<DetonatorProjectileEntity>of(
                                    DetonatorProjectileEntity::new,
                                    MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("detonator_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<DetonatorThrowingMonsterEntity>> DETONATOR_THROWING_MONSTER =
            ENTITY_TYPES.register("detonator_throwing_monster",
                    () -> EntityType.Builder.of(
                                    DetonatorThrowingMonsterEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("detonator_throwing_monster"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostGuardianEntity>> FROST_GUARDIAN =
            ENTITY_TYPES.register("frost_guardian",
                    () -> EntityType.Builder.of(FrostGuardianEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .passengerAttachments(2.0F)
                            .clientTrackingRange(8)
                            .build("frost_guardian"));

    public static final DeferredHolder<EntityType<?>, EntityType<FrostProjectileEntity>> FROST_PROJECTILE =
            ENTITY_TYPES.register("frost_projectile",
                    () -> EntityType.Builder.<FrostProjectileEntity>of(FrostProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("frost_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostHealProjectileEntity>> FROST_HEAL_PROJECTILE =
            ENTITY_TYPES.register("frost_heal_projectile",
                    () -> EntityType.Builder.<FrostHealProjectileEntity>of(FrostHealProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("frost_heal_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostGuardianV2Entity>> FROST_GUARDIAN_V2 =
            ENTITY_TYPES.register("frost_guardian_v2",
                    () -> EntityType.Builder.<FrostGuardianV2Entity>of(FrostGuardianV2Entity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .build("frost_guardian_v2"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostBoltV2Projectile>> FROST_BOLT_V2 =
            ENTITY_TYPES.register("frost_bolt_v2",
                    () -> EntityType.Builder.<FrostBoltV2Projectile>of(FrostBoltV2Projectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .build("frost_bolt_v2"));
    public static final DeferredHolder<EntityType<?>, EntityType<FloatingSentinelEntity>> FLOATING_SENTINEL =
            ENTITY_TYPES.register("floating_sentinel",
                    () -> EntityType.Builder.of(FloatingSentinelEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("floating_sentinel"));
    public static final DeferredHolder<EntityType<?>, EntityType<LevitationBulletEntity>> LEVITATION_BULLET =
            ENTITY_TYPES.register("levitation_bullet",
                    () -> EntityType.Builder.<LevitationBulletEntity>of(LevitationBulletEntity::new, MobCategory.MISC)
                            .sized(0.3125f, 0.3125f)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build("levitation_bullet"));
    public static final DeferredHolder<EntityType<?>, EntityType<VillageGuardianEntity>> VILLAGE_GUARDIAN =
            ENTITY_TYPES.register("village_guardian",
                    () -> EntityType.Builder.of(
                                    VillageGuardianEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("village_guardian"));
    public static final DeferredHolder<EntityType<?>, EntityType<SprinklerCreepEntity>> SPRINKLER_CREEP =
            ENTITY_TYPES.register("sprinkler_creep",
                    () -> EntityType.Builder.of(SprinklerCreepEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(8)
                            .build("sprinkler_creep"));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterBallEntity>> WATER_BALL =
            ENTITY_TYPES.register("water_ball",
                    () -> EntityType.Builder.<WaterBallEntity>of(WaterBallEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("water_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuitarMonsterEntity>> GUITAR_MONSTER =
            ENTITY_TYPES.register("guitar_monster",
                    () -> EntityType.Builder.<GuitarMonsterEntity>of(GuitarMonsterEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build("guitar_monster"));
    public static final DeferredHolder<EntityType<?>, EntityType<SleepySpiritEntity>> SLEEPY_SPIRIT =
            ENTITY_TYPES.register("sleepy_spirit",
                    () -> EntityType.Builder.<SleepySpiritEntity>of(SleepySpiritEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("sleepy_spirit"));
    public static final DeferredHolder<EntityType<?>, EntityType<MagicBolt>> MAGIC_BOLT =
            ENTITY_TYPES.register("magic_bolt",
                    () -> EntityType.Builder.<MagicBolt>of(MagicBolt::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("magic_bolt"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlashbangProjectile>> FLASHBANG_PROJECTILE =
            ENTITY_TYPES.register("flashbang_projectile",
                    () -> EntityType.Builder.<FlashbangProjectile>of(FlashbangProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("flashbang_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlashbangMonster>> FLASHBANG_MONSTER =
            ENTITY_TYPES.register("flashbang_monster",
                    () -> EntityType.Builder.<FlashbangMonster>of(FlashbangMonster::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("flashbang_monster"));
    public static final DeferredHolder<EntityType<?>, EntityType<HammerWielder>> HAMMER_WIELDER =
            ENTITY_TYPES.register("hammer_wielder",
                    () -> EntityType.Builder.of(HammerWielder::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .passengerAttachments(2.0F)
                            .clientTrackingRange(8)
                            .build("hammer_wielder"));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodSuckerZombieEntity>> BLOOD_SUCKER_ZOMBIE =
            ENTITY_TYPES.register("blood_sucker_zombie",
                    () -> EntityType.Builder.of(BloodSuckerZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(8)
                            .build("blood_sucker_zombie"));
    public static final DeferredHolder<EntityType<?>, EntityType<DualFormMobEntity>> DUAL_FORM_MOB =
            ENTITY_TYPES.register("dual_form_mob",
                    () -> EntityType.Builder.of(DualFormMobEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(1.5f)
                            .ridingOffset(-0.4f)
                            .clientTrackingRange(8)
                            .build("dual_form_mob"));
    public static final DeferredHolder<EntityType<?>, EntityType<HumanoidCreatureEntity>> HUMANOID_CREATURE =
            ENTITY_TYPES.register("humanoid_creature",
                    () -> EntityType.Builder.of(HumanoidCreatureEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .build("humanoid_creature"));
    public static final DeferredHolder<EntityType<?>, EntityType<SuzuneEntity>> SUZUNE =
            ENTITY_TYPES.register("suzune",
                    () -> EntityType.Builder.<SuzuneEntity>of(SuzuneEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .build("suzune"));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
