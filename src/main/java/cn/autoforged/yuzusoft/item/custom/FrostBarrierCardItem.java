package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 冰霜屏障卡片：使用后在玩家周围生成不可见屏障（4 格半径），持续 300 tick，
 * 期间推开并驱散靠近的生物。不可堆叠，具备 10 点耐久——每次使用消耗 1 点，耗尽即损毁。
 * 使用后进入 600 tick 冷却。
 */
public class FrostBarrierCardItem extends Item {
    public static final int BARRIER_DURATION_TICKS = 300;
    public static final int USE_COOLDOWN_TICKS = 600;
    public static final int MAX_DURABILITY = 10;

    public FrostBarrierCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(ModEffects.FROST_BARRIER,
                BARRIER_DURATION_TICKS, 0, false, false, true));
            player.getCooldowns().addCooldown(stack.getItem(), USE_COOLDOWN_TICKS);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                // 每次使用消耗 1 点耐久（非创造模式下），耗尽即损毁
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}