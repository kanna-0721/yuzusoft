package cn.autoforged.yuzusoft.charm.ai;

import cn.autoforged.yuzusoft.charm.CharmHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 契约守护目标：本身不做任何行为，只在魅惑契约存在时挂起，
 * 每刻检查"契约还在、效果已消失"（时限到期、奶桶、/effect clear、主人死亡等），
 * 一旦效果消失就释放契约，生物恢复原本 AI 与消失规则。
 */
public class CharmStateGoal extends Goal {
    private final Mob mob;

    public CharmStateGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return CharmHelper.getState(this.mob) != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        CharmHelper.checkRelease(this.mob);
    }
}
