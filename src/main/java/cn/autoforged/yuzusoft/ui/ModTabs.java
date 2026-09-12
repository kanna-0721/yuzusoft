package cn.autoforged.yuzusoft.ui;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
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
                        output.accept(ModItems.SPRINKLER.get());
                        output.accept(ModItems.SUZUNE_ALARM_ITEM.get());
                        output.accept(ModItems.GUITAR_WEAPON.get());
                        output.accept(ModItems.GUITAR_WEAPON_FIXED.get());
                        output.accept(ModItems.GUITAR_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.FLASHBANG.get());
                        output.accept(ModItems.BLOOD_PACK_BASIC.get());
                        output.accept(ModItems.BLOOD_PACK_MID.get());
                        output.accept(ModItems.BLOOD_PACK_HIGH.get());
                        output.accept(ModItems.BLOOD_PACK_TEMPLATE.get());
                        output.accept(ModItems.DETONATOR_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.FROST_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.HAMMER_WIELDER_SPAWN_EGG.get());
                        output.accept(ModItems.GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.GUARDIANS_SPAWN_EGG.get());
                        output.accept(ModItems.SHADOW_ASSASSIN_SPAWN_EGG.get());
                        output.accept(ModItems.FLOATING_SENTINEL_SPAWN_EGG.get());
                        output.accept(ModItems.VILLAGE_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.FROST_GUARDIAN_V2_SPAWN_EGG.get());
                        output.accept(ModItems.SPRINKLER_CREEP_SPAWN_EGG.get());
                        output.accept(ModItems.SUZUNE_SPAWN_EGG.get());
                        output.accept(ModItems.DUAL_FORM_MOB_SPAWN_EGG.get());
                        output.accept(ModItems.HUMANOID_CREATURE_SPAWN_EGG.get());
                        output.accept(ModItems.GUITAR_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.SLEEPY_SPIRIT_SPAWN_EGG.get());
                        output.accept(ModItems.FLASHBANG_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.BLOOD_SUCKER_SPAWN_EGG.get());
                    }).build());
}
