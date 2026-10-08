package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * 低水位近战：愤怒且水位低于 0.2（不足发射水弹）时改为近战攻击（用户自定义创意）。
 */
public class WaterSpiritMeleeGoal extends MeleeAttackGoal {

    private final WaterSpiritEntity spirit;

    public WaterSpiritMeleeGoal(WaterSpiritEntity spirit) {
        super(spirit, 1.25D, true);
        this.spirit = spirit;
    }

    @Override
    public boolean canUse() {
        // 不走 super.canUse()：MeleeAttackGoal 内含 navigation.canReach 门槛，
        // 对水中目标常返回 false 导致近战永不启动（表现为丢失仇恨站桩）
        LivingEntity target = this.spirit.getTarget();
        return WaterSpiritConfig.MELEE_WHEN_LOW_WATER.get()
                && this.spirit.isAngry()
                && !this.spirit.canShootMore()
                && target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return WaterSpiritConfig.MELEE_WHEN_LOW_WATER.get()
                && this.spirit.isAngry()
                && !this.spirit.canShootMore()
                && super.canContinueToUse();
    }
}
