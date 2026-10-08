package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class Effect0721DamageHandler {
    private static final float MAX_MULTIPLIER = 2.0F;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        MobEffectInstance effectInstance = entity.getEffect(ModEffects.EFFECT_0721);
        if (effectInstance == null) return;

        int amplifier = effectInstance.getAmplifier();
        float multiplier = 1.0f + (amplifier + 1) * (MAX_MULTIPLIER - 1.0f) / 2.0f;

        event.setNewDamage(event.getNewDamage() * multiplier);
    }
}