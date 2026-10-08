package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "0721英雄" 交易折扣（挂 {@link Villager}）。
 *
 * <p>玩家带着 0721英雄 打开交易（{@code Villager#updateSpecialPrices} 调用点）时，
 * 在原版声望/英雄折扣之后追加 0721 折扣，全部作用于 costA 的
 * {@code specialPriceDiff}（原版绿宝石只出现在 costA 或 result，costB 无需处理）：
 * <ul>
 *   <li>用原材料换绿宝石（result 为绿宝石）：costA 原材料数量减半（至少 1）</li>
 *   <li>消耗绿宝石（costA 为绿宝石）：≤5→1、6~15→2、&gt;15→7</li>
 * </ul>
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {

    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void yz0721$applyHeroDiscount(Player player, CallbackInfo ci) {
        if (!player.hasEffect(ModEffects.EFFECT_0721_HERO)) {
            return;
        }
        for (MerchantOffer offer : ((Villager) (Object) this).getOffers()) {
            if (offer.getResult().is(Items.EMERALD)) {
                // 原材料换绿宝石：所有原材料消耗减半（下限 1）
                int base = offer.getBaseCostA().getCount();
                int half = Math.max(1, base / 2);
                offer.addToSpecialPriceDiff(-(base - half));
            } else if (offer.getBaseCostA().is(Items.EMERALD)) {
                // 消耗绿宝石：分档降价
                int count = offer.getBaseCostA().getCount();
                int target = count <= 5 ? 1 : (count <= 15 ? 2 : 7);
                offer.addToSpecialPriceDiff(-(count - target));
            }
        }
    }
}
