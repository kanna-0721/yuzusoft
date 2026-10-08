package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.head.HeadAbilityTable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

/**
 * 融合生物的周期光环：照抄头部原生物的定时扫描行为。
 * <p>
 * 两个来源：七海（除自己外所有存活实体，4 格内生命恢复 II / 8 格内 I）、
 * Evil 七海（只作用于 0721 族员，8 格内生命恢复 II / 16 格内 I）。
 * <p>
 * 不占任何控制旗标且 {@code canUse} 恒真——按 {@code GoalSelector} 的规则，
 * 这类 goal 会一直运行，且不会与移动 / 注视类 goal 抢旗标。
 */
public class FusionAuraGoal extends Goal {
    private final Mob mob;
    private final HeadAbilityTable.Aura aura;

    public FusionAuraGoal(Mob mob, HeadAbilityTable.Aura aura) {
        this.mob = mob;
        this.aura = aura;
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
        if (this.mob.tickCount % this.aura.interval() != 0) {
            return;
        }
        AABB area = this.mob.getBoundingBox().inflate(this.aura.outerRadius());
        for (LivingEntity entity : this.mob.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == this.mob || !entity.isAlive()) {
                continue;
            }
            if (this.aura.groupOnly() && !Group0721Helper.isGroup0721(entity)) {
                continue;
            }
            double distance = entity.distanceTo(this.mob);
            int amplifier = distance <= this.aura.innerRadius()
                    ? this.aura.innerAmplifier() : this.aura.outerAmplifier();
            entity.addEffect(new MobEffectInstance(this.aura.effect(), this.aura.duration(), amplifier,
                    false, false, true));
        }
    }
}