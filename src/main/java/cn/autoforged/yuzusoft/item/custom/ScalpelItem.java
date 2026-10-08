package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 手术刀：攻击力 1（玩家基础伤害 1 + 0），有耐久、可附魔（见 swords 物品标签）。
 * 用它击杀「有头生物」时必定掉落其头颅，并在原地留下无头身体（见 {@code ScalpelEvents}）。
 */
public class ScalpelItem extends Item {
    /** 附魔能力（同铁剑 14）：1.21.1 没有 Properties#enchantable，只能在此覆写。 */
    private static final int ENCHANTMENT_VALUE = 14;

    public ScalpelItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTMENT_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".scalpel.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}