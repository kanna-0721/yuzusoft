package cn.autoforged.yuzusoft.charm.client;

import cn.autoforged.yuzusoft.charm.CharmRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 客户端表现：箭镞紫红染色（复用原版附魔箭模型的 layer0 规则）与魅惑目标的爱心粒子。
 */
public final class CharmClient {
    private CharmClient() {}

    public static void register(IEventBus modBus) {
        modBus.register(new ModBusHandlers());
        NeoForge.EVENT_BUS.register(new GameBusHandlers());
    }

    static class ModBusHandlers {
        @SubscribeEvent
        void itemColors(RegisterColorHandlersEvent.Item event) {
            // 与原版附魔箭同一规则：仅 layer0（箭镞）染成药水色
            event.register((stack, index) -> index == 0
                    ? FastColor.ARGB32.opaque(CharmRegistry.CHARM_COLOR) : -1,
                    CharmRegistry.CHARM_ARROW.get());
        }
    }

    static class GameBusHandlers {
        @SubscribeEvent
        void clientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            net.minecraft.client.multiplayer.ClientLevel level = mc.level;
            if (level == null || level.getGameTime() % 30L != 0L) {
                return;
            }
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof LivingEntity living
                        && living.hasEffect(CharmRegistry.CHARM_EFFECT)
                        && level.random.nextFloat() < 0.3F) {
                    level.addParticle(ParticleTypes.HEART, true,
                            living.getRandomX(0.55),
                            living.getY() + living.getBbHeight() * 0.85 + level.random.nextFloat() * 0.25,
                            living.getRandomZ(0.55),
                            0.0, 0.02, 0.0);
                }
            }
        }
    }
}
