package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity;
import cn.autoforged.yuzusoft.network.payload.ChestplateAbilityPayload;
import cn.autoforged.yuzusoft.network.payload.DismountPayload;
import cn.autoforged.yuzusoft.network.payload.MountPayload;
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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
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

