package cn.autoforged.yuzusoft.charm;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.core.particles.ParticleTypes;

/**
 * 魅惑状态效果。
 * 中性类别、紫红色；环境粒子直接用原版爱心粒子；添加时播放经验球音效（复用原版音效）。
 * 行为逻辑（跟随/攻击/不消失/不进食回复）全部由 {@link CharmEvents} 与目标 AI 驱动。
 */
public class CharmMobEffect extends MobEffect {
    public CharmMobEffect() {
        super(MobEffectCategory.NEUTRAL, CharmRegistry.CHARM_COLOR, ParticleTypes.HEART);
        this.withSoundOnAdded(SoundEvents.EXPERIENCE_ORB_PICKUP);
    }
}
