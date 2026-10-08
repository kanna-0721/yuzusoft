package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.worldgen.spawner.ShadowAssassinPatrolSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 阴影刺客巡逻小队驱动：每个服务端 tick 在主线程上驱动一次专属巡逻生成器。
 * 用静态单例保证间隔计数独立，与原版掠夺者 PatrolSpawner 互不影响。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class ShadowAssassinPatrolEvents {

    private static final ShadowAssassinPatrolSpawner SPAWNER = new ShadowAssassinPatrolSpawner();

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        if (!event.getServer().isSameThread()) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 仅在主世界（Overworld）维度生成
            if (Level.OVERWORLD.equals(level.dimension())) {
                SPAWNER.tick(level, true, true);
            }
        }
    }
}