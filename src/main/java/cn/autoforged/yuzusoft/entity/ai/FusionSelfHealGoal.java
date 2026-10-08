package cn.autoforged.yuzusoft.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 融合生物的自身周期回复：照抄头部原生物（乃爱每 20 刻回 2 点血）。
 * <p>
 * 不占控制旗标且 {@code canUse} 恒真，作为常驻行为一直运行。
 */
public class FusionSelfHealGoal extends Goal {
    private final Mob mob;
    private final float amount;
    private final int interval;

    public FusionSelfHealGoal(Mob mob, float amount, int interval) {
        this.mob = mob;
        this.amount = amount;
        this.interval = interval;
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
        if (this.mob.level().isClientSide) {
            return;
        }
        if (this.mob.tickCount % this.interval != 0) {
            return;
        }
        if (this.mob.getHealth() > 0.0F && this.mob.getHealth() < this.mob.getMaxHealth()) {
            this.mob.heal(this.amount);
        }
    }
}