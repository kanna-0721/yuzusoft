package cn.autoforged.yuzusoft.transformation.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端持有的变身状态：玩家 UUID -> 目标生物 id，以及复用的渲染模型实例。
 * 仅客户端使用，服务端不会加载本类。并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class ClientTransformation {
    private static final Map<UUID, String> STATES = new HashMap<>();
    private static final Map<UUID, Entity> MODELS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_TICK = new HashMap<>();

    private ClientTransformation() {}

    /** 由网络包调用；entityId 为空或 minecraft:player 表示解除变身。 */
    public static void set(UUID playerId, String entityId) {
        if (entityId == null || entityId.isEmpty() || entityId.equals("minecraft:player")) {
            STATES.remove(playerId);
            MODELS.remove(playerId);
            LAST_TICK.remove(playerId);
            return;
        }
        String previous = STATES.put(playerId, entityId);
        if (!entityId.equals(previous)) MODELS.remove(playerId);
    }

    public static String get(UUID playerId) {
        return STATES.get(playerId);
    }

    public static void clear() {
        STATES.clear();
        MODELS.clear();
        LAST_TICK.clear();
    }

    /** 取得该玩家用于渲染的实体实例，按需创建并缓存。 */
    public static Entity model(UUID playerId, EntityType<?> type, Level level) {
        Entity cached = MODELS.get(playerId);
        if (cached != null && cached.getType() == type && cached.level() == level) return cached;
        Entity created = type.create(level);
        if (created == null) {
            MODELS.remove(playerId);
            return null;
        }
        MODELS.put(playerId, created);
        return created;
    }

    /** 每游戏刻只返回一次 true，用于按刻驱动模型的行走动画。 */
    public static boolean beginTick(UUID playerId, int tickCount) {
        Integer last = LAST_TICK.get(playerId);
        if (last != null && last == tickCount) return false;
        LAST_TICK.put(playerId, tickCount);
        return true;
    }
}