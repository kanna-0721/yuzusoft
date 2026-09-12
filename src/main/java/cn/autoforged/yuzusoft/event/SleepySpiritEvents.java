package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.SleepySpiritEntity;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class SleepySpiritEvents {

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.updateLevel()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        List<SleepySpiritEntity> spirits = level.getEntitiesOfClass(
                SleepySpiritEntity.class,
                player.getBoundingBox().inflate(16.0),
                SleepySpiritEntity::isSleeping);
        for (SleepySpiritEntity spirit : spirits) {
            spirit.wakeUpAndGift(player);
        }
    }
}