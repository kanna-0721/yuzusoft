package cn.autoforged.yuzusoft.transformation.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.network.payload.ClimbPayload;
import cn.autoforged.yuzusoft.transformation.TransformationTraits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端攀爬上报器。
 * <p>
 * 只有客户端知道自己是否"贴着墙按着移动键"，故攀爬期间每刻上报一次，
 * 停止时补发一次 {@code false}；断线时会话终止，服务端的攀爬窗口 4 刻后自然过期。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public final class ClimbInputHandler {
    /** 上一次上报给服务端的状态是否为"攀爬中"。 */
    private static boolean reported;

    private ClimbInputHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean climbing = player != null && isClimbing(player);
        if (player == null) {
            reported = false;
            return;
        }
        if (!climbing && !reported) return; // 没在爬、也没上报过：保持安静
        PacketDistributor.sendToServer(new ClimbPayload(climbing));
        reported = climbing;
    }

    /** 贴着墙 + 按着前进/侧移 + 未潜行 + 当前形态是蜘蛛类。 */
    private static boolean isClimbing(LocalPlayer player) {
        if (player.isShiftKeyDown()) return false;
        if (player.zza <= 0.0F && player.xxa == 0.0F) return false;
        if (!player.horizontalCollision) return false;
        String id = ClientTransformation.get(player.getUUID());
        if (id == null) return false;
        return TransformationTraits.canClimb(EntityType.byString(id).orElse(null));
    }
}