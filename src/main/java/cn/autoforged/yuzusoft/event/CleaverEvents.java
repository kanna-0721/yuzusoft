package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.component.MobMeatData;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.item.custom.CleaverItem;
import cn.autoforged.yuzusoft.item.custom.RustyCleaverItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 菜刀（并入自 knife 工程）的掉落、生锈、恢复与创造栏逻辑（仅在游戏侧生效）。
 */
public final class CleaverEvents {
    private CleaverEvents() {}

    /** 生锈概率：每次用菜刀击败生物后 25% 变生锈菜刀。 */
    public static final float RUST_CHANCE = 0.25F;
    /** 生锈持续：60 秒 = 1200 游戏 tick。 */
    public static final long RUST_DURATION_TICKS = 20L * 60L;

    /**
     * 任意生物（Mob）被玩家以菜刀近战击杀：
     * - 额外掉落 (1 + 抢夺等级) 块来源生物肉，抢夺 III 封顶 4 块；
     * - 25% 概率把手上的菜刀变成生锈菜刀（附魔/自定义名/ lore 保留），并播放低沉铁器音效。
     */
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Mob)) {
            return;
        }
        Level level = victim.level();
        if (level.isClientSide) {
            return;
        }
        // 只认近战击杀（弓箭直射不算“用菜刀击败”）
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof CleaverItem)) {
            return;
        }

        int lootingLevel = Math.max(0, EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
                weapon));
        int count = Math.min(1 + lootingLevel, 4);

        ResourceLocation sourceId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        int nutrition = Math.max(1, Mth.ceil(victim.getMaxHealth()));
        MobMeatData data = new MobMeatData(sourceId, nutrition);
        for (int i = 0; i < count; i++) {
            ItemStack meat = new ItemStack(ModItems.MOB_MEAT.get());
            meat.set(ModDataComponents.MOB_MEAT.get(), data);
            ItemEntity drop = new ItemEntity(level,
                    victim.getX() + (level.random.nextFloat() - 0.5F) * 0.6D,
                    victim.getY() + (level.random.nextFloat() - 0.5F) * 0.3D,
                    victim.getZ() + (level.random.nextFloat() - 0.5F) * 0.6D,
                    meat);
            drop.setPickUpDelay(10);
            event.getDrops().add(drop);
        }

        if (level.random.nextFloat() < RUST_CHANCE) {
            ItemStack rusty = toRusty(weapon, level.getGameTime() + RUST_DURATION_TICKS);
            Inventory inventory = player.getInventory();
            inventory.items.set(inventory.selected, rusty);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundSource.PLAYERS, 0.7F, 0.6F);
        }
    }

    /** 每 10 tick 检查玩家物品栏中的生锈菜刀，60 秒一到自动换回菜刀并播放清亮铁器音效。 */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide || player.tickCount % 10 != 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        boolean recovered = false;
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (stack.getItem() instanceof RustyCleaverItem && isRustFinished(stack, level)) {
                inventory.items.set(i, toCleaver(stack));
                recovered = true;
            }
        }
        ItemStack offhand = inventory.offhand.isEmpty() ? ItemStack.EMPTY : inventory.offhand.get(0);
        if (offhand.getItem() instanceof RustyCleaverItem && isRustFinished(offhand, level)) {
            inventory.offhand.set(0, toCleaver(offhand));
            recovered = true;
        }
        if (recovered && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundSource.PLAYERS, 0.7F, 1.5F);
        }
    }

    /** 创造栏：菜刀/生锈菜刀进“战斗”，一份牛肉示例进“食物”。 */
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            event.accept(new ItemStack(ModItems.CLEAVER.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(ModItems.RUSTY_CLEAVER.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)) {
            ItemStack beef = new ItemStack(ModItems.MOB_MEAT.get());
            beef.set(ModDataComponents.MOB_MEAT.get(),
                    new MobMeatData(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.COW), 10));
            event.accept(beef, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean isRustFinished(ItemStack stack, Level level) {
        Long deadline = stack.get(ModDataComponents.RUST_UNTIL.get());
        return deadline == null || level.getGameTime() >= deadline;
    }

    private static ItemStack toRusty(ItemStack old, long deadline) {
        ItemStack rusty = new ItemStack(ModItems.RUSTY_CLEAVER.get());
        transfer(old, rusty);
        rusty.set(ModDataComponents.RUST_UNTIL.get(), deadline);
        return rusty;
    }

    private static ItemStack toCleaver(ItemStack old) {
        ItemStack cleaver = new ItemStack(ModItems.CLEAVER.get());
        transfer(old, cleaver);
        return cleaver;
    }

    /** 新旧物品之间保留附魔、自定义名与 lore。 */
    private static void transfer(ItemStack from, ItemStack to) {
        ItemEnchantments enchantments = from.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            to.set(DataComponents.ENCHANTMENTS, enchantments);
        }
        Component customName = from.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            to.set(DataComponents.CUSTOM_NAME, customName);
        }
        ItemLore lore = from.get(DataComponents.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            to.set(DataComponents.LORE, lore);
        }
    }
}