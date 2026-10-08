package cn.autoforged.yuzusoft.transformation.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.UUID;

/**
 * 客户端渲染钩子：当玩家处于变身状态时，取消原版玩家模型渲染，
 * 改为渲染所选生物对应的实体模型。并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public final class TransformationRenderHandler {
    private static double lastScale = Double.NaN;

    private TransformationRenderHandler() {}

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();
        String id = ClientTransformation.get(uuid);
        if (id == null) return;
        EntityType<?> type = EntityType.byString(id).orElse(null);
        if (type == null) return;
        Level level = player.level();
        Entity model = ClientTransformation.model(uuid, type, level);
        if (model == null) return;

        // 取消原版玩家渲染，改用生物模型
        event.setCanceled(true);

        // 同步位置与朝向，保证模型贴合玩家当前位置
        model.setPos(player.getX(), player.getY(), player.getZ());
        model.setYRot(player.getYRot());
        model.yRotO = player.yRotO;
        model.setXRot(player.getXRot());
        model.xRotO = player.xRotO;
        model.tickCount = player.tickCount;
        if (model instanceof LivingEntity living) {
            living.yBodyRot = player.yBodyRot;
            living.yBodyRotO = player.yBodyRotO;
            living.yHeadRot = player.yHeadRot;
            living.yHeadRotO = player.yHeadRotO;
            living.hurtTime = player.hurtTime;
            living.hurtDuration = player.hurtDuration;
            living.deathTime = player.deathTime;
            living.setSprinting(player.isSprinting());
            living.setShiftKeyDown(player.isShiftKeyDown());
            // 每游戏刻推进一次行走动画，避免逐帧叠加导致摆臂过快
            if (ClientTransformation.beginTick(uuid, player.tickCount)) {
                living.walkAnimation.update(player.walkAnimation.speed(), 1.0F);
            }
        }

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        // poseStack 此时已平移到玩家位置，因此用 (0,0,0) 渲染即可
        dispatcher.render(model, 0.0D, 0.0D, 0.0D, player.getYRot(), event.getPartialTick(),
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    /**
     * 原版 {@code ClientPacketListener.handleUpdateAttributes} 只写入属性值，不会重算实体尺寸，
     * 导致客户端 LocalPlayer 的 eyeHeight 不随 SCALE 变化，相机始终停在 1.62。
     * 这里在 SCALE 实际变动时补一次 refreshDimensions，让第一人称视高跟随变身形态。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        AttributeInstance instance = player.getAttribute(Attributes.SCALE);
        double scale = instance == null ? 1.0D : instance.getValue();
        if (scale == lastScale) return;
        lastScale = scale;
        player.refreshDimensions();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTransformation.clear();
        lastScale = Double.NaN;
    }
}