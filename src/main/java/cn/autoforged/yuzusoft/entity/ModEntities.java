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
            ENTITY_TYPES.register("murasame",
                    () -> EntityType.Builder.<GuardianEntity>of(GuardianEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("murasame"));
    public static final DeferredHolder<EntityType<?>, EntityType<GuardianTraderEntity>> GUARDIAN_TRADER =
            ENTITY_TYPES.register("tomotake_yoshino",
                    () -> EntityType.Builder.<GuardianTraderEntity>of(
                                    GuardianTraderEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(10)
                            .ridingOffset(-0.6F)
                            .build("tomotake_yoshino"));
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowAssassinEntity>> SHADOW_ASSASSIN =
            ENTITY_TYPES.register("hitachi_mako",
                    () -> EntityType.Builder.of(ShadowAssassinEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("hitachi_mako"));
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
            ENTITY_TYPES.register("ayachi_nene",
                    () -> EntityType.Builder.of(
                                    DetonatorThrowingMonsterEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("ayachi_nene"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostGuardianEntity>> FROST_GUARDIAN =
            ENTITY_TYPES.register("inaba_meguru",
                    () -> EntityType.Builder.of(FrostGuardianEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .passengerAttachments(2.0F)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("inaba_meguru"));

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
            ENTITY_TYPES.register("shikibe_mayu",
                    () -> EntityType.Builder.<FrostGuardianV2Entity>of(FrostGuardianV2Entity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .ridingOffset(-0.6F)
                            .build("shikibe_mayu"));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostBoltV2Projectile>> FROST_BOLT_V2 =
            ENTITY_TYPES.register("frost_bolt_v2",
                    () -> EntityType.Builder.<FrostBoltV2Projectile>of(FrostBoltV2Projectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .build("frost_bolt_v2"));
    public static final DeferredHolder<EntityType<?>, EntityType<FloatingSentinelEntity>> FLOATING_SENTINEL =
            ENTITY_TYPES.register("mitsukasa_ayase",
                    () -> EntityType.Builder.of(FloatingSentinelEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("mitsukasa_ayase"));
    public static final DeferredHolder<EntityType<?>, EntityType<LevitationBulletEntity>> LEVITATION_BULLET =
            ENTITY_TYPES.register("levitation_bullet",
                    () -> EntityType.Builder.<LevitationBulletEntity>of(LevitationBulletEntity::new, MobCategory.MISC)
                            .sized(0.3125f, 0.3125f)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build("levitation_bullet"));
    public static final DeferredHolder<EntityType<?>, EntityType<VillageGuardianEntity>> VILLAGE_GUARDIAN =
            ENTITY_TYPES.register("arihara_nanami",
                    () -> EntityType.Builder.of(
                                    VillageGuardianEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("arihara_nanami"));
    public static final DeferredHolder<EntityType<?>, EntityType<SprinklerCreepEntity>> SPRINKLER_CREEP =
            ENTITY_TYPES.register("akizuki_kanna",
                    () -> EntityType.Builder.of(SprinklerCreepEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("akizuki_kanna"));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterBallEntity>> WATER_BALL =
            ENTITY_TYPES.register("water_ball",
                    () -> EntityType.Builder.<WaterBallEntity>of(WaterBallEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("water_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuitarMonsterEntity>> GUITAR_MONSTER =
            ENTITY_TYPES.register("harumi_ena",
                    () -> EntityType.Builder.<GuitarMonsterEntity>of(GuitarMonsterEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("harumi_ena"));
    public static final DeferredHolder<EntityType<?>, EntityType<SleepySpiritEntity>> SLEEPY_SPIRIT =
            ENTITY_TYPES.register("futamihara_ririko",
                    () -> EntityType.Builder.<SleepySpiritEntity>of(SleepySpiritEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("futamihara_ririko"));
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
            ENTITY_TYPES.register("nabari_anju",
                    () -> EntityType.Builder.<FlashbangMonster>of(FlashbangMonster::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("nabari_anju"));
    public static final DeferredHolder<EntityType<?>, EntityType<HammerWielder>> HAMMER_WIELDER =
            ENTITY_TYPES.register("shiiba_tsumugi",
                    () -> EntityType.Builder.of(HammerWielder::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .passengerAttachments(2.0F)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("shiiba_tsumugi"));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodSuckerZombieEntity>> BLOOD_SUCKER_ZOMBIE =
            ENTITY_TYPES.register("yarai_miu",
                    () -> EntityType.Builder.of(BloodSuckerZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(2.0f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("yarai_miu"));
    public static final DeferredHolder<EntityType<?>, EntityType<Cat0721Entity>> CAT_0721 =
            ENTITY_TYPES.register("mikado_takanori",
                    () -> EntityType.Builder.<Cat0721Entity>of(Cat0721Entity::new, MobCategory.CREATURE)
                            .sized(0.6f, 0.7f)
                            .clientTrackingRange(8)
                            .build("mikado_takanori"));
    public static final DeferredHolder<EntityType<?>, EntityType<DualFormMobEntity>> DUAL_FORM_MOB =
            ENTITY_TYPES.register("shirayuki_noa",
                    () -> EntityType.Builder.of(DualFormMobEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .passengerAttachments(1.5f)
                            .ridingOffset(-0.4f)
                            .clientTrackingRange(8)
                            .build("shirayuki_noa"));
    public static final DeferredHolder<EntityType<?>, EntityType<HumanoidCreatureEntity>> HUMANOID_CREATURE =
            ENTITY_TYPES.register("tanikaze_amane",
                    () -> EntityType.Builder.of(HumanoidCreatureEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("tanikaze_amane"));
    public static final DeferredHolder<EntityType<?>, EntityType<SuzuneEntity>> SUZUNE =
            ENTITY_TYPES.register("shioyama_suzune",
                    () -> EntityType.Builder.<SuzuneEntity>of(SuzuneEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .ridingOffset(-0.6F)
                            .build("shioyama_suzune"));

    // ---- 来海（kurumi 工程并入）----
    public static final DeferredHolder<EntityType<?>, EntityType<DiamondGuardianEntity>> DIAMOND_GUARDIAN =
            ENTITY_TYPES.register("kohibari_kurumi",
                    () -> EntityType.Builder.of(DiamondGuardianEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("kohibari_kurumi"));

    // ---- 邪恶七海：0721 袭击专属（替换女巫），仅在袭击中生成 ----
    public static final DeferredHolder<EntityType<?>, EntityType<EvilNanamiEntity>> EVIL_NANAMI =
            ENTITY_TYPES.register("evil_nanami",
                    () -> EntityType.Builder.of(EvilNanamiEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(8)
                            .ridingOffset(-0.6F)
                            .build("evil_nanami"));

    // ---- 水灵（J 工程并入）----
    public static final DeferredHolder<EntityType<?>, EntityType<WaterSpiritEntity>> WATER_SPIRIT =
            ENTITY_TYPES.register("nijouin_hazuki",
                    () -> EntityType.Builder.<WaterSpiritEntity>of(WaterSpiritEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.9F)
                            .eyeHeight(1.74F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .ridingOffset(-0.6F)
                            .build("nijouin_hazuki"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaterOrbEntity>> WATER_ORB =
            ENTITY_TYPES.register("water_orb",
                    () -> EntityType.Builder.<WaterOrbEntity>of(WaterOrbEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("water_orb"));

    // ---- 岛越月望（PianoNeoForge 工程并入）----
    public static final DeferredHolder<EntityType<?>, EntityType<ShimagoeTsukumi>> SHIMAGOE_TSUKUMI =
            ENTITY_TYPES.register("shimagoe_tsukumi",
                    () -> EntityType.Builder.<ShimagoeTsukumi>of(ShimagoeTsukumi::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .eyeHeight(1.62F)
                            .clientTrackingRange(10)
                            .ridingOffset(-0.6F)
                            .build("shimagoe_tsukumi"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
