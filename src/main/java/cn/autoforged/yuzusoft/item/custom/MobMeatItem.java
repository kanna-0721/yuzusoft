package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.MobMeatData;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 生物肉：营养总量等于来源生物最大生命值；食用结算规则为“先补满损失的饱食度，
 * 剩余数值转为饱和度（上限 20）”。物品/掉落/手持均通过 builtin/entity 自定义渲染器
 * 显示来源生物的迷你模型（缩放按体型归一化）。
 */
public class MobMeatItem extends Item {
    /** 1.21.1 玩家最大饱食度为固定 20。 */
    private static final int MAX_FOOD_LEVEL = 20;
    /** 饱和度上限，按策划固定为 20。 */
    private static final float SATURATION_CAP = 20.0F;

    public MobMeatItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MobMeatData data = stack.get(ModDataComponents.MOB_MEAT.get());
        if (data == null) {
            return super.getName(stack);
        }
        return Component.translatable("item." + CycloneSwordMod.MODID + ".mob_meat", sourceName(data.entity()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide && consumer instanceof Player player) {
            MobMeatData data = stack.get(ModDataComponents.MOB_MEAT.get());
            if (data != null) {
                applyNutrition(player, data.nutrition());
            }
        }
        // 基类 FoodProperties 营养为 0，只做动画收尾与物品消耗，不会重复补给
        return super.finishUsingItem(stack, level, consumer);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        MobMeatData data = stack.get(ModDataComponents.MOB_MEAT.get());
        if (data == null) {
            return;
        }
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".mob_meat.tooltip.source", sourceName(data.entity()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".mob_meat.tooltip.nutrition", data.nutrition())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".mob_meat.tooltip.rule")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * 先补饥饿、余量转饱和：
     * nutrition 中最多 (20 - 当前饱食度) 的部分补饥饿，剩余部分转成饱和度，总饱和度不超过 20。
     */
    private static void applyNutrition(Player player, int nutrition) {
        FoodData foodData = player.getFoodData();
        int missingFood = Math.max(0, MAX_FOOD_LEVEL - foodData.getFoodLevel());
        int toFood = Math.min(nutrition, missingFood);
        if (toFood > 0) {
            foodData.setFoodLevel(foodData.getFoodLevel() + toFood);
        }
        int surplus = nutrition - toFood;
        if (surplus > 0) {
            foodData.setSaturation(Math.min(SATURATION_CAP, foodData.getSaturationLevel() + surplus));
        }
    }

    private static Component sourceName(ResourceLocation id) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (type == null) {
            return Component.translatable("item." + CycloneSwordMod.MODID + ".mob_meat.unknown_source");
        }
        return Component.translatable(type.getDescriptionId());
    }
}