package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.config.YuzusoftConfig;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * 自然刷怪门控：YuzusoftConfig.ENABLE_NATURAL_SPAWNS 关闭时，
 * 屏蔽 yuzusoft 实体的一切自然生成（自然/结构/巡逻/刷怪笼等），
 * 刷怪蛋与 /summon 指令仍可用。
 */
public class NaturalSpawnGate {

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (YuzusoftConfig.ENABLE_NATURAL_SPAWNS.get()) {
            return;
        }
        Mob mob = event.getEntity();
        if (!isYuzusoftMonster(mob)) {
            return;
        }
        // 刷怪蛋 / 指令生成保持可用
        MobSpawnType type = event.getSpawnType();
        if (type == MobSpawnType.SPAWN_EGG || type == MobSpawnType.COMMAND) {
            return;
        }
        event.setSpawnCancelled(true);
    }

    /** 仅门控 yuzusoft 的 MONSTER 类别实体；友好/中立（CREATURE 等）不受影响。 */
    private static boolean isYuzusoftMonster(Mob mob) {
        return mob.getType().getCategory() == MobCategory.MONSTER
                && ModEntities.ENTITY_TYPES.getEntries().stream()
                        .anyMatch(e -> e.value() == mob.getType());
    }
}
