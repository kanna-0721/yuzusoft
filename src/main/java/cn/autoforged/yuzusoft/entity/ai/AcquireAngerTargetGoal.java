package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

/**
 * 愤怒时若目标丢失（例如攻击者跑远/换个玩家来骚扰），
 * 在愤怒持续期内就近寻找一个玩家重新锁定。
 */
public class AcquireAngerTargetGoal extends NearestAttackableTargetGoal<Player> {

    private final WaterSpiritEntity spirit;

    public AcquireAngerTargetGoal(WaterSpiritEntity spirit) {
        super(spirit, Player.class, true, (mob) ->
        mob instanceof Player player && !player.isSpectator() && !player.getAbilities().instabuild);
        this.spirit = spirit;
    }

    @Override
    public boolean canUse() {
        // 仅在愤怒状态下索敌；默认中立，不会主动攻击
        return this.spirit.isAngry() && super.canUse();
    }
}
