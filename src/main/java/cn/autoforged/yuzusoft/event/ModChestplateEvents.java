package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 浮游使者胸甲被动效果：
 * - 飘浮免疫（默认开启，set_bonus）
 * - 跳跃提升 I（set_bonus）
 * - 攻击命中目标施加飘浮（effect_on_attack）
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class ModChestplateEvents {

    private static final ResourceLocation JUMP_BOOST_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "sentinel_jump_boost");
    private static final double JUMP_BOOST_LEVEL = 0.1; // 跳跃提升 I

    private static boolean isWearingChestplate(net.minecraft.world.entity.LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
    }

    /**
     * 飘浮免疫：穿戴胸甲时拒绝飘浮效果。
     */
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player
                && isWearingChestplate(event.getEntity())
                && event.getEffectInstance() != null
                && event.getEffectInstance().is(MobEffects.LEVITATION)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /**
     * 跳跃提升 I：装备/卸下胸甲时增删 JUMP_STRENGTH 修饰符。
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        AttributeInstance jumpStrength = event.getEntity().getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpStrength == null) {
            return;
        }
        boolean wearing = event.getTo().is(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
        boolean hasModifier = jumpStrength.hasModifier(JUMP_BOOST_MODIFIER_ID);
        if (wearing && !hasModifier) {
            jumpStrength.addTransientModifier(
                    new AttributeModifier(JUMP_BOOST_MODIFIER_ID, JUMP_BOOST_LEVEL, AttributeModifier.Operation.ADD_VALUE));
        } else if (!wearing && hasModifier) {
            jumpStrength.removeModifier(JUMP_BOOST_MODIFIER_ID);
        }
    }

    /**
     * effect_on_attack：玩家穿着胸甲近战命中目标时，目标获得飘浮 100 ticks。
     */
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player
                && isWearingChestplate(player)
                && !event.getEntity().level().isClientSide()) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
        }
    }
}

