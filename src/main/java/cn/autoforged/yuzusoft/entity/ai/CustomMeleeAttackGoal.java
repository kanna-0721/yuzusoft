package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.HumanoidCreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class CustomMeleeAttackGoal extends MeleeAttackGoal {
    private final HumanoidCreatureEntity mob;

    public CustomMeleeAttackGoal(HumanoidCreatureEntity mob, double speedMod, boolean seeTarget) {
        super(mob, speedMod, seeTarget);
        this.mob = mob;
    }

    // 攻击前校验，同类直接放弃攻击
    @Override
    protected boolean canPerformAttack(LivingEntity target) {
        // 目标是同类，禁止攻击
        if (target instanceof HumanoidCreatureEntity) return false;
        return super.canPerformAttack(target);
    }
}