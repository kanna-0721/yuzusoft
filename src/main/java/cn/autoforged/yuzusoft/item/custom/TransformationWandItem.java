package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.network.payload.OpenTransformationListPayload;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * 变身法杖：右键打开变身列表，选择已解锁的生物形态；变身状态下左键释放该生物的专属技能。
 * <p>
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationWandItem extends Item {

    public TransformationWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            var list = serverPlayer.getPersistentData().getList("transformation_unlocked", Tag.TAG_STRING);
            List<String> unlocked = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) unlocked.add(list.getString(i));
            PacketDistributor.sendToPlayer(serverPlayer, new OpenTransformationListPayload(List.copyOf(unlocked)));
        }
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 法杖不可丢弃：Q 键丢弃时直接拦截，物品保留在原槽位。 */
    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return false;
    }
}