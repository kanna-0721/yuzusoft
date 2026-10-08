package cn.autoforged.yuzusoft.recipe;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 0721 专属合成序列化器注册。 */
public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CycloneSwordMod.MODID);

    /** crafting_special_omen_0721：1×原版不祥之瓶 + 8×影刺 → 0721 不祥之瓶。 */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<Omen0721BottleRecipe>> OMEN_0721_BOTTLE =
            RECIPE_SERIALIZERS.register("crafting_special_omen_0721",
                    () -> new SimpleCraftingRecipeSerializer<>(Omen0721BottleRecipe::new));

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}