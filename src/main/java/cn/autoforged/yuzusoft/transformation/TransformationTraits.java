package cn.autoforged.yuzusoft.transformation;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;

import java.util.Set;

/**
 * 变身形态的原版生物特性表。
 * <p>
 * 尽量复用原版的数据标签与实体属性，让"变成什么就像什么"：
 * <ul>
 *     <li>{@code fireImmune()} / {@code FALL_DAMAGE_IMMUNE} / {@code FREEZE_IMMUNE_ENTITY_TYPES} /
 *     {@code CAN_BREATHE_UNDER_WATER}：对应原版 {@code Entity.isInvulnerableTo} 的免伤判定；</li>
 *     <li>{@code isSensitiveToWater()}：原版仅末影人/雪傀儡/烈焰人/炽足兽重写，沾水每刻掉 1 点；</li>
 *     <li>{@code WaterAnimal} 系列离水窒息；</li>
 *     <li>{@code IGNORES_POISON_AND_REGEN}：亡灵免疫中毒与生命恢复；</li>
 *     <li>弹射物免疫：旋风人反弹弹射物（风弹除外）、末影人被弹射物命中即瞬移躲避。</li>
 * </ul>
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationTraits {
    /** 原版仅这 4 种生物重写了 {@code isSensitiveToWater()}。 */
    private static final Set<EntityType<?>> WATER_SENSITIVE = Set.of(
            EntityType.ENDERMAN, EntityType.SNOW_GOLEM, EntityType.BLAZE, EntityType.STRIDER);

    /** 原版会因离水而窒息的水生生物（水生动物的 {@code handleAirSupply} 逻辑）。 */
    private static final Set<EntityType<?>> LAND_SUFFOCATING = Set.of(
            EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN,
            EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH, EntityType.TADPOLE,
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);

    /** 天生会飞的生物：变身即获得飞行能力，无需左键触发。 */
    private static final Set<EntityType<?>> FLYING = Set.of(
            EntityType.BAT, EntityType.BEE, EntityType.PARROT, EntityType.ALLAY, EntityType.VEX,
            EntityType.PHANTOM, EntityType.GHAST, EntityType.WITHER, EntityType.ENDER_DRAGON,
            EntityType.BLAZE, EntityType.BREEZE);

    /** 能爬墙的生物（蜘蛛类，原版由 {@code Spider.onClimbable} 实现）。 */
    private static final Set<EntityType<?>> CLIMBING = Set.of(
            EntityType.SPIDER, EntityType.CAVE_SPIDER);

    /** 原版 {@code Spider.canBeAffected} 直接屏蔽中毒（洞穴蜘蛛未覆写，共用同一份）。 */
    private static final Set<EntityType<?>> POISON_IMMUNE = Set.of(
            EntityType.SPIDER, EntityType.CAVE_SPIDER);

    /** 原版 {@code WitherSkeleton} 与 {@code WitherBoss} 的 {@code canBeAffected} 直接屏蔽凋零。 */
    private static final Set<EntityType<?>> WITHER_IMMUNE = Set.of(
            EntityType.WITHER_SKELETON, EntityType.WITHER);

    /** 原版 {@code addEffect} 直接返回 false、拒绝一切状态效果的生物。 */
    private static final Set<EntityType<?>> EFFECT_IMMUNE = Set.of(
            EntityType.WITHER, EntityType.ENDER_DRAGON);

    private TransformationTraits() {}

    /** 该形态是否对这类伤害完全免疫（复刻原版 {@code Entity.isInvulnerableTo} 与方块豁免）。 */
    public static boolean blocksDamage(EntityType<?> type, DamageSource source, LivingEntity entity) {
        if (type.fireImmune() && source.is(DamageTypeTags.IS_FIRE)) return true;                 // 烈焰人/岩浆怪/僵尸猪灵/凋灵等
        if (type.is(EntityTypeTags.FALL_DAMAGE_IMMUNE) && source.is(DamageTypeTags.IS_FALL)) return true;  // 猫/铁傀儡等
        if (type.is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES) && source.is(DamageTypeTags.IS_FREEZING)) return true; // 雪傀儡/北极熊/流浪者
        // 水下呼吸：仅当确实泡在水里时免于溺水，避免连带免除"离水窒息"伤害
        if (type.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER) && source.is(DamageTypeTags.IS_DROWNING)
                && entity.isInWaterOrBubble()) return true;                                      // 水生/亡灵
        if (source.is(DamageTypes.SWEET_BERRY_BUSH) && (type == EntityType.FOX || type == EntityType.BEE)) return true; // 原版豁免
        // 弹射物免疫：旋风人反弹几乎一切弹射物（原版唯一例外是风弹，无法反弹），
        // 末影人被弹射物命中即瞬移躲避，二者都等同于不吃弹射物伤害。
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            if (type == EntityType.BREEZE && !source.is(DamageTypes.WIND_CHARGE)) return true;
            if (type == EntityType.ENDERMAN) return true;
        }
        return false;
    }

    /**
     * 该形态是否拒绝这一状态效果。
     * <p>
     * 完整复刻原版 {@code LivingEntity.canBeAffected} 及其 4 处子类覆写：
     * <ul>
     *     <li>{@code WitherBoss.addEffect} / {@code EnderDragon.addEffect} → 拒绝一切效果；</li>
     *     <li>{@code WitherBoss.canBeAffected} / {@code WitherSkeleton.canBeAffected} → 屏蔽凋零；</li>
     *     <li>{@code Spider.canBeAffected} → 屏蔽中毒（洞穴蜘蛛未覆写，共用同一份）；</li>
     *     <li>基类数据标签分支 {@code IMMUNE_TO_INFESTED} / {@code IMMUNE_TO_OOZING} /
     *     {@code IGNORES_POISON_AND_REGEN}（三者互斥，按原版 else-if 顺序）。</li>
     * </ul>
     */
    public static boolean resistsEffect(EntityType<?> type, Holder<MobEffect> effect) {
        if (type == null) return false;
        if (EFFECT_IMMUNE.contains(type)) return true;                                          // 凋灵 / 末影龙：拒绝一切
        MobEffect value = effect.value();
        if (value == MobEffects.WITHER.value() && WITHER_IMMUNE.contains(type)) return true;   // 凋灵骷髅 / 凋灵
        if (value == MobEffects.POISON.value() && POISON_IMMUNE.contains(type)) return true;   // 蜘蛛 / 洞穴蜘蛛
        // 原版 LivingEntity.canBeAffected 的标签分支
        if (type.is(EntityTypeTags.IMMUNE_TO_INFESTED)) return value == MobEffects.INFESTED.value();
        if (type.is(EntityTypeTags.IMMUNE_TO_OOZING)) return value == MobEffects.OOZING.value();
        if (!type.is(EntityTypeTags.IGNORES_POISON_AND_REGEN)) return false;
        return value == MobEffects.POISON.value() || value == MobEffects.REGENERATION.value();
    }

    /** 沾水/淋雨会持续掉血的形态（末影人、雪傀儡、烈焰人、炽足兽）。 */
    public static boolean isWaterSensitive(EntityType<?> type) {
        return WATER_SENSITIVE.contains(type);
    }

    /** 离水会窒息的水生形态。 */
    public static boolean suffocatesOnLand(EntityType<?> type) {
        return LAND_SUFFOCATING.contains(type);
    }

    /** 火焰免疫形态（用于顺带清除身上的火）。 */
    public static boolean isFireImmune(EntityType<?> type) {
        return type.fireImmune();
    }

    /** 天生会飞的形态：变身即拥有飞行能力（无需左键）。 */
    public static boolean canFly(EntityType<?> type) {
        return type != null && FLYING.contains(type);
    }

    /** 能爬墙的形态：贴着墙壁移动时可向上攀爬。 */
    public static boolean canClimb(EntityType<?> type) {
        return type != null && CLIMBING.contains(type);
    }
}