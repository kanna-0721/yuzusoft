package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 隐身卡片（并入自 mod1007 工程）：
 * <ul>
 *   <li>右键开关隐身，开启需至少 10 级经验；</li>
 *   <li>隐身期间每 20 tick 消耗 1 点经验，经验不足时自动关闭；</li>
 *   <li>死亡或重生时自动关闭；</li>
 *   <li>玩家首次进入世界（开局）时发放一张卡片。</li>
 * </ul>
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public final class InvisibilityCardEvents {
    public static final int REQUIRED_LEVEL = 10;
    private static final int DRAIN_INTERVAL_TICKS = 20;
    private static final int EFFECT_REFRESH_TICKS = 60;
    /** 开局发放标记：存于玩家持久数据，确保每名玩家只发放一次。 */
    private static final String GRANTED_TAG = "yuzusoft_invisibility_card_granted";

    private InvisibilityCardEvents() {
    }

    public static void toggle(ServerPlayer player) {
        if (isActive(player)) {
            deactivate(player);
            return;
        }

        if (player.experienceLevel < REQUIRED_LEVEL) {
            player.displayClientMessage(Component.translatable("message.yuzusoft.level_insufficient"), true);
            return;
        }

        activate(player);
    }

    private static boolean isActive(Player player) {
        return Boolean.TRUE.equals(player.getData(ModAttachments.CARD_ACTIVE.get()));
    }

    private static void activate(ServerPlayer player) {
        player.setData(ModAttachments.CARD_ACTIVE.get(), Boolean.TRUE);
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, EFFECT_REFRESH_TICKS, 0, false, false, true));
        updateCardGlint(player, true);
    }

    private static void deactivate(ServerPlayer player) {
        player.setData(ModAttachments.CARD_ACTIVE.get(), Boolean.FALSE);
        player.removeEffect(MobEffects.INVISIBILITY);
        updateCardGlint(player, false);
    }

    private static void updateCardGlint(Player player, boolean active) {
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            updateCardStackGlint(stack, active);
        }
        for (ItemStack stack : inventory.offhand) {
            updateCardStackGlint(stack, active);
        }
    }

    private static void updateCardStackGlint(ItemStack stack, boolean active) {
        if (!stack.is(ModItems.INVISIBILITY_CARD.get())) {
            return;
        }
        if (active) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !isActive(player)
                || player.tickCount % DRAIN_INTERVAL_TICKS != 0) {
            return;
        }
        drainOnePointOrCancel(player);
    }

    private static void drainOnePointOrCancel(ServerPlayer player) {
        if (player.experienceLevel < REQUIRED_LEVEL || !player.hasEffect(MobEffects.INVISIBILITY)) {
            deactivate(player);
            return;
        }

        int needed = player.getXpNeededForNextLevel();
        int pointsInLevel = Math.round(player.experienceProgress * needed);

        if (player.experienceLevel == REQUIRED_LEVEL) {
            if (pointsInLevel < 1) {
                deactivate(player);
                return;
            }

            player.experienceProgress = (pointsInLevel - 1) / (float) needed;
            player.totalExperience = Math.max(0, player.totalExperience - 1);
            syncExperience(player);

            if (pointsInLevel - 1 < 1) {
                deactivate(player);
                return;
            }
        } else if (pointsInLevel >= 1) {
            player.experienceProgress = (pointsInLevel - 1) / (float) needed;
            player.totalExperience = Math.max(0, player.totalExperience - 1);
            syncExperience(player);
        } else {
            player.experienceLevel--;
            int previousLevelNeeded = player.getXpNeededForNextLevel();
            player.experienceProgress = (previousLevelNeeded - 1) / (float) previousLevelNeeded;
            player.totalExperience = Math.max(0, player.totalExperience - 1);
            syncExperience(player);
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, EFFECT_REFRESH_TICKS, 0, false, false, true));
    }

    private static void syncExperience(ServerPlayer player) {
        player.connection.send(new ClientboundSetExperiencePacket(
                player.experienceProgress, player.totalExperience, player.experienceLevel));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            deactivate(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            deactivate(player);
        }
    }

    /** 开局发放：玩家首次进入世界时获得一张隐身卡片（每名玩家仅一次）。 */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getPersistentData().getBoolean(GRANTED_TAG)) {
            return;
        }
        player.getPersistentData().putBoolean(GRANTED_TAG, true);

        ItemStack card = new ItemStack(ModItems.INVISIBILITY_CARD.get());
        if (!player.getInventory().add(card)) {
            player.drop(card, false);
        }
    }
}