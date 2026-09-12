package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class ModGameEvents {
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.hasEffect(ModEffects.STUN)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().is(ModEffects.STUN)) {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.STUN_EXPIRE.get(), entity.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }
}
