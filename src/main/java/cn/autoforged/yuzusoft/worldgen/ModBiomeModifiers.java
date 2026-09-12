package cn.autoforged.yuzusoft.worldgen;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.ShadowAssassinEntity;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_SHADOW_ASSASSIN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_shadow_assassin"));
    public static final ResourceKey<BiomeModifier> ADD_GUARDIAN_TRADER_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_guardian_trader_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_FLOATING_SENTINEL_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_floating_sentinel_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_VILLAGE_GUARDIAN_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_village_guardian_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_SPRINKLER_CREEP_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_sprinkler_creep_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_SLEEPY_SPIRIT_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_sleepy_spirit_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_GUITAR_MONSTER_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_guitar_monster_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_FLASHBANG_MONSTER_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_flashbang_monster_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_HAMMER_WIELDER_SPAWN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_hammer_wielder_spawn"));
    public static final ResourceKey<BiomeModifier> ADD_BLOOD_SUCKER_ZOMBIE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_blood_sucker_zombie"));
    public static final ResourceKey<BiomeModifier> ADD_SUZUNE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "add_suzune"));
    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ADD_SHADOW_ASSASSIN,
                BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        new MobSpawnSettings.SpawnerData(ModEntities.SHADOW_ASSASSIN.get(), 70, 1, 3)));
        context.register(ADD_GUARDIAN_TRADER_SPAWN,
                BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        new MobSpawnSettings.SpawnerData(ModEntities.GUARDIAN_TRADER.get(), 20, 1, 1)));
        context.register(ADD_FLOATING_SENTINEL_SPAWN, new BiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.FLOATING_SENTINEL.get(), 30, 1, 2))));
        context.register(ADD_VILLAGE_GUARDIAN_SPAWN, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                new MobSpawnSettings.SpawnerData(
                        ModEntities.VILLAGE_GUARDIAN.get(), 3, 1, 1)));
        context.register(ADD_SPRINKLER_CREEP_SPAWN, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                new MobSpawnSettings.SpawnerData(
                        ModEntities.SPRINKLER_CREEP.get(), 60, 1, 3)));
        context.register(ADD_SLEEPY_SPIRIT_SPAWN, new BiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.SLEEPY_SPIRIT.get(), 10, 1, 2))));
        context.register(ADD_GUITAR_MONSTER_SPAWN, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                new MobSpawnSettings.SpawnerData(ModEntities.GUITAR_MONSTER.get(), 40, 1, 3)));
        context.register(ADD_FLASHBANG_MONSTER_SPAWN, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                new MobSpawnSettings.SpawnerData(ModEntities.FLASHBANG_MONSTER.get(), 40, 1, 3)));
        context.register(ADD_HAMMER_WIELDER_SPAWN,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        List.of(new MobSpawnSettings.SpawnerData(ModEntities.HAMMER_WIELDER.get(), 50, 1, 1))));
        context.register(ADD_BLOOD_SUCKER_ZOMBIE,
                BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        new MobSpawnSettings.SpawnerData(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), 30, 1, 3)));
        context.register(ADD_SUZUNE,
                BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        new MobSpawnSettings.SpawnerData(ModEntities.SUZUNE.get(), 30, 1, 2)));
    }
}

