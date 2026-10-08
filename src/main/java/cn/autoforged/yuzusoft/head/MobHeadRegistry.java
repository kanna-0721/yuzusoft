package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.block.custom.MobSkullType;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 「实体类型 ↔ 头颅物品」的统一双向映射，供头颅掉落、手术刀击杀、身体方块复活/融合共用。
 * <p>
 * 采用懒绑定：本类可能在注册表填充前被加载，直接取 {@code DeferredHolder#get()} 会抛异常，
 * 故推迟到首次查询时才构建。原版只收录需求列出的 4 种（僵尸/骷髅/苦力怕/猪灵）。
 */
public final class MobHeadRegistry {
    private MobHeadRegistry() {}

    /** 实体类型 -> 头颅物品（yuzusoft 生物头颅方块物品 + 原版头颅物品）。 */
    private static final Map<EntityType<?>, Item> BY_ENTITY = new LinkedHashMap<>();
    /** 头颅物品 -> 实体类型。 */
    private static final Map<Item, EntityType<?>> BY_ITEM = new LinkedHashMap<>();
    /** 实体类型 -> yuzusoft 头颅类型（仅 yuzusoft 生物；原版为 null）。 */
    private static final Map<EntityType<?>, MobSkullType> BY_SKULL = new LinkedHashMap<>();
    private static boolean bound = false;

    private static void ensureBound() {
        if (bound) {
            return;
        }
        bound = true;
        bind(ModEntities.GUARDIAN.get(), MobSkullType.MURASAME);
        bind(ModEntities.GUARDIAN_TRADER.get(), MobSkullType.TOMOTAKE_YOSHINO);
        bind(ModEntities.SHADOW_ASSASSIN.get(), MobSkullType.HITACHI_MAKO);
        bind(ModEntities.DETONATOR_THROWING_MONSTER.get(), MobSkullType.NENE);
        bind(ModEntities.FROST_GUARDIAN.get(), MobSkullType.INABA_MEGURU);
        bind(ModEntities.FROST_GUARDIAN_V2.get(), MobSkullType.SHIKIBE_MAYU);
        bind(ModEntities.FLOATING_SENTINEL.get(), MobSkullType.MITSUKASA_AYASE);
        bind(ModEntities.VILLAGE_GUARDIAN.get(), MobSkullType.ARIHARA_NANAMI);
        bind(ModEntities.SPRINKLER_CREEP.get(), MobSkullType.AKIZUKI_KANNA);
        bind(ModEntities.GUITAR_MONSTER.get(), MobSkullType.HARUMI_ENA);
        bind(ModEntities.SLEEPY_SPIRIT.get(), MobSkullType.FUTAMIHARA_RIRIKO);
        bind(ModEntities.FLASHBANG_MONSTER.get(), MobSkullType.NABARI_ANJU);
        bind(ModEntities.HAMMER_WIELDER.get(), MobSkullType.SHIIBA_TSUMUGI);
        bind(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), MobSkullType.YARAI_MIU);
        bind(ModEntities.CAT_0721.get(), MobSkullType.MIKADO_TAKANORI);
        bind(ModEntities.DUAL_FORM_MOB.get(), MobSkullType.SHIRAYUKI_NOA);
        bind(ModEntities.HUMANOID_CREATURE.get(), MobSkullType.TANIKAZE_AMANE);
        bind(ModEntities.SUZUNE.get(), MobSkullType.SHIOYAMA_SUZUNE);
        bind(ModEntities.DIAMOND_GUARDIAN.get(), MobSkullType.KOHIBARI_KURUMI);
        bind(ModEntities.EVIL_NANAMI.get(), MobSkullType.EVIL_NANAMI);
        bind(ModEntities.WATER_SPIRIT.get(), MobSkullType.NIJOUIN_HAZUKI);
        bind(ModEntities.SHIMAGOE_TSUKUMI.get(), MobSkullType.SHIMAGOE_TSUKUMI);

        // 原版 4 种（仅需求列出的）
        bindVanilla(EntityType.ZOMBIE, Items.ZOMBIE_HEAD);
        bindVanilla(EntityType.SKELETON, Items.SKELETON_SKULL);
        bindVanilla(EntityType.CREEPER, Items.CREEPER_HEAD);
        bindVanilla(EntityType.PIGLIN, Items.PIGLIN_HEAD);
    }

    private static void bind(EntityType<?> entity, MobSkullType type) {
        Item item = ModItems.headItem(type).get();
        BY_ENTITY.put(entity, item);
        BY_ITEM.put(item, entity);
        BY_SKULL.put(entity, type);
    }

    private static void bindVanilla(EntityType<?> entity, Item item) {
        BY_ENTITY.put(entity, item);
        BY_ITEM.put(item, entity);
    }

    /** 该实体是否「有头」（可被手术刀取头）。 */
    public static boolean hasHead(EntityType<?> type) {
        ensureBound();
        return BY_ENTITY.containsKey(type);
    }

    /** 实体类型对应的头颅物品；无则 null。 */
    @Nullable
    public static Item headFor(EntityType<?> type) {
        ensureBound();
        return BY_ENTITY.get(type);
    }

    /** 头颅物品对应的实体类型；非头颅物品返回 null。 */
    @Nullable
    public static EntityType<?> entityFor(Item item) {
        ensureBound();
        return BY_ITEM.get(item);
    }

    /** yuzusoft 头颅类型；原版或未知返回 null。 */
    @Nullable
    public static MobSkullType skullFor(EntityType<?> type) {
        ensureBound();
        return BY_SKULL.get(type);
    }

    /** 所有「有 yuzusoft 头颅」的实体类型（创造栏「柚子社原材料」据此枚举身体）。 */
    public static java.util.Set<EntityType<?>> yuzusoftHeadTypes() {
        ensureBound();
        return java.util.Collections.unmodifiableSet(BY_SKULL.keySet());
    }
}