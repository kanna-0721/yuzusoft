package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.head.FusionSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 融合生物音效：让融合体播「头部」对应原生物的音效。
 * <p>
 * 融合体的实体类型来自身体，自身 {@code getHurtSound} 等拿到的是身体的音效；
 * 组装时身体已被 {@code setSilent(true)}（见
 * {@link cn.autoforged.yuzusoft.head.FusionAssembler}），这里再按 HEAD 槽里的头颅
 * 播头部音效——环境音 / 受击音 / 死亡音三类。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public final class FusionSoundEvents {
    private FusionSoundEvents() {}

    /** 环境音触发概率分母：约每 240 tick（12 秒）一次，与原版 ambientSoundTime 机制同量级。 */
    private static final int AMBIENT_CHANCE = 240;

    /** 环境音：身体自身的已随 setSilent 静音，这里按头部补播。 */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) {
            return;
        }
        if (!isFusion(mob)) {
            return;
        }
        if (mob.getRandom().nextInt(AMBIENT_CHANCE) == 0) {
            FusionSounds.playAmbient(mob);
        }
    }

    /** 受击音：改用 LivingDamageEvent.Post，拿到的是护甲/减伤后的实际伤害。 */
    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || event.getNewDamage() <= 0.0F) {
            return;
        }
        if (isFusion(entity)) {
            FusionSounds.playHurt(entity);
        }
    }

    /** 死亡音。 */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (isFusion(entity)) {
            FusionSounds.playDeath(entity);
        }
    }

    private static boolean isFusion(LivingEntity entity) {
        return Boolean.TRUE.equals(entity.getExistingDataOrNull(ModAttachments.FUSION_BODY));
    }
}