package cn.autoforged.yuzusoft.charm.ai;

import cn.autoforged.yuzusoft.charm.CharmHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

/**
 * 魅惑期间才生效的自卫反击目标（未魅惑时 canUse 恒为 false，
 * 不给普通生物新增反击行为，契约结束自然恢复原版 AI）。
 */
public class CharmRetaliateGoal extends HurtByTargetGoal {
    private final PathfinderMob mob;

    public CharmRetaliateGoal(PathfinderMob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return CharmHelper.hasCharmEffect(this.mob) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return CharmHelper.hasCharmEffect(this.mob) && super.canContinueToUse();
    }
}
