package cn.autoforged.yuzusoft.transformation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拟态锁敌判定：判断某个生物是否会主动把"变身为指定生物"的玩家当作攻击目标。
 * <p>
 * 做法是直接读取该生物目标选择器里的 {@link NearestAttackableTargetGoal} 目标类型，
 * 与变身生物的实体类比对，从而自动复刻原版规则：
 * 僵尸的目标表含 Villager/IronGolem，骷髅只含 Player/IronGolem，
 * 于是"变村民 → 僵尸会打、骷髅不打"，"变铁傀儡 → 两者都会打"。
 * <p>
 * 注意：不能使用 {@link EntityType#getBaseClass()}（它恒返回 {@link Entity}.class），
 * 必须通过实例化取得真实实体类。并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationTargeting {
    private static final Field GOAL_TARGET_TYPE = findField(NearestAttackableTargetGoal.class, "targetType");
    private static final Map<EntityType<?>, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();

    private TransformationTargeting() {}

    /** 该生物是否会主动锁定"变身为 disguised 的玩家"。 */
    public static boolean wouldTarget(Mob mob, EntityType<?> disguised, Level level) {
        if (GOAL_TARGET_TYPE == null) return true; // 读不到目标表时放行，避免把生物全部变成"和平模式"
        Class<?> base = entityClass(disguised, level);
        if (base == null) return true; // 取不到真实类时放行，避免误伤
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            if (!(goal instanceof NearestAttackableTargetGoal<?>)) continue;
            Class<?> type = goalTargetType(goal);
            if (type != null && type.isAssignableFrom(base)) return true;
        }
        return false;
    }

    /** 取得实体类型对应的真实实体类（实例化一次并缓存）。 */
    static Class<?> entityClass(EntityType<?> type, Level level) {
        Optional<Class<?>> cached = CLASS_CACHE.get(type);
        if (cached != null) return cached.orElse(null);
        Class<?> result = null;
        try {
            Entity entity = type.create(level);
            if (entity != null) result = entity.getClass();
        } catch (Throwable ignored) {
            // 实例化失败时保持 null，交由上层放行
        }
        CLASS_CACHE.put(type, Optional.ofNullable(result));
        return result;
    }

    private static Class<?> goalTargetType(Goal goal) {
        try {
            return (Class<?>) GOAL_TARGET_TYPE.get(goal);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            return null;
        }
    }
}