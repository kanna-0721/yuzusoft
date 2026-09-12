package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EntityAttributeHandler {

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GUARDIAN.get(), GuardianEntity.createAttributes().build());
        event.put(ModEntities.GUARDIAN_TRADER.get(), GuardianTraderEntity.createAttributes().build());
        event.put(ModEntities.SHADOW_ASSASSIN.get(), ShadowAssassinEntity.createAttributes().build());
        event.put(ModEntities.DETONATOR_THROWING_MONSTER.get(),
                DetonatorThrowingMonsterEntity.createAttributes().build());
        event.put(ModEntities.FROST_GUARDIAN.get(),
                FrostGuardianEntity.createAttributes().build());
        event.put(ModEntities.FROST_GUARDIAN_V2.get(),
                FrostGuardianV2Entity.createAttributes().build());
        event.put(ModEntities.FLOATING_SENTINEL.get(), FloatingSentinelEntity.createAttributes().build());
        event.put(ModEntities.VILLAGE_GUARDIAN.get(), VillageGuardianEntity.createAttributes().build());
        event.put(ModEntities.SPRINKLER_CREEP.get(), SprinklerCreepEntity.createAttributes().build());
        event.put(ModEntities.GUITAR_MONSTER.get(), GuitarMonsterEntity.createAttributes().build());
        event.put(ModEntities.SLEEPY_SPIRIT.get(), SleepySpiritEntity.createAttributes().build());
        event.put(ModEntities.FLASHBANG_MONSTER.get(), FlashbangMonster.createAttributes().build());
        event.put(ModEntities.HAMMER_WIELDER.get(), HammerWielder.createAttributes().build());
        event.put(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), BloodSuckerZombieEntity.createAttributes().build());
        event.put(ModEntities.DUAL_FORM_MOB.get(), DualFormMobEntity.createAttributes().build());
        event.put(ModEntities.HUMANOID_CREATURE.get(), HumanoidCreatureEntity.createAttributes().build());
        event.put(ModEntities.SUZUNE.get(), SuzuneEntity.createAttributes().build());
    }
    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.GUARDIAN_TRADER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SHADOW_ASSASSIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(
                ModEntities.DETONATOR_THROWING_MONSTER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.FROST_GUARDIAN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.FROST_GUARDIAN_V2.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.FLOATING_SENTINEL.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FloatingSentinelEntity::checkFloatingSentinelSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(
                (net.minecraft.world.entity.EntityType<? extends Animal>) ModEntities.VILLAGE_GUARDIAN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VillageGuardianEntity::checkVillageGuardianSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(
                (net.minecraft.world.entity.EntityType<? extends Monster>) ModEntities.SPRINKLER_CREEP.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SprinklerCreepEntity::checkSprinklerCreepSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(
                (net.minecraft.world.entity.EntityType<? extends Monster>) ModEntities.GUITAR_MONSTER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.FLASHBANG_MONSTER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FlashbangMonster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(
                ModEntities.SLEEPY_SPIRIT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SleepySpiritEntity::checkSleepySpiritSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.HAMMER_WIELDER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HammerWielder::checkHammerWielderSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BloodSuckerZombieEntity::checkBloodSuckerSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(ModEntities.SUZUNE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
