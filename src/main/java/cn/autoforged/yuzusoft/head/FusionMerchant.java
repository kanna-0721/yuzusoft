package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.entity.custom.GuardianTraderEntity;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import javax.annotation.Nullable;

/**
 * 「芳乃头」融合生物的交易对象。
 * <p>
 * 身体（僵尸、骷髅……）本身不是 {@code Merchant}，无法在运行时给它加接口，
 * 故用这个包装类把 {@link Merchant} 的默认 {@code openTradingScreen} 借给融合体：
 * 报价直接复用芳乃原生物 {@link GuardianTraderEntity#createOffers()} 的那一套。
 * 每个融合体持有一份（见 {@link cn.autoforged.yuzusoft.component.ModAttachments#FUSION_MERCHANT}），
 * 交易次数在多次开关界面之间保持。
 */
public final class FusionMerchant implements Merchant {
    private final Entity owner;
    private final MerchantOffers offers = GuardianTraderEntity.createOffers();
    @Nullable
    private Player tradingPlayer;

    public FusionMerchant(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.owner.level().playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                ModSounds.GUARDIANS_TRADE.get(), this.owner.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (!this.owner.level().isClientSide) {
            this.owner.level().playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                    ModSounds.GUARDIANS_IDLE.get(), this.owner.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.GUARDIANS_TRADE.get();
    }

    @Override
    public boolean isClientSide() {
        return this.owner.level().isClientSide;
    }
}