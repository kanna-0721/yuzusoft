package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * "防爆"效果：免疫爆炸伤害。
 * 判定依据伤害来源是否带 is_explosion 标签，取消该次伤害结算。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class BlastProtectionHandler {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!entity.hasEffect(ModEffects.BLAST_PROTECTION)) return;
        if (!event.getSource().is(DamageTypeTags.IS_EXPLOSION)) return;

        event.setCanceled(true);
    }
}
