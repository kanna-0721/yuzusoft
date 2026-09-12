package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.config.ModConfig;
import cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity;
import cn.autoforged.yuzusoft.network.payload.ChestplateAbilityPayload;
import cn.autoforged.yuzusoft.network.payload.DismountPayload;
import cn.autoforged.yuzusoft.network.payload.MountPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 检测 J 键按下并发送胸甲技能请求到服务端；R/X 键用于骑乘/脱离白雪乃爱。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    /** 启动后是否已强制应用过一次配置键位（options.txt 旧值恢复发生在 setup 之后，只能在这里兜底）。 */
    private static boolean descendKeyForced = false;

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
            Entity target = findNearestDualFormMob(mc.player.position(), 5.0);
            if (target instanceof DualFormMobEntity mob && !mob.isAngry()) {
                PacketDistributor.sendToServer(new MountPayload(target.getId()));
            }
        }

        while (ModKeyMappings.DISMOUNT_KEY.get().consumeClick()) {
            if (mc.player.isPassenger() && mc.player.getVehicle() instanceof DualFormMobEntity) {
                PacketDistributor.sendToServer(new DismountPayload());
            }
        }
    }

    private static Entity findNearestDualFormMob(Vec3 pos, double range) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        AABB area = new AABB(pos.x - range, pos.y - range, pos.z - range,
                pos.x + range, pos.y + range, pos.z + range);
        List<DualFormMobEntity> mobs = mc.level.getEntitiesOfClass(DualFormMobEntity.class, area);

        Entity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (DualFormMobEntity mob : mobs) {
            double dist = mob.position().distanceToSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = mob;
            }
        }
        return nearest;
    }
}

