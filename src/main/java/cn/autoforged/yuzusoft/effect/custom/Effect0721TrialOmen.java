package cn.autoforged.yuzusoft.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * "0721试炼之兆"：持有 0721 不祥之兆的玩家靠近试炼刷怪笼时，由
 * {@code TrialSpawnerMixin} 转换获得（镜像原版 不祥之兆→试炼之兆 的转换链路）。
 *
 * <p>与原版试炼之兆互斥：生效期间持续清除原版 不祥之兆/试炼之兆，
 * 保证 试炼之兆 / 0721试炼之兆 只会存在其中一种。
 */
public class Effect0721TrialOmen extends MobEffect {

    public Effect0721TrialOmen(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 与原版兆头互斥：持有 0721 试炼之兆期间，原版 不祥之兆/试炼之兆 会被持续清除
        entity.removeEffect(MobEffects.BAD_OMEN);
        entity.removeEffect(MobEffects.TRIAL_OMEN);
        return true;
    }
}
