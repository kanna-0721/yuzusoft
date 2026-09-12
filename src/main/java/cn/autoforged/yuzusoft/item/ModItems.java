package cn.autoforged.yuzusoft.item;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.ModBlocks;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.custom.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CycloneSwordMod.MODID);

    public static final DeferredItem<CycloneSwordItem> CYCLONE_SWORD = registerItem("cyclone_sword",
            () -> new CycloneSwordItem());;

    public static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }
    public static final DeferredItem<Item> GUARDIAN_SPAWN_EGG = registerItem("guardian_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GUARDIAN, 0x79DF84, 0xB53C26, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GUARDIANS_SPAWN_EGG =
            ITEMS.register("guardian_trader_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            () -> ModEntities.GUARDIAN_TRADER.get(),
                            0xFDFCFE,
                            0x85B7E0,
                            new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, ShadowDartItem> SHADOW_DART =
            ITEMS.register("shadow_dart", ShadowDartItem::new);

    public static final DeferredHolder<Item, DeferredSpawnEggItem> SHADOW_ASSASSIN_SPAWN_EGG =
            ITEMS.register("shadow_assassin_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.SHADOW_ASSASSIN::get,
                            0x000000,
                            0x2B6C32,
                            new Item.Properties()));
    public static final DeferredItem<Item> TAMAGOYAKI = ITEMS.register("tamagoyaki",
            () -> new Item(new Item.Properties().food(ModFoods.TAMAGOYAKI)));

    public static final DeferredItem<DetonatorItem> DETONATOR = registerItem("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<DeferredSpawnEggItem> DETONATOR_MONSTER_SPAWN_EGG = registerItem("detonator_monster_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DETONATOR_THROWING_MONSTER, 0xF2F2FA, 0x7B4FA6,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> FROST_GUARDIAN_SPAWN_EGG =
            ITEMS.register("frost_guardian_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.FROST_GUARDIAN,
                            0xFEAD94,
                            0xCB647C,
                            new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredItem<FrostBarrierCardItem> FROST_BARRIER_CARD = ITEMS.register(
            "frost_barrier_card",
            () -> new FrostBarrierCardItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(10)));
    // 冰霜守卫 V2 刷怪蛋：前景冰蓝 / 背景深青
    public static final DeferredItem<SpawnEggItem> FROST_GUARDIAN_V2_SPAWN_EGG = ITEMS.register(
            "frost_guardian_v2_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FROST_GUARDIAN_V2.get(), 0xBACAC9, 0x90C1B8,
                    new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> FLOATING_SENTINEL_SPAWN_EGG =
            ITEMS.register("floating_sentinel_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.FLOATING_SENTINEL,
                            0xFDC8D8,
                            0x250D27,
                            new Item.Properties().stacksTo(64)));
    public static final DeferredItem<FloatingSentinelChestplateItem> FLOATING_SENTINEL_CHESTPLATE =
            ITEMS.register("floating_sentinel_chestplate",
                    () -> new FloatingSentinelChestplateItem(
                            ModArmorMaterials.FLOATING_SENTINEL,
                            ArmorItem.Type.CHESTPLATE,
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.RARE)
                                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(15))));
    public static final DeferredItem<Item> DUMPLINGS = ITEMS.register("dumplings",
            () -> new Item(new Item.Properties().food(ModFoods.DUMPLINGS)));
    public static final DeferredItem<Item> VILLAGE_GUARDIAN_SPAWN_EGG = ITEMS.register("village_guardian_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.VILLAGE_GUARDIAN,
                    0xEBE70E,
                    0xA61E05,
                    new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> SPRINKLER = registerItem("sprinkler",
            () -> new SprinklerItem(new Item.Properties().stacksTo(1).durability(400)));

    public static final DeferredItem<Item> WATER_BALL = registerItem("water_ball",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPRINKLER_CREEP_SPAWN_EGG = registerItem("sprinkler_creep_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SPRINKLER_CREEP, 0xFFFFFF, 0x4E0B0B, new Item.Properties()));
    public static final DeferredHolder<Item, GuitarWeaponItem> GUITAR_WEAPON = ITEMS.register("guitar_weapon",
            () -> new GuitarWeaponItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(500)
                    .attributes(createGuitarAttributes())));
    public static final DeferredHolder<Item, GuitarWeaponItem> GUITAR_WEAPON_FIXED = ITEMS.register("guitar_weapon_fixed",
            () -> new GuitarWeaponItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(1500)
                    .attributes(createGuitarAttributes())));
    public static final DeferredHolder<Item, SpawnEggItem> GUITAR_MONSTER_SPAWN_EGG = ITEMS.register("guitar_monster_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GUITAR_MONSTER.get(), 0xE972DB, 0xF13954, new Item.Properties()));
    public static final DeferredItem<Item> SLEEPY_SPIRIT_SPAWN_EGG =
            ITEMS.register("sleepy_spirit_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.SLEEPY_SPIRIT,
                            0xB5536D,
                            0xCFC584,
                            new Item.Properties()));
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final Component UPGRADE_TITLE = Component.translatable(
                    Util.makeDescriptionId("upgrade", ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_upgrade"))
            )
            .withStyle(TITLE_FORMAT);
    private static final Component APPLIES_TO = Component.translatable(
                    Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "smithing_template.guitar_upgrade.applies_to"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component INGREDIENTS = Component.translatable(
                    Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "smithing_template.guitar_upgrade.ingredients"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component BASE_SLOT_DESC = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "smithing_template.guitar_upgrade.base_slot_description"))
    );
    private static final Component ADDITIONS_SLOT_DESC = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "smithing_template.guitar_upgrade.additions_slot_description"))
    );
    private static final ResourceLocation EMPTY_SLOT_GUITAR_BASE = ResourceLocation.withDefaultNamespace("item/empty_slot_emerald");
    private static final ResourceLocation EMPTY_SLOT_GUITAR_MATERIAL = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");

    private static List<ResourceLocation> createBaseIconList() {
        return List.of(EMPTY_SLOT_GUITAR_BASE);
    }

    private static List<ResourceLocation> createAdditionIconList() {
        return List.of(EMPTY_SLOT_GUITAR_MATERIAL);
    }

    public static final DeferredItem<SmithingTemplateItem> GUITAR_UPGRADE_TEMPLATE = registerItem("guitar_upgrade_template",
            () -> new SmithingTemplateItem(
                    APPLIES_TO,
                    INGREDIENTS,
                    UPGRADE_TITLE,
                    BASE_SLOT_DESC,
                    ADDITIONS_SLOT_DESC,
                    createBaseIconList(),
                    createAdditionIconList()
            ));

    private static ItemAttributeModifiers createGuitarAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 5.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
    public static final DeferredItem<Item> FLASHBANG = ITEMS.register("flashbang",
            () -> new FlashbangItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> FLASHBANG_MONSTER_SPAWN_EGG = ITEMS.register("flashbang_monster_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FLASHBANG_MONSTER.get(), 0x702AAA, 0xCC8800, new Item.Properties()));
    public static final DeferredItem<StunHammerItem> STUN_HAMMER = registerItem("stun_hammer",
            () -> new StunHammerItem(StunHammerItem.HAMMER_TIER, new Item.Properties()
                    .stacksTo(1)
                    .durability(500)
                    .attributes(SwordItem.createAttributes(StunHammerItem.HAMMER_TIER, 9, -3.4F))));

    public static final DeferredItem<DeferredSpawnEggItem> HAMMER_WIELDER_SPAWN_EGG = registerItem("hammer_wielder_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HAMMER_WIELDER, 0xA88991, 0x76CDDB, new Item.Properties()));

    public static final DeferredItem<BloodPackItem> BLOOD_PACK_BASIC = registerItem("blood_pack_basic",
            () -> new BloodPackItem(
                    new Item.Properties()
                            .food(ModFoods.BLOOD_PACK_BASIC)
                            .stacksTo(1),
                    0.1f,
                    20,
                    3600));

    public static final DeferredItem<BloodPackItem> BLOOD_PACK_MID = registerItem("blood_pack_mid",
            () -> new BloodPackItem(
                    new Item.Properties()
                            .food(ModFoods.BLOOD_PACK_MID)
                            .stacksTo(1),
                    0.15f,
                    40,
                    3600));

    public static final DeferredItem<BloodPackItem> BLOOD_PACK_HIGH = registerItem("blood_pack_high",
            () -> new BloodPackItem(
                    new Item.Properties()
                            .food(ModFoods.BLOOD_PACK_HIGH)
                            .stacksTo(1),
                    0.2f,
                    80,
                    3600));

    public static final DeferredItem<Item> BLOOD_PACK_TEMPLATE = registerItem("blood_pack_template",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)));

    public static final DeferredItem<Item> BLOOD_SUCKER_SPAWN_EGG = registerItem("blood_sucker_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.BLOOD_SUCKER_ZOMBIE,
                    0xE85159,
                    0xD59350,
                    new Item.Properties()));

    public static final DeferredItem<Item> DUAL_FORM_MOB_SPAWN_EGG = registerItem("dual_form_mob_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.DUAL_FORM_MOB,
                    0xFFF8D9,
                    0x7DC0EB,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HUMANOID_CREATURE_SPAWN_EGG = registerItem("humanoid_creature_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.HUMANOID_CREATURE,
                    0xF283E2,
                    0x4ED95E,
                    new Item.Properties()));

    public static final DeferredItem<Item> SUZUNE_SPAWN_EGG = registerItem("suzune_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.SUZUNE,
                    0xE2AABC,
                    0xB6D6EB,
                    new Item.Properties()));
    public static final DeferredItem<BlockItem> SUZUNE_ALARM_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SUZUNE_ALARM);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
