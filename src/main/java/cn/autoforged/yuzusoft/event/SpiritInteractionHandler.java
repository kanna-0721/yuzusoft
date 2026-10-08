package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 取水交互入口：Mob#interact 在 1.21.1 是 final，改走 NeoForge 玩家交互事件。
 * 玩家手持空桶/玻璃瓶右键水灵时，交给 WaterSpiritEntity#tryTakeWater 结算。
 */
public final class SpiritInteractionHandler {

    private SpiritInteractionHandler() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof WaterSpiritEntity spirit)) {
            return;
        }
        if (event.getItemStack().isEmpty()) {
            return;
        }
        InteractionResult result = spirit.tryTakeWater(event.getEntity(), event.getHand());
        if (result != null) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
