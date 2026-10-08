package cn.autoforged.yuzusoft.charm;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 魅惑之瓶：一次性、可堆叠 16。
 * - 手持（主/副手）时实体交互距离 +3 格；
 * - 右键任意"有攻击能力"的生物：与其缔结永久魅惑契约，消耗 1 个；
 * - 已被该玩家永久契约的生物不会再次吞瓶。
 */
public class CharmBottleItem extends Item {
    /** 手持（主/副手）时实体交互距离 +3 格。 */
    public static final ItemAttributeModifiers REACH_MODIFIERS = ItemAttributeModifiers.builder()
            .add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(CharmRegistry.id("charm_bottle_reach"),
                            3.0, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HAND)
            .build();

    public CharmBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Mob mob)) {
            return InteractionResult.PASS;
        }
        if (!CharmHelper.canCharmTarget(mob, player)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        // 永久契约：效果实例写 -1（无限时长），主人 UUID 与永久标记写入实体 NBT
        CharmHelper.applyCharm(mob, player, MobEffectInstance.INFINITE_DURATION);
        player.level().playSound(null, mob, SoundEvents.GENERIC_DRINK, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.4F);
        CharmHelper.celebrate(mob);
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yuzusoft.charm_bottle.tooltip"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
