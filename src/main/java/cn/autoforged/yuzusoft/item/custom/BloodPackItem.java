package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 血包：可存储战斗产生的血量，食用后恢复存储血量并附带等量伤害吸收。
 * 不提供饱食度与饱和度；空血包不可食用；食用后存储清零但物品本身不消失（consume_on_eat = false）。
 */
public class BloodPackItem extends Item {
    public static final int DEFAULT_USE_DURATION = 32;
    public static final int USE_COOLDOWN_TICKS = 20;

    private final float storageEfficiency;
    private final int storageCapacity;
    private final int absorptionDurationTicks;

    public BloodPackItem(Properties properties, float storageEfficiency, int storageCapacity, int absorptionDurationTicks) {
        super(properties);
        this.storageEfficiency = storageEfficiency;
        this.storageCapacity = storageCapacity;
        this.absorptionDurationTicks = absorptionDurationTicks;
    }

    public float getStorageEfficiency() {
        return storageEfficiency;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public static float getStoredHealth(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_HEALTH.get(), 0.0f);
    }

    public static void setStoredHealth(ItemStack stack, float value) {
        stack.set(ModDataComponents.STORED_HEALTH.get(), Math.max(0.0f, value));
    }

    /** 按存储效率将战斗中获得的血量存入血包，超出容量部分丢弃。 */
    public static void addStoredHealth(ItemStack stack, float amount) {
        if (stack.getItem() instanceof BloodPackItem pack) {
            float current = getStoredHealth(stack);
            float max = pack.storageCapacity;
            setStoredHealth(stack, Math.min(max, current + Math.max(0.0f, amount)));
        }
    }

    public static void clearStorage(ItemStack stack) {
        setStoredHealth(stack, 0.0f);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DEFAULT_USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (getStoredHealth(stack) <= 0.0f) {
            // 空血包不可食用
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(stack.getItem()));
        }
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);

        if (!level.isClientSide()) {
            // 释放存储的血量恢复生命
            float stored = getStoredHealth(stack);
            if (stored > 0.0f) {
                livingEntity.heal(stored);
                // 伤害吸收提供的血量 = 血包存储的血量（非固定值）：
                // 先按 amplifier 抬升 MAX_ABSORPTION 至 >= stored，再精确设回 stored
                int amplifier = Math.max(0, (int) Math.ceil(stored / 4.0f) - 1);
                livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionDurationTicks, amplifier));
                livingEntity.setAbsorptionAmount(stored);
            }
            // 清空存储，物品不消失
            clearStorage(stack);

            if (livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(stack.getItem(), USE_COOLDOWN_TICKS);
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        float stored = getStoredHealth(stack);
        tooltipComponents.add(Component.translatable(
                        "tooltip." + CycloneSwordMod.MODID + ".stored_health",
                        String.format("%.1f", stored), storageCapacity)
                .withStyle(ChatFormatting.RED));
    }
}