package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** 菜刀：伤害 4，可随钻石剑一起获得剑类附魔（见 enchantable 标签）。 */
public class CleaverItem extends Item {
    public CleaverItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".cleaver.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}