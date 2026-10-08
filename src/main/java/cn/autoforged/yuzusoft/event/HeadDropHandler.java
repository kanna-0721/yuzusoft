package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.head.MobHeadRegistry;
import cn.autoforged.yuzusoft.item.custom.ScalpelItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * 生物头相关事件：
 * 1. 掉落——统一在此处掉落头颅：
 *    <ul>
 *      <li>普通有头生物：1% 概率掉自身头，被闪电苦力怕炸死或手术刀击杀则必定掉；</li>
 *      <li>融合生物（HEAD 槽戴着「接上去的头」）：只返还该头，身体自身的原旧头不掉落。</li>
 *    </ul>
 * 2. 减探测——玩家戴上某生物的头时，该生物对该玩家的可见度减半（同原版戴头机制）。
 * <p>
 * 实体↔头颅映射统一由 {@link MobHeadRegistry} 提供。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class HeadDropHandler {

    /** 普通死亡掉头概率（1%）。 */
    private static final float HEAD_DROP_CHANCE = 0.01F;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        // 融合生物：HEAD 槽里就是「接上去的那个新头」。死亡只返还这个头，
        // 身体对应的原旧头不再掉落（否则会出现 1 身体 + 2 头）。
        ItemStack wornHead = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
        if (MobHeadRegistry.entityFor(wornHead.getItem()) != null) {
            event.getEntity().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            event.getEntity().spawnAtLocation(wornHead);
            return;
        }
        Item head = MobHeadRegistry.headFor(event.getEntity().getType());
        if (head == null) {
            return;
        }
        // 手术刀击杀必定掉头（由 ScalpelEvents 掉出无头躯体）；被闪电苦力怕炸死也必掉
        boolean killedByScalpel = event.getSource().getDirectEntity() instanceof ServerPlayer player
                && player.getMainHandItem().getItem() instanceof ScalpelItem;
        boolean killedByChargedCreeper = event.getSource().getDirectEntity() instanceof Creeper creeper
                && creeper.isPowered();
        if (killedByScalpel || killedByChargedCreeper
                || event.getEntity().getRandom().nextFloat() < HEAD_DROP_CHANCE) {
            event.getEntity().spawnAtLocation(new ItemStack(head));
        }
    }

    /**
     * 戴头减探测（同原版）：玩家头上戴着某生物的头时，该生物对该玩家的可见度减半，
     * 经原版 TargetingConditions 的 range 计算表现为搜索/索敌范围减半。
     */
    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        EntityType<?> ownerType = MobHeadRegistry.entityFor(
                event.getEntity().getItemBySlot(EquipmentSlot.HEAD).getItem());
        if (ownerType != null
                && event.getLookingEntity() != null
                && event.getLookingEntity().getType() == ownerType) {
            event.modifyVisibility(0.5);
        }
    }
}