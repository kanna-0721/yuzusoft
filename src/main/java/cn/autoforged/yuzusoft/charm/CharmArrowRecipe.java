package cn.autoforged.yuzusoft.charm;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 魅惑之箭合成（完整替代原版附魔箭特殊配方）：3×3 摆满，中心为任意滞留药水，其余 8 格为箭。
 * - 滞留魅惑药水 → 8 张魅惑之箭，效果以"自定义效果列表"形式完整继承药水时长（命中不衰减）；
 * - 其他滞留药水 → 保持原版行为，输出携带原药水成分的附魔箭。
 * （原版 minecraft:tipped_arrow 配方由数据包条件禁用，见 data/minecraft/recipe/tipped_arrow.json。）
 */
public class CharmArrowRecipe extends CustomRecipe {
    public CharmArrowRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean hasCharmEffect(PotionContents contents) {
        for (MobEffectInstance instance : contents.getAllEffects()) {
            if (instance.is(CharmRegistry.CHARM_EFFECT)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                ItemStack stack = input.getItem(x, y);
                if (x == 1 && y == 1) {
                    if (!stack.is(Items.LINGERING_POTION)) {
                        return false;
                    }
                } else if (!stack.is(Items.ARROW)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack center = input.getItem(1, 1);
        PotionContents contents = center.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!hasCharmEffect(contents)) {
            // 原版行为：普通滞留药水 → 附魔之箭（命中时按原版 1/8 缩放）
            ItemStack vanillaOutput = new ItemStack(Items.TIPPED_ARROW, 8);
            vanillaOutput.set(DataComponents.POTION_CONTENTS, contents);
            return vanillaOutput;
        }
        // 魅惑滞留药水 → 魅惑之箭：自定义效果列表原样保留时长
        PotionContents arrowContents = PotionContents.EMPTY;
        for (MobEffectInstance instance : contents.getAllEffects()) {
            arrowContents = arrowContents.withEffectAdded(new MobEffectInstance(
                    instance.getEffect(),
                    Math.max(1, instance.getDuration()),
                    instance.getAmplifier(),
                    instance.isAmbient(),
                    instance.isVisible()));
        }
        ItemStack output = new ItemStack(CharmRegistry.CHARM_ARROW.get(), 8);
        output.set(DataComponents.POTION_CONTENTS, arrowContents);
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CharmRegistry.CHARM_ARROW_SERIALIZER.get();
    }
}
