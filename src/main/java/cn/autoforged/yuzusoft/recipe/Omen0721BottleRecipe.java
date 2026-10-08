package cn.autoforged.yuzusoft.recipe;

import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 合成：1×原版不祥之瓶 + 8×影刺（shadow_dart） → 1×0721不祥之瓶。
 * <p>等级保持一致：输出瓶的 0721 等级 = 输入瓶放大器 + 1（原版不祥之兆放大器 a 对应袭击等级 a+1，
 * 0721 瓶等级 1-5 对应 0721 袭击等级 1-5），并夹取到 [1,5]。
 */
public class Omen0721BottleRecipe extends CustomRecipe {

    public Omen0721BottleRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasBottle = false;
        int dartCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.OMINOUS_BOTTLE)) {
                if (hasBottle) {
                    return false; // 多于一瓶
                }
                hasBottle = true;
            } else if (stack.is(ModItems.SHADOW_DART.get())) {
                dartCount++;
            } else {
                return false; // 其余材料一律不允许
            }
        }
        return hasBottle && dartCount == 8;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int value = 1; // 兜底等级
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(Items.OMINOUS_BOTTLE)) {
                int amplifier = stack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 1);
                value = amplifier + 1; // 输入放大器 a → 0721 等级 a+1
                break;
            }
        }
        ItemStack result = new ItemStack(ModItems.OMENS_BOTTLE_0721.get());
        result.set(ModDataComponents.BOTTLE_0721_LEVEL, Mth.clamp(value, 1, 5));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.OMEN_0721_BOTTLE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // 1 瓶 + 8 影刺共 9 件材料，需要 3×3 工作格
        return width * height >= 9;
    }

    /** 原料数量标签（用于合成提示，不参与逻辑）。 */
    public int getDartCount() {
        return 8;
    }
}