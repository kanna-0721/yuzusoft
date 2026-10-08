package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * "0721不祥之瓶"（1-5 级，等级存于数据组件 {@link ModDataComponents#BOTTLE_0721_LEVEL}）。
 * 与原版不祥之瓶一致：右键后进入 32 tick 的饮用过程（DRINK 动画），饮完才生效——
 * 施加 0721 不祥之兆（8 分钟，放大器 = 等级-1，对应袭击等级 1-5），
 * 生存模式消耗 1 瓶并返还玻璃瓶；仍可堆叠 64。
 */
public class OmensBottle0721Item extends Item {

    public OmensBottle0721Item(Properties properties) {
        super(properties);
    }

    /**
     * 按瓶内等级返回翻译键：item.yuzusoft.omen_0721_bottle_level_1 ~ _5，
     * 使所有瓶子（创造栏 / 巡逻队长掉落）都显示"… I ~ V"等级后缀。
     */
    @Override
    public String getDescriptionId(ItemStack stack) {
        int bottleLevel = Mth.clamp(stack.getOrDefault(ModDataComponents.BOTTLE_0721_LEVEL, 1), 1, 5);
        return super.getDescriptionId() + "_level_" + bottleLevel;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        // 开始饮用（与饮原版药水一致），不立即生效
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            // 等级 1-5 → 0721 不祥之兆放大器 0-4 → Effect0721BadOmen 触发袭击等级 1-5
            int bottleLevel = Mth.clamp(stack.getOrDefault(ModDataComponents.BOTTLE_0721_LEVEL, 1), 1, 5);
            // 与原版兆头互斥：只能存在不祥之兆 / 0721不祥之兆 其中一种
            entity.removeEffect(MobEffects.BAD_OMEN);
            entity.removeEffect(MobEffects.TRIAL_OMEN);
            entity.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721_BAD_OMEN, 4800, bottleLevel - 1));
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            if (stack.getCount() == 1) {
                // 最后一瓶：直接换成玻璃瓶（与饮完原版不祥之瓶一致）
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            stack.shrink(1);
            ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(glassBottle)) {
                player.drop(glassBottle, false);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}
