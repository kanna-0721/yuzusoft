package cn.autoforged.yuzusoft.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * "0721英雄" 效果。
 *
 * <p>击败 0721 袭击后由 {@code RaidMixin} 代替原版村庄英雄施加。
 * 交易折扣逻辑在 {@code VillagerMixin#updateSpecialPrices} 中实现：
 * <ul>
 *   <li>消耗绿宝石的交易：绿宝石数量 ≤5→1、6~15→2、&gt;15→7</li>
 *   <li>用原材料换绿宝石的交易：原材料消耗量减半</li>
 * </ul>
 * 效果本身无需每 tick 行为。
 */
public class Effect0721Hero extends MobEffect {

    public Effect0721Hero(MobEffectCategory category, int color) {
        super(category, color);
    }
}
