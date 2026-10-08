package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.client.DecapitatedBodyRenderHelper;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.config.ModConfig;
import cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity;
import cn.autoforged.yuzusoft.network.payload.ChestplateAbilityPayload;
import cn.autoforged.yuzusoft.network.payload.DismountPayload;
import cn.autoforged.yuzusoft.network.payload.MountPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 检测 J 键按下并发送胸甲技能请求到服务端；R/X 键用于骑乘/脱离（乃爱本体或头部为乃爱的融合体）；
 * 并在渲染无头躯体时隐藏模型头部。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    /** 启动后是否已强制应用过一次配置键位（options.txt 旧值恢复发生在 setup 之后，只能在这里兜底）。 */
    private static boolean descendKeyForced = false;

    /**
     * 无头躯体是「真实生物实体 + 隐藏头部」，所以渲染前后临时把头部部件设为不可见。
     * 原版与 yuzusoft 的模型都能被 {@link DecapitatedBodyRenderHelper#headParts} 命中。
     */
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!event.getEntity().hasData(ModAttachments.DECAPITATED_BODY)) {
            return;
        }
        DecapitatedBodyRenderHelper.setVisible(
                DecapitatedBodyRenderHelper.headParts(event.getRenderer()), false);
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!event.getEntity().hasData(ModAttachments.DECAPITATED_BODY)) {
            return;
        }
        DecapitatedBodyRenderHelper.setVisible(
                DecapitatedBodyRenderHelper.headParts(event.getRenderer()), true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // 首帧强制应用配置里的下降键，覆盖 options.txt 恢复的旧绑定（如旧版本遗留的 X 键）。
        // 不依赖玩家是否进入世界：标题界面第一 tick 就执行，早于玩家查看按键设置。
        if (!descendKeyForced) {
            descendKeyForced = true;
            ModKeyMappings.DUALFORM_DESCEND_KEY.get()
                .setKey(InputConstants.getKey(ModConfig.DUALFORM_DESCEND_KEY.get()));
        }

        if (mc.player == null) {
            return;
        }

        while (ModKeyMappings.CHESTPLATE_ABILITY_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(new ChestplateAbilityPayload());
        }

        while (ModKeyMappings.MOUNT_KEY.get().consumeClick()) {
            if (mc.player.isPassenger()) continue;
            Mob target = findNearestRideable(mc.player.position(), 5.0);
            if (target != null) {
                PacketDistributor.sendToServer(new MountPayload(target.getId()));
            }
        }

        while (ModKeyMappings.DISMOUNT_KEY.get().consumeClick()) {
            if (!mc.player.isPassenger()) continue;
            if (isRideable(mc.player.getVehicle())) {
                PacketDistributor.sendToServer(new DismountPayload());
            }
        }
    }

    /** 找附近可骑乘的目标：乃爱本体，或头部为乃爱的融合体。 */
    private static Mob findNearestRideable(Vec3 pos, double range) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        AABB area = new AABB(pos.x - range, pos.y - range, pos.z - range,
                pos.x + range, pos.y + range, pos.z + range);
        List<Mob> mobs = mc.level.getEntitiesOfClass(Mob.class, area);

        Mob nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Mob mob : mobs) {
            if (!isRideable(mob)) continue;
            double dist = mob.position().distanceToSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = mob;
            }
        }
        return nearest;
    }

    /** 该实体当前是否可骑乘（乃爱本体不处于愤怒状态，或头部为乃爱的融合体）。 */
    private static boolean isRideable(@Nullable Entity entity) {
        if (entity instanceof DualFormMobEntity dual) {
            return !dual.isAngry();
        }
        return entity instanceof Mob mob
                && Boolean.TRUE.equals(mob.getExistingDataOrNull(ModAttachments.FUSION_RIDEABLE));
    }
}

