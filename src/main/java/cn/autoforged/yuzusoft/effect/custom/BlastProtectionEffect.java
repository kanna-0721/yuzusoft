package cn.autoforged.yuzusoft.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * "防爆"：免疫爆炸伤害。
 * 该类仅作为效果载体（原版 MobEffect 构造器为 protected，无法直接 new），
 * 实际的伤害判定与取消逻辑在 {@code event.BlastProtectionHandler}。
 */
public class BlastProtectionEffect extends MobEffect {

    public BlastProtectionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
