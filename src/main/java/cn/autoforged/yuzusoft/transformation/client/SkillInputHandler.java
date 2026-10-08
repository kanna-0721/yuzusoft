package cn.autoforged.yuzusoft.transformation.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.network.payload.CastSkillPayload;
import cn.autoforged.yuzusoft.transformation.SkillSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端左键处理器：变身成拥有技能的生物且手持变身法杖时，
 * 拦截普通攻击（左键）并改为向服务端发送"释放技能"请求，其余情况保持原版行为。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public final class SkillInputHandler {
    /** 连按限流：最多每 3 刻发一次请求，真实冷却由服务端判定。 */
    private static final int SEND_INTERVAL = 3;
    private static int lastSendTick = -100;

    private SkillInputHandler() {}

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!SkillSystem.hasSkill(ClientTransformation.get(player.getUUID()))) return;
        if (!holdsWand(player)) return;

        // 取消普通攻击（攻击实体 / 破坏方块），改为释放技能
        event.setCanceled(true);

        int tick = player.tickCount;
        if (tick < lastSendTick) lastSendTick = -100;
        if (tick - lastSendTick < SEND_INTERVAL) return;
        lastSendTick = tick;
        PacketDistributor.sendToServer(new CastSkillPayload());
    }

    private static boolean holdsWand(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(ModItems.TRANSFORMATION_WAND.get()) || off.is(ModItems.TRANSFORMATION_WAND.get());
    }
}