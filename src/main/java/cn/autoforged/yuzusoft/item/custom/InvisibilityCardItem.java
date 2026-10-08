package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.event.InvisibilityCardEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 隐身卡片（并入自 mod1007 工程）：右键开关隐身。
 * <p>
 * 需至少 10 级经验才能开启，隐身期间持续消耗经验；具体规则见 {@link InvisibilityCardEvents}。
 * 不可堆叠。
 */
public final class InvisibilityCardItem extends Item {
    public InvisibilityCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            InvisibilityCardEvents.toggle(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}