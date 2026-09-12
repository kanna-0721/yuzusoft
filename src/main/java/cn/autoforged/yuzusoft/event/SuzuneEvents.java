package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.SuzuneEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;

/**
 * Suzune（凉音）通用事件：
 * 通过 EntityEvent.Size 在埋地时将碰撞箱缩小到极小，避免在方块内部阻挡玩家。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class SuzuneEvents {

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof SuzuneEntity suzune && suzune.isHidden()) {
            event.setNewSize(SuzuneEntity.HIDDEN_DIMENSIONS);
        }
    }
}
