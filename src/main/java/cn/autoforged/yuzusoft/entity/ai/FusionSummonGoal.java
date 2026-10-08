package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 融合生物的召唤行为：照抄芳乃（{@code GuardianTraderEntity}）——
 * 有活着的可见目标时周期性召唤一只丛雨（{@code GUARDIAN}）。
 * <p>
 * 原生物用「愤怒计时 + 召唤冷却 600 刻」控制；融合体没有愤怒字段，
 * 改为「锁定目标且看得见」作为等价判据，冷却同样取 600 刻。
 * 不占控制旗标，常驻运行。
 */
public class FusionSummonGoal extends Goal {
    private static final int SPAWN_COOLDOWN = 600;

    private final Mob mob;
    private int nextSpawnTick;

    public FusionSummonGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void tick() {
        if (!(this.mob.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.mob.tickCount < this.nextSpawnTick) {
            return;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !this.mob.hasLineOfSight(target)) {
            return;
        }
        EntityType<?> type = ModEntities.GUARDIAN.get();
        Entity summoned = type.create(level);
        if (summoned == null) {
            return;
        }
        summoned.moveTo(this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getYRot(), 0.0F);
        if (!level.addFreshEntity(summoned)) {
            return;
        }
        if (summoned instanceof PathfinderMob pathfinder) {
            pathfinder.setTarget(target);
        }
        this.nextSpawnTick = this.mob.tickCount + SPAWN_COOLDOWN;
    }
}