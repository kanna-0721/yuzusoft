package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CYCLONE_SWORD.get())
                .pattern(" B ")
                .pattern(" E ")
                .pattern(" H ")
                .define('E', Items.HEAVY_CORE)
                .define('B', Items.EMERALD_BLOCK)
                .define('H', Items.STICK)
                .unlockedBy("has_heavy_core", has(Items.HEAVY_CORE))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.SHADOW_DART.get(), 4)
                .requires(Items.IRON_NUGGET, 3)
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.TAMAGOYAKI.get(), 1)
                .requires(Items.EGG)
                .requires(Items.SUGAR)
                .unlockedBy("has_egg", has(Items.EGG))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DETONATOR.get(), 3)
                .pattern("AD")
                .pattern("CB")
                .define('A', Items.TNT)
                .define('B', Items.TNT)
                .define('C', Items.REDSTONE)
                .define('D', Items.REDSTONE)
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOATING_SENTINEL_CHESTPLATE.get())
                .pattern("ABA")
                .pattern("BBB")
                .pattern("CCC")
                .define('A', Items.WIND_CHARGE)
                .define('B', Items.LEATHER)
                .define('C', Items.STRING)
                .unlockedBy("has_wind_charge", has(Items.WIND_CHARGE))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.DUMPLINGS.get(), 2)
                .requires(Items.WHEAT)
                .requires(Tags.Items.FOODS_COOKED_MEAT)
                .unlockedBy("has_cooked_meat", has(Tags.Items.FOODS_COOKED_MEAT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GUITAR_WEAPON.get())
                .pattern("CDC")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ItemTags.LOGS)
                .define('B', Items.IRON_INGOT)
                .define('C', Items.STRING)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GUITAR_UPGRADE_TEMPLATE.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', Items.STRING)
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.GUITAR_UPGRADE_TEMPLATE.get()),
                        Ingredient.of(ModItems.GUITAR_WEAPON.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        ModItems.GUITAR_WEAPON_FIXED.get())
                .unlocks("has_guitar_weapon", has(ModItems.GUITAR_WEAPON.get()))
                .save(recipeOutput, "guitar_weapon_upgrade");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.FLASHBANG.get(), 1)
                .requires(Items.FIRE_CHARGE)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_fire_charge", has(Items.FIRE_CHARGE))
                .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STUN_HAMMER.get())
                .pattern(" II")
                .pattern(" SI")
                .pattern("S  ")
                .define('I', Items.IRON_BLOCK)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_BLOCK))
                .save(recipeOutput);

        // 初级血包 + 锻造模版 + 皮革 = 中级血包
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.BLOOD_PACK_TEMPLATE.get()),
                        Ingredient.of(ModItems.BLOOD_PACK_BASIC.get()),
                        Ingredient.of(Items.LEATHER),
                        RecipeCategory.FOOD,
                        ModItems.BLOOD_PACK_MID.get())
                .unlocks("has_template", has(ModItems.BLOOD_PACK_TEMPLATE.get()))
                .save(recipeOutput, "blood_pack_mid_from_basic");

        // 中级血包 + 锻造模版 + 皮革 = 高级血包
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.BLOOD_PACK_TEMPLATE.get()),
                        Ingredient.of(ModItems.BLOOD_PACK_MID.get()),
                        Ingredient.of(Items.LEATHER),
                        RecipeCategory.FOOD,
                        ModItems.BLOOD_PACK_HIGH.get())
                .unlocks("has_template", has(ModItems.BLOOD_PACK_TEMPLATE.get()))
                .save(recipeOutput, "blood_pack_high_from_mid");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DUAL_FORM_MOB_SPAWN_EGG.get())
                .pattern("ADA")
                .pattern("CBC")
                .pattern("ADA")
                .define('A', Items.EMERALD_BLOCK)
                .define('B', Items.EGG)
                .define('C', Items.GOLD_BLOCK)
                .define('D', Items.REDSTONE_BLOCK)
                .unlockedBy("has_emerald_block", has(Items.EMERALD_BLOCK))
                .unlockedBy("has_gold_block", has(Items.GOLD_BLOCK))
                .unlockedBy("has_redstone_block", has(Items.REDSTONE_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HUMANOID_CREATURE_SPAWN_EGG.get())
                .pattern("ABC")
                .pattern("DED")
                .pattern("CBA")
                .define('A', Items.COD)
                .define('B', Items.SALMON)
                .define('C', Items.TROPICAL_FISH)
                .define('D', Items.PUFFERFISH)
                .define('E', Items.EGG)
                .unlockedBy("has_cod", has(Items.COD))
                .unlockedBy("has_salmon", has(Items.SALMON))
                .unlockedBy("has_tropical_fish", has(Items.TROPICAL_FISH))
                .unlockedBy("has_pufferfish", has(Items.PUFFERFISH))
                .save(recipeOutput);
    }
}
