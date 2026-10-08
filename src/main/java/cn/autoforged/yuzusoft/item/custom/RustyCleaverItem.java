package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 生锈菜刀：攻击伤害被压到 0；物品提示中显示文字倒计时（60 秒冷却恢复），
 * 图标本身使用暗色铁锈贴图实现“灰化”效果，不附加粒子。
 */
public class RustyCleaverItem extends Item {
    public RustyCleaverItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Long end = stack.get(ModDataComponents.RUST_UNTIL.get());
        long deadline = end == null ? Long.MIN_VALUE : end;
        Level level = context.level();
        long now = level == null ? Long.MAX_VALUE : level.getGameTime();

        if (deadline != Long.MIN_VALUE && now < deadline) {
            int seconds = (int) Math.ceil((deadline - now) / 20.0D);
            tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".rusty_cleaver.tooltip.remain", seconds)
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".rusty_cleaver.tooltip.ready")
                    .withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".rusty_cleaver.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}