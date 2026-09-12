package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModLootModifierProvider extends GlobalLootModifierProvider {
    public ModLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CycloneSwordMod.MODID);
    }

    @Override
    protected void start() {
        add("blood_template_dungeon_chest",
                new AddTableLootModifier(
                        new LootItemCondition[] {
                                LootTableIdCondition.builder(
                                        ResourceLocation.withDefaultNamespace("chests/simple_dungeon")
                                ).build(),
                                LootItemRandomChanceCondition.randomChance(1.0f).build()

                        },
                        ResourceKey.create(Registries.LOOT_TABLE,
                                ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "inject/blood_template"))
                )
        );
        add("blood_template_mineshaft_chest",
                new AddTableLootModifier(
                        new LootItemCondition[] {
                                LootTableIdCondition.builder(
                                        ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")
                                ).build(),
                                LootItemRandomChanceCondition.randomChance(1.0f).build()
                        },
                        ResourceKey.create(Registries.LOOT_TABLE,
                                ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "inject/blood_template"))
                )
        );
    }
}