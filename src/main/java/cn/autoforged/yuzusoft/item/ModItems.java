package cn.autoforged.yuzusoft.item;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.ModBlocks;
import cn.autoforged.yuzusoft.block.custom.MobSkullType;
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

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CycloneSwordMod.MODID);

    public static final DeferredItem<CycloneSwordItem> CYCLONE_SWORD = registerItem("cyclone_sword",
            () -> new CycloneSwordItem());;

    public static final DeferredItem<OmensBottle0721Item> OMENS_BOTTLE_0721 = ITEMS.register("omen_0721_bottle",
            () -> new OmensBottle0721Item(new Item.Properties().stacksTo(64)));

    public static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }
    public static final DeferredItem<Item> GUARDIAN_SPAWN_EGG = registerItem("murasame_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GUARDIAN, 0x79DF84, 0xB53C26, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GUARDIANS_SPAWN_EGG =
            ITEMS.register("tomotake_yoshino_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            () -> ModEntities.GUARDIAN_TRADER.get(),
                            0xFDFCFE,
                            0x85B7E0,
                            new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, ShadowDartItem> SHADOW_DART =
            ITEMS.register("shadow_dart", ShadowDartItem::new);

    public static final DeferredHolder<Item, DeferredSpawnEggItem> SHADOW_ASSASSIN_SPAWN_EGG =
            ITEMS.register("hitachi_mako_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.SHADOW_ASSASSIN::get,
                            0x000000,
                            0x2B6C32,
                            new Item.Properties()));
    public static final DeferredItem<Item> TAMAGOYAKI = ITEMS.register("tamagoyaki",
            () -> new Item(new Item.Properties().food(ModFoods.TAMAGOYAKI)));

    public static final DeferredItem<DetonatorItem> DETONATOR = registerItem("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<DeferredSpawnEggItem> DETONATOR_MONSTER_SPAWN_EGG = registerItem("ayachi_nene_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DETONATOR_THROWING_MONSTER, 0xF2F2FA, 0x7B4FA6,
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> FROST_GUARDIAN_SPAWN_EGG =
            ITEMS.register("inaba_meguru_spawn_egg",
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

    // ===== 隐身卡片（并入自 mod1007 工程）：右键开关隐身，需 10 级经验并持续消耗经验 =====
    public static final DeferredItem<InvisibilityCardItem> INVISIBILITY_CARD = ITEMS.register(
            "invisibility_card",
            () -> new InvisibilityCardItem(new Item.Properties().stacksTo(1)));
    // 冰霜守卫 V2 刷怪蛋：前景冰蓝 / 背景深青
    public static final DeferredItem<SpawnEggItem> FROST_GUARDIAN_V2_SPAWN_EGG = ITEMS.register(
            "shikibe_mayu_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FROST_GUARDIAN_V2.get(), 0xBACAC9, 0x90C1B8,
                    new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> FLOATING_SENTINEL_SPAWN_EGG =
            ITEMS.register("mitsukasa_ayase_spawn_egg",
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
    public static final DeferredItem<Item> VILLAGE_GUARDIAN_SPAWN_EGG = ITEMS.register("arihara_nanami_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.VILLAGE_GUARDIAN,
                    0xEBE70E,
                    0xA61E05,
                    new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> EVIL_NANAMI_SPAWN_EGG = ITEMS.register("evil_nanami_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.EVIL_NANAMI,
                    0xEBE70E,
                    0xA61E05,
                    new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> SPRINKLER = registerItem("sprinkler",
            () -> new SprinklerItem(new Item.Properties().stacksTo(1).durability(400)));

    public static final DeferredItem<Item> WATER_BALL = registerItem("water_ball",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPRINKLER_CREEP_SPAWN_EGG = registerItem("akizuki_kanna_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SPRINKLER_CREEP, 0xFFFFFF, 0x4E0B0B, new Item.Properties()));
    public static final DeferredHolder<Item, GuitarWeaponItem> GUITAR_WEAPON = ITEMS.register("guitar_weapon",
            () -> new GuitarWeaponItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(40)
                    .attributes(createGuitarAttributes())));
    public static final DeferredHolder<Item, GuitarWeaponItem> GUITAR_WEAPON_FIXED = ITEMS.register("guitar_weapon_fixed",
            () -> new GuitarWeaponItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(100)
                    .attributes(createGuitarAttributes())));
    public static final DeferredHolder<Item, SpawnEggItem> GUITAR_MONSTER_SPAWN_EGG = ITEMS.register("harumi_ena_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GUITAR_MONSTER.get(), 0xE972DB, 0xF13954, new Item.Properties()));
    public static final DeferredItem<Item> SLEEPY_SPIRIT_SPAWN_EGG =
            ITEMS.register("futamihara_ririko_spawn_egg",
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
    public static final DeferredItem<Item> FLASHBANG_MONSTER_SPAWN_EGG = ITEMS.register("nabari_anju_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FLASHBANG_MONSTER.get(), 0x702AAA, 0xCC8800, new Item.Properties()));
    public static final DeferredItem<StunHammerItem> STUN_HAMMER = registerItem("stun_hammer",
            () -> new StunHammerItem(StunHammerItem.HAMMER_TIER, new Item.Properties()
                    .stacksTo(1)
                    .durability(500)
                    .attributes(SwordItem.createAttributes(StunHammerItem.HAMMER_TIER, 9, -3.4F))));

    public static final DeferredItem<DeferredSpawnEggItem> HAMMER_WIELDER_SPAWN_EGG = registerItem("shiiba_tsumugi_spawn_egg",
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

    public static final DeferredItem<Item> BLOOD_SUCKER_SPAWN_EGG = registerItem("yarai_miu_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.BLOOD_SUCKER_ZOMBIE,
                    0xE85159,
                    0xD59350,
                    new Item.Properties()));

    public static final DeferredItem<Item> CAT_0721_SPAWN_EGG = registerItem("mikado_takanori_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.CAT_0721,
                    0x9FB6C9,
                    0xF5F2EC,
                    new Item.Properties()));

    public static final DeferredItem<Item> DUAL_FORM_MOB_SPAWN_EGG = registerItem("shirayuki_noa_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.DUAL_FORM_MOB,
                    0xFFF8D9,
                    0x7DC0EB,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> HUMANOID_CREATURE_SPAWN_EGG = registerItem("tanikaze_amane_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.HUMANOID_CREATURE,
                    0xF283E2,
                    0x4ED95E,
                    new Item.Properties()));

    public static final DeferredItem<Item> SUZUNE_SPAWN_EGG = registerItem("shioyama_suzune_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.SUZUNE,
                    0xE2AABC,
                    0xB6D6EB,
                    new Item.Properties()));
    public static final DeferredItem<BlockItem> SUZUNE_ALARM_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SUZUNE_ALARM);

    // ===== 三角钢琴（并入自 PianoNeoForge 工程）=====
    public static final DeferredItem<BlockItem> GRAND_PIANO = ITEMS.registerSimpleBlockItem(ModBlocks.GRAND_PIANO);

    /** 岛越月望刷怪蛋：深靛发色 + 裙装高光两色。 */
    public static final DeferredItem<DeferredSpawnEggItem> SHIMAGOE_TSUKUMI_SPAWN_EGG =
            ITEMS.register("shimagoe_tsukumi_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SHIMAGOE_TSUKUMI, 0xFEEC80, 0xAAD0E5, new Item.Properties()));

    /** 全部生物头颅物品（与 {@link ModBlocks#HEADS} 一一对应）。 */
    public static final Map<MobSkullType, DeferredItem<BlockItem>> HEAD_ITEMS =
            Collections.unmodifiableMap(createHeadItems());

    private static Map<MobSkullType, DeferredItem<BlockItem>> createHeadItems() {
        Map<MobSkullType, DeferredItem<BlockItem>> items = new EnumMap<>(MobSkullType.class);
        for (MobSkullType type : MobSkullType.values()) {
            items.put(type, ITEMS.registerSimpleBlockItem(ModBlocks.head(type)));
        }
        return items;
    }

    /** 按骷髅类型取对应的头颅物品。 */
    public static DeferredItem<BlockItem> headItem(MobSkullType type) {
        return HEAD_ITEMS.get(type);
    }

    public static final DeferredItem<Item> DIAMOND_GUARDIAN_SPAWN_EGG = registerItem("kohibari_kurumi_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.DIAMOND_GUARDIAN,
                    0x5DECF5,
                    0x2F3B46,
                    new Item.Properties()));

    public static final DeferredItem<WaterSpiritUmbrellaItem> WATER_SPIRIT_UMBRELLA =
            ITEMS.register("water_spirit_umbrella",
                    () -> new WaterSpiritUmbrellaItem(new Item.Properties().stacksTo(1).durability(120)));
    public static final DeferredItem<Item> WATER_SPIRIT_SPAWN_EGG = registerItem("nijouin_hazuki_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ModEntities.WATER_SPIRIT,
                    0x52484E,
                    0x3F3861,
                    new Item.Properties()));

    // ===== 变身法杖（并入自 mod_92152f0a 工程）=====
    public static final DeferredItem<TransformationWandItem> TRANSFORMATION_WAND =
            ITEMS.register("transformation_wand",
                    () -> new TransformationWandItem(new Item.Properties().stacksTo(1).durability(1500)));

    // ===== 菜刀（并入自 knife 工程）：菜刀 / 生锈菜刀 / 生物肉 =====

    /** 菜刀：总攻击伤害 4（玩家基础 1 + 修正 3），出手略快于剑。 */
    private static final ItemAttributeModifiers CLEAVER_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .build();

    /** 生锈菜刀：攻击伤害修正 -1，与玩家基础伤害相抵为 0。 */
    private static final ItemAttributeModifiers RUSTY_CLEAVER_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, -1.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .build();

    public static final DeferredItem<CleaverItem> CLEAVER = ITEMS.register("cleaver",
            () -> new CleaverItem(new Item.Properties().stacksTo(1).attributes(CLEAVER_ATTRIBUTES)));

    public static final DeferredItem<RustyCleaverItem> RUSTY_CLEAVER = ITEMS.register("rusty_cleaver",
            () -> new RustyCleaverItem(new Item.Properties().stacksTo(1).setNoRepair().attributes(RUSTY_CLEAVER_ATTRIBUTES)));

    public static final DeferredItem<MobMeatItem> MOB_MEAT = ITEMS.register("mob_meat",
            () -> new MobMeatItem(new Item.Properties().stacksTo(16).food(ModFoods.MOB_MEAT)));

    // ===== 手术刀（击杀有头生物取头 + 留身体）=====

    /** 手术刀：总攻击伤害 1（玩家基础 1 + 修正 0），出手速度同剑。 */
    private static final ItemAttributeModifiers SCALPEL_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 0.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND)
            .build();

    public static final DeferredItem<ScalpelItem> SCALPEL = ITEMS.register("scalpel",
            () -> new ScalpelItem(new Item.Properties().stacksTo(1).durability(150)
                    .attributes(SCALPEL_ATTRIBUTES)));

    /** 无头身体物品：右键 2 格高空间还原为冻结的真实生物实体，携带来源生物完整 NBT。 */
    public static final DeferredItem<DecapitatedBodyItem> DECAPITATED_BODY = ITEMS.register("decapitated_body",
            () -> new DecapitatedBodyItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
