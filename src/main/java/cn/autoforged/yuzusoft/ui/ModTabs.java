package cn.autoforged.yuzusoft.ui;

import cn.autoforged.yuzusoft.block.custom.MobSkullType;
import cn.autoforged.yuzusoft.charm.CharmRegistry;
import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.MobMeatData;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.head.MobHeadRegistry;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CycloneSwordMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CYCLONE_TAB =
            CREATIVE_TABS.register("cyclone_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CycloneSwordMod.MODID))
                    .withTabsBefore(  CreativeModeTabs.BUILDING_BLOCKS,
                            CreativeModeTabs.COLORED_BLOCKS,
                            CreativeModeTabs.REDSTONE_BLOCKS,
                            CreativeModeTabs.TOOLS_AND_UTILITIES,
                            CreativeModeTabs.COMBAT,
                            CreativeModeTabs.FOOD_AND_DRINKS,
                            CreativeModeTabs.INGREDIENTS,
                            CreativeModeTabs.SPAWN_EGGS,
                            CreativeModeTabs.OP_BLOCKS)
                    .icon(() -> ModItems.DETONATOR.get().getDefaultInstance())
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.DETONATOR.get());
                        output.accept(ModItems.STUN_HAMMER.get());
                        output.accept(ModItems.CYCLONE_SWORD.get());
                        output.accept(ModItems.SHADOW_DART.get());
                        output.accept(ModItems.TAMAGOYAKI.get());
                        output.accept(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
                        output.accept(ModItems.DUMPLINGS.get());
                        output.accept(ModItems.FROST_BARRIER_CARD.get());
                        output.accept(ModItems.INVISIBILITY_CARD.get());
                        output.accept(ModItems.WATER_SPIRIT_UMBRELLA.get());
                        output.accept(ModItems.SPRINKLER.get());
                        output.accept(ModItems.SUZUNE_ALARM_ITEM.get());
                        output.accept(ModItems.GUITAR_WEAPON.get());
                        output.accept(ModItems.GUITAR_WEAPON_FIXED.get());
                        output.accept(ModItems.GUITAR_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.FLASHBANG.get());
                        output.accept(ModItems.GRAND_PIANO.get());
                        output.accept(ModItems.BLOOD_PACK_BASIC.get());
                        output.accept(ModItems.BLOOD_PACK_MID.get());
                        output.accept(ModItems.BLOOD_PACK_HIGH.get());
                        output.accept(ModItems.BLOOD_PACK_TEMPLATE.get());
                        output.accept(ModItems.TRANSFORMATION_WAND.get());
                        for (int i = 1; i <= 5; i++) {
                            ItemStack bottle = new ItemStack(ModItems.OMENS_BOTTLE_0721.get());
                            bottle.set(ModDataComponents.BOTTLE_0721_LEVEL, i);
                            output.accept(bottle);
                        }
                        ItemStack mobMeat = new ItemStack(ModItems.MOB_MEAT.get());
                        mobMeat.set(ModDataComponents.MOB_MEAT.get(), new MobMeatData(
                                BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.COW), 10));
                        output.accept(mobMeat);
                        output.accept(ModItems.CLEAVER.get());
                        output.accept(ModItems.RUSTY_CLEAVER.get());
                        output.accept(ModItems.SCALPEL.get());
                        output.accept(sampleBody());
                        output.accept(CharmRegistry.CHARM_BOTTLE.get());
                        output.accept(CharmRegistry.CHARM_HEART.get());
                        output.accept(CharmRegistry.CHARM_ARROW.get());
                        output.accept(CharmRegistry.charmArrow(CharmRegistry.LONG_CHARM_DURATION)); // 延长 90s
                        acceptPotion(output, Items.POTION, CharmRegistry.CHARM_POTION);
                        acceptPotion(output, Items.SPLASH_POTION, CharmRegistry.CHARM_POTION);
                        acceptPotion(output, Items.LINGERING_POTION, CharmRegistry.CHARM_POTION);
                        acceptPotion(output, Items.POTION, CharmRegistry.LONG_CHARM_POTION);
                        acceptPotion(output, Items.SPLASH_POTION, CharmRegistry.LONG_CHARM_POTION);
                        acceptPotion(output, Items.LINGERING_POTION, CharmRegistry.LONG_CHARM_POTION);
                        output.accept(ModItems.DETONATOR_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.FROST_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.HAMMER_WIELDER_SPAWN_EGG.get());
                        output.accept(ModItems.GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.GUARDIANS_SPAWN_EGG.get());
                        output.accept(ModItems.SHADOW_ASSASSIN_SPAWN_EGG.get());
                        output.accept(ModItems.FLOATING_SENTINEL_SPAWN_EGG.get());
                        output.accept(ModItems.VILLAGE_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.EVIL_NANAMI_SPAWN_EGG.get());
                        output.accept(ModItems.FROST_GUARDIAN_V2_SPAWN_EGG.get());
                        output.accept(ModItems.WATER_SPIRIT_SPAWN_EGG.get());
                        output.accept(ModItems.SPRINKLER_CREEP_SPAWN_EGG.get());
                        output.accept(ModItems.SUZUNE_SPAWN_EGG.get());
                        output.accept(ModItems.DUAL_FORM_MOB_SPAWN_EGG.get());
                        output.accept(ModItems.HUMANOID_CREATURE_SPAWN_EGG.get());
                        output.accept(ModItems.DIAMOND_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.GUITAR_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.SLEEPY_SPIRIT_SPAWN_EGG.get());
                        output.accept(ModItems.FLASHBANG_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.SHIMAGOE_TSUKUMI_SPAWN_EGG.get());
                        output.accept(ModItems.BLOOD_SUCKER_SPAWN_EGG.get());
                        output.accept(ModItems.CAT_0721_SPAWN_EGG.get());
                    }).build());

    /**
     * 「柚子社原材料」：21 种柚子社生物的「无头身体」+ 对应的头颅。
     * <p>
     * 头颅原先放在 {@link #CYCLONE_TAB}，现整体迁入本栏。
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
            CREATIVE_TABS.register("yuzusoft_materials", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CycloneSwordMod.MODID + ".materials"))
                    .withTabsBefore(CYCLONE_TAB.getKey())
                    .icon(() -> ModItems.HEAD_ITEMS.get(MobSkullType.MURASAME).get().getDefaultInstance())
                    .displayItems((itemDisplayParameters, output) -> {
                        for (EntityType<?> type : MobHeadRegistry.yuzusoftHeadTypes()) {
                            output.accept(bodyOf(type));
                        }
                        ModItems.HEAD_ITEMS.values().forEach(head -> output.accept(head.get()));
                    }).build());

    /** 把指定药水烘焙成对应物品形态（普通/喷溅/滞留）的健康栈加入标签页。 */
    private static void acceptPotion(CreativeModeTab.Output output, Item item, Holder<Potion> potion) {
        ItemStack stack = new ItemStack(item);
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(potion));
        output.accept(stack);
    }

    /** 创造栏示例：只带 id 的僵尸无头身体（供渲染演示与试放置）。 */
    private static ItemStack sampleBody() {
        return bodyOf(EntityType.ZOMBIE);
    }

    /** 指定生物的「无头身体」物品栈（只带实体 id 的示例 NBT，放置时会还原为该生物本体）。 */
    private static ItemStack bodyOf(EntityType<?> type) {
        ItemStack stack = new ItemStack(ModItems.DECAPITATED_BODY.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        stack.set(ModDataComponents.STORED_MOB.get(), tag);
        return stack;
    }
}
