package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.community.LimelightHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * limelight 族群成员之间完全免疫伤害。
 *
 * <p>仅靠索敌排除同族只能挡住近战锁定，挡不住"远程弹射物飞行途中误撞同伴"与
 * "范围攻击溅射到同伴"。这里在伤害结算最前（HIGHEST）拦截：只要受害者是族员，
 * 且伤害的造成者（弹射物则取其发射者）也是族员，就取消该次伤害。</p>
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class LimelightFriendlyFireHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!LimelightHelper.isLimelight(victim)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) {
            attacker = event.getSource().getDirectEntity();
        }
        if (attacker instanceof LivingEntity living
                && living != victim
                && LimelightHelper.isLimelight(living)) {
            event.setCanceled(true);
        }
    }
}