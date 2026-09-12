package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.custom.BloodPackItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * 血包存储逻辑：当玩家手持血包进行近战攻击 / 受到伤害 / 远程攻击时，按存储效率将血量存入血包。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class BloodPackEvents {

    /** 近战攻击：玩家造成直接（近战）伤害时，血包存储伤害的一定比例 */
    @SubscribeEvent
    public static void onMeleeAttack(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        DamageSource source = event.getSource();
        if (!source.isDirect()) return;
        if (!(source.getEntity() instanceof Player player)) return;
        float damage = event.getNewDamage();
        if (damage <= 0.0f) return;

        storeBloodFromPlayer(player, damage);
    }

    /** 受到伤害：玩家承受伤害时，血包存储所受伤害的一定比例 */
    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        float damage = event.getNewDamage();
        if (damage <= 0.0f) return;

        storeBloodFromPlayer(player, damage);
    }

    /** 远程攻击：玩家发射的投射物命中生物时，血包按伤害存储血量 */
    @SubscribeEvent
    public static void onRangedHit(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide()) return;
        if (!(projectile.getOwner() instanceof Player player)) return;

        HitResult hit = event.getRayTraceResult();
        if (!(hit instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity)) return;

        float damage = estimateProjectileDamage(projectile);
        storeBloodFromPlayer(player, damage);
    }

    /** 估算投射物伤害：箭矢用其基础伤害，其他投射物取一个保守默认值 */
    private static float estimateProjectileDamage(Projectile projectile) {
        if (projectile instanceof AbstractArrow arrow) {
            return (float) arrow.getBaseDamage();
        }
        return 6.0f;
    }

    /** 优先存入主手血包，其次副手 */
    private static void storeBloodFromPlayer(Player player, float damage) {
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (stack.getItem() instanceof BloodPackItem pack) {
                BloodPackItem.addStoredHealth(stack, damage * pack.getStorageEfficiency());
                return;
            }
        }
    }
}