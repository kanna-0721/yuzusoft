package cn.autoforged.yuzusoft.transformation;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 周围生物对变身形态的反应。
 * <p>
 * 复刻原版的躲避行为：若某生物本身有 {@link AvoidEntityGoal}（苦力怕躲猫/豹猫、骷髅躲狼、
 * 狐狸躲狼与北极熊等），就给它的目标表追加一条针对"形态匹配该类别"的玩家的躲避目标，
 * 于是变成猫靠近苦力怕，苦力怕就会逃跑。并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationReactions {
    private static final String PATCHED_KEY = CycloneSwordMod.MODID + ":disguised_avoid";
    private static final String TARGETING_KEY = CycloneSwordMod.MODID + ":disguised_targeting";
    private static final Field AVOID_CLASS = findField(AvoidEntityGoal.class, "avoidClass");
    private static final Field MAX_DIST = findField(AvoidEntityGoal.class, "maxDist");
    private static final Field TARGET_CONDITIONS = findField(NearestAttackableTargetGoal.class, "targetConditions");
    private static final Field CONDITIONS_SELECTOR = findField(TargetingConditions.class, "selector");

    private TransformationReactions() {}

    /** 在生物加入世界时追加"躲避变身形态"的目标（用持久数据标记，避免重复注入）。 */
    public static void patchAvoid(PathfinderMob mob) {
        if (AVOID_CLASS == null || MAX_DIST == null) return;
        if (mob.getPersistentData().getBoolean(PATCHED_KEY)) return;
        List<Class<?>> avoided = new ArrayList<>();
        float maxDist = 0.0F;
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            if (!(goal instanceof AvoidEntityGoal<?>)) continue;
            Class<?> cls = readClass(goal);
            if (cls == null || cls == Player.class) continue;
            avoided.add(cls);
            maxDist = Math.max(maxDist, readFloat(goal));
        }
        if (avoided.isEmpty()) return;
        mob.getPersistentData().putBoolean(PATCHED_KEY, true);
        mob.goalSelector.addGoal(3, new AvoidEntityGoal<>(mob, Player.class, maxDist <= 0.0F ? 6.0F : maxDist, 1.0, 1.2,
                (LivingEntity entity) -> entity instanceof Player player && mimicsAvoided(player, avoided)));
    }

    /**
     * 在生物加入世界时，给它的每个 {@link NearestAttackableTargetGoal} 追加"拟态过滤"。
     * <p>
     * 只取消 {@code LivingChangeTargetEvent} 是不够的：{@link NearestAttackableTargetGoal} 的
     * {@code targetType} 是 {@code Player.class}，而变身玩家仍是 {@link Player} 实例，
     * 因此它每 10 tick 仍会把玩家"选中"，随后 {@code start()} 里的 {@code setTarget} 被取消，
     * 但 goal 已经启动并占用了 {@code Flag.TARGET} 目标槽位——高优先级的玩家目标会顶掉
     * 低优先级的村民/铁傀儡目标，却又因为没有真正设上目标而立刻停摆，导致目标反复清空重取
     * （表现为生物"兜兜转转"、不如创造模式直接）。
     * <p>
     * 这里直接在"选择阶段"把"本就不会攻击其形态"的变身玩家从候选里剔除，
     * 于是 goal 从不启动、不占槽位，生物对其它目标的行为与创造模式/未变身时完全一致。
     */
    public static void patchTargeting(Mob mob) {
        if (TARGET_CONDITIONS == null || CONDITIONS_SELECTOR == null) return;
        if (mob.getPersistentData().getBoolean(TARGETING_KEY)) return;
        boolean any = false;
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> goal) {
                any |= filterTargetGoal(mob, goal);
            }
        }
        if (any) mob.getPersistentData().putBoolean(TARGETING_KEY, true);
    }

    /** 给单个目标 goal 的 TargetingConditions 套上一层"排除不可锁定的拟态玩家"的谓词。 */
    @SuppressWarnings("unchecked")
    private static boolean filterTargetGoal(Mob mob, Goal goal) {
        try {
            Object conditions = TARGET_CONDITIONS.get(goal);
            if (!(conditions instanceof TargetingConditions targeting)) return false;
            Predicate<LivingEntity> original = (Predicate<LivingEntity>) CONDITIONS_SELECTOR.get(targeting);
            targeting.selector(candidate -> {
                if (original != null && !original.test(candidate)) return false;
                return !shouldIgnore(mob, candidate);
            });
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** 该候选目标是否为"变身为本形态后、该生物本来不会主动攻击"的玩家。 */
    private static boolean shouldIgnore(Mob mob, LivingEntity candidate) {
        if (!(candidate instanceof Player player)) return false;
        EntityType<?> disguised = TransformationSystem.currentType(player);
        if (disguised == null) return false; // 未变身，保持原版行为
        if (player.isCreative() || player.isSpectator()) return true;
        return !TransformationTargeting.wouldTarget(mob, disguised, mob.level());
    }

    /** 玩家当前形态的真实实体类是否落在该生物原本要躲避的类别里。 */
    private static boolean mimicsAvoided(Player player, List<Class<?>> avoided) {
        EntityType<?> type = TransformationSystem.currentType(player);
        if (type == null) return false;
        Class<?> cls = TransformationTargeting.entityClass(type, player.level());
        if (cls == null) return false;
        for (Class<?> candidate : avoided) {
            if (candidate.isAssignableFrom(cls)) return true;
        }
        return false;
    }

    private static Class<?> readClass(Goal goal) {
        try {
            return (Class<?>) AVOID_CLASS.get(goal);
        } catch (Throwable t) {
            return null;
        }
    }

    private static float readFloat(Goal goal) {
        try {
            return MAX_DIST.getFloat(goal);
        } catch (Throwable t) {
            return 0.0F;
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