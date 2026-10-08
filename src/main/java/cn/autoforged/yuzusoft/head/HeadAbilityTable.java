package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.DetonatorProjectileEntity;
import cn.autoforged.yuzusoft.entity.custom.FlashbangProjectile;
import cn.autoforged.yuzusoft.entity.custom.FrostBoltV2Projectile;
import cn.autoforged.yuzusoft.entity.custom.FrostProjectileEntity;
import cn.autoforged.yuzusoft.entity.custom.LevitationBulletEntity;
import cn.autoforged.yuzusoft.entity.custom.ShadowDartEntity;
import cn.autoforged.yuzusoft.entity.custom.WaterBallEntity;
import cn.autoforged.yuzusoft.entity.custom.WaterOrbEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 「头部档案表」：融合生物就是「头部对应的那只原生物」——原生物有什么特性、怎么打人、
 * 有没有光环 / 召唤 / 自愈 / 防御形态 / 驯服，全部照抄到融合体身上；身体只负责移速。
 * <p>
 * 每只头部对应一份 {@link HeadProfile}，字段与「原生物确实做过的事情」一一对应：
 * <ul>
 *   <li>{@link Trait} —— 行为特性（自爆 / 交易 / 可骑乘 / 主动清怪）；</li>
 *   <li>{@link Ability} —— 招牌攻击（远程投射物，或春的声波）；</li>
 *   <li>{@link OnHit} —— 近战命中附加（效果 + 吸血）；</li>
 *   <li>{@link Aura} —— 周期光环（七海 / Evil 七海的生命恢复）；</li>
 *   <li>{@link Special} —— 形态类特殊能力（真雪防御形态、芳乃召唤丛雨）；</li>
 *   <li>自愈、驯服食物、自爆半径、近距离切换近战的阈值等零散参数。</li>
 * </ul>
 * 是否主动攻击玩家不查表：头部实体本身是 {@code Enemy}（怪物）时即为敌对——
 * 与「身体是什么」无关。
 */
public final class HeadAbilityTable {
    private HeadAbilityTable() {}

    /** 头部生物的非攻击行为 / 阵营特性（可组合）。 */
    public enum Trait {
        /** 苦力怕式自爆：靠到目标身边蓄力后爆炸（苦力怕、铃音）。 */
        SELF_DESTRUCT,
        /** 可交易：右键打开交易界面（芳乃）。 */
        TRADER,
        /** 可骑乘：玩家可骑乘并操控（乃爱）。 */
        RIDEABLE,
        /** 主动攻击怪物：与守卫类头部一致，追击 Monster / 史莱姆 / 幻翼等（芳乃、丛雨、乃爱）。 */
        HOSTILE_TO_MONSTERS
    }

    /** 需要专门 Goal 承载的形态类特殊能力。 */
    public enum Special {
        /** 真雪：目标贴近时切防御形态（获得 FROST_BARRIER、攻速下降），到期/冷却后回攻形态。 */
        DEFENSE_FORM,
        /** 芳乃：愤怒且看得见目标时周期性召唤丛雨。 */
        SUMMON_GUARDIAN
    }

    /** 投射物工厂：与原生物一样的 {@code (Level, LivingEntity)} 构造形式。 */
    @FunctionalInterface
    public interface ProjectileFactory {
        Projectile create(Level level, LivingEntity shooter);
    }

    /**
     * 一项头部攻击能力。
     *
     * @param projectile    投射物工厂；{@code null} 表示没有投射物
     * @param velocity      弹速
     * @param inaccuracy    散布
     * @param cooldownTicks 两次攻击间隔（刻）
     * @param range         开火距离（格），超出则持续追击
     * @param sonicBoom     true = 春的声波（无投射物，直接射线伤害 + 击退）
     */
    public record Ability(@Nullable ProjectileFactory projectile, float velocity, float inaccuracy,
                          int cooldownTicks, float range, boolean sonicBoom) {
        public static final Ability NONE = new Ability(null, 0.0F, 0.0F, 0, 0.0F, false);

        public boolean isRanged() {
            return projectile != null || sonicBoom;
        }
    }

    /**
     * 近战命中附加（与原生物 {@code doHurtTarget} 一致）。
     *
     * @param effect   命中后施加的效果；{@code null} 表示无
     * @param duration 效果时长（刻）
     * @param amplifier 效果等级（0 = I 级）
     * @param lifesteal 吸血比例（0 = 无；美羽 0.5 = 回血 50% 伤害）
     */
    public record OnHit(@Nullable Holder<MobEffect> effect, int duration, int amplifier, float lifesteal) {
        public static final OnHit NONE = new OnHit(null, 0, 0, 0.0F);

        public boolean isEmpty() {
            return effect == null && lifesteal <= 0.0F;
        }
    }

    /**
     * 周期光环（原生物 {@code aiStep} 里的定时扫描）。
     *
     * @param interval       施放间隔（刻）
     * @param innerRadius    内层半径（格），此范围内用内层等级
     * @param innerAmplifier 内层等级
     * @param outerRadius    外层半径（格）
     * @param outerAmplifier 外层等级
     * @param effect         施加的效果
     * @param duration       效果时长（刻）
     * @param groupOnly      true = 只作用于 0721 族员（Evil 七海）；false = 除自己外所有存活实体（七海）
     */
    public record Aura(int interval, double innerRadius, int innerAmplifier, double outerRadius,
                       int outerAmplifier, Holder<MobEffect> effect, int duration, boolean groupOnly) {
    }

    /** 一只头部生物的完整行为档案。 */
    public static final class HeadProfile {
        private final Set<Trait> traits = EnumSet.noneOf(Trait.class);
        private final Set<Special> specials = EnumSet.noneOf(Special.class);
        private Ability ability = Ability.NONE;
        private OnHit onHit = OnHit.NONE;
        @Nullable
        private Aura aura;
        private float selfHealAmount;
        private int selfHealInterval = 20;
        @Nullable
        private Item tameFood;
        private float selfDestructRadius = 3.0F;
        private float hybridMeleeRange;
        private boolean attackIronGolems;
        private boolean passive;

        HeadProfile trait(Trait... values) {
            this.traits.addAll(Arrays.asList(values));
            return this;
        }

        HeadProfile special(Special... values) {
            this.specials.addAll(Arrays.asList(values));
            return this;
        }

        HeadProfile ability(Ability value) {
            this.ability = value;
            return this;
        }

        HeadProfile onHit(OnHit value) {
            this.onHit = value;
            return this;
        }

        HeadProfile aura(Aura value) {
            this.aura = value;
            return this;
        }

        HeadProfile selfHeal(float amount, int interval) {
            this.selfHealAmount = amount;
            this.selfHealInterval = interval;
            return this;
        }

        HeadProfile tame(Item food) {
            this.tameFood = food;
            return this;
        }

        HeadProfile selfDestruct(float radius) {
            this.selfDestructRadius = radius;
            return this;
        }

        /** 近距离切换近战的阈值（格）；0 = 纯远程，不做近战切换。 */
        HeadProfile hybrid(float range) {
            this.hybridMeleeRange = range;
            return this;
        }

        HeadProfile attackIronGolems() {
            this.attackIronGolems = true;
            return this;
        }

        /**
         * 完全被动：既不主动索敌，也不因挨打反击（七海 / 来海 / 里子——
         * 原生物的目标选择器是空的）。这类融合体只会闲逛、环顾，外加头部带来的光环等常驻行为。
         */
        HeadProfile passive() {
            this.passive = true;
            return this;
        }

        public Set<Trait> traits() {
            return this.traits;
        }

        public Set<Special> specials() {
            return this.specials;
        }

        public Ability ability() {
            return this.ability;
        }

        public OnHit onHit() {
            return this.onHit;
        }

        @Nullable
        public Aura aura() {
            return this.aura;
        }

        public float selfHealAmount() {
            return this.selfHealAmount;
        }

        public int selfHealInterval() {
            return this.selfHealInterval;
        }

        @Nullable
        public Item tameFood() {
            return this.tameFood;
        }

        public float selfDestructRadius() {
            return this.selfDestructRadius;
        }

        public float hybridMeleeRange() {
            return this.hybridMeleeRange;
        }

        public boolean attacksIronGolems() {
            return this.attackIronGolems;
        }

        public boolean isPassive() {
            return this.passive;
        }
    }

    private static final float DEFAULT_VELOCITY = 1.2F;
    private static final float DEFAULT_INACCURACY = 2.0F;
    private static final int DEFAULT_COOLDOWN = 40;
    private static final float DEFAULT_RANGE = 16.0F;

    private static final Map<EntityType<?>, HeadProfile> BY_HEAD = new HashMap<>();
    /** 未收录的头部：退化近战、无附加。 */
    private static final HeadProfile DEFAULT = new HeadProfile();
    private static boolean bound = false;

    private static void ensureBound() {
        if (bound) {
            return;
        }
        bound = true;

        // ---- 守卫系（不主动攻击玩家，主动清怪）----
        // 丛雨：近战 ATK 6
        put(ModEntities.GUARDIAN.get(), new HeadProfile().trait(Trait.HOSTILE_TO_MONSTERS));
        // 芳乃：近战 ATK 8 + 交易 + 清怪 + 愤怒时召唤丛雨
        put(ModEntities.GUARDIAN_TRADER.get(), new HeadProfile()
                .trait(Trait.TRADER, Trait.HOSTILE_TO_MONSTERS)
                .special(Special.SUMMON_GUARDIAN));
        // 乃爱：可骑乘的飞行坐骑 + 清怪 + 每 20 刻自愈 2
        put(ModEntities.DUAL_FORM_MOB.get(), new HeadProfile()
                .trait(Trait.RIDEABLE, Trait.HOSTILE_TO_MONSTERS)
                .selfHeal(2.0F, 20));

        // ---- 0721 系（阵营由 FusionAssembler 走族群目标表）----
        // 茉子：暗影飞镖 v2.0 / 散布 0.6 / 间隔 30 / 射程 15；近战附虚弱 100 刻
        put(ModEntities.SHADOW_ASSASSIN.get(), new HeadProfile()
                .ability(ranged(ShadowDartEntity::new, 2.0F, 0.6F, 30, 15.0F))
                .onHit(new OnHit(MobEffects.WEAKNESS, 100, 0, 0.0F)));
        // 宁宁：起爆器 v1.2 / 散布 2.0 / 间隔 40 / 射程 15
        put(ModEntities.DETONATOR_THROWING_MONSTER.get(), new HeadProfile()
                .ability(ranged(DetonatorProjectileEntity::new, 1.2F, 2.0F, 40, 15.0F)));
        // 环：水球 v1.5 / 散布 0 / 间隔 60 / 射程 12；近战附 0721 效果 300 刻
        put(ModEntities.SPRINKLER_CREEP.get(), new HeadProfile()
                .ability(ranged(WaterBallEntity::new, 1.5F, 0.0F, 60, 12.0F))
                .onHit(new OnHit(ModEffects.EFFECT_0721, 300, 0, 0.0F)));
        // 美羽：近战 ATK 6 + 吸血 50% + 0721 效果 100 刻
        put(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), new HeadProfile()
                .onHit(new OnHit(ModEffects.EFFECT_0721, 100, 0, 0.5F)));
        // 三田：近战 ATK 4
        put(ModEntities.CAT_0721.get(), new HeadProfile());
        // Evil 七海：近战 ATK 3 + 光环（8 格内 0721 族员生命恢复 II / 8-16 格 I）
        put(ModEntities.EVIL_NANAMI.get(), new HeadProfile()
                .aura(new Aura(20, 8.0D, 1, 16.0D, 0, MobEffects.REGENERATION, 100, true)));

        // ---- 可驯服系（非敌对，跟随主人）----
        // 惠：面包驯服；冰锥 v1.4 / 散布 1.0 / 间隔 40 / 射程 32；6 格内切近战附缓慢 60 刻
        put(ModEntities.FROST_GUARDIAN.get(), new HeadProfile()
                .tame(Items.BREAD)
                .ability(ranged(FrostProjectileEntity::new, 1.4F, 1.0F, 40, 32.0F))
                .onHit(new OnHit(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, 0.0F))
                .hybrid(6.0F));
        // 天音：面包驯服；近战 ATK 8
        put(ModEntities.HUMANOID_CREATURE.get(), new HeadProfile().tame(Items.BREAD));
        // 来海：曲奇驯服；不攻击
        put(ModEntities.DIAMOND_GUARDIAN.get(), new HeadProfile().tame(Items.COOKIE).passive());

        // ---- 敌对系（Enemy 头，主动攻击玩家）----
        // 真雪：冰弹 v1.2 / 散布 0 / 间隔 40 / 射程 24 + 防御形态
        put(ModEntities.FROST_GUARDIAN_V2.get(), new HeadProfile()
                .ability(ranged(FrostBoltV2Projectile::new, 1.2F, 0.0F, 40, 24.0F))
                .special(Special.DEFENSE_FORM));
        // 绫濑：浮游弹 v1.0 / 散布 0.2 / 间隔 40 / 射程 16；近战附飘浮 100 刻；2.55 格内切近战；追铁傀儡
        put(ModEntities.FLOATING_SENTINEL.get(), new HeadProfile()
                .ability(ranged(LevitationBulletEntity::new, 1.0F, 0.2F, 40, 16.0F))
                .onHit(new OnHit(MobEffects.LEVITATION, 100, 0, 0.0F))
                .hybrid(2.55F)
                .attackIronGolems());
        // 杏寿：闪光弹 v1.6 / 散布 0 / 间隔 60 / 射程 15；追铁傀儡
        put(ModEntities.FLASHBANG_MONSTER.get(), new HeadProfile()
                .ability(ranged(FlashbangProjectile::new, 1.6F, 0.0F, 60, 15.0F))
                .attackIronGolems());
        // 纺：近战 ATK 8 + 硬直 40 刻；追铁傀儡
        put(ModEntities.HAMMER_WIELDER.get(), new HeadProfile()
                .onHit(new OnHit(ModEffects.STUN, 40, 0, 0.0F))
                .attackIronGolems());
        // 春：声波（无投射物）间隔 80 / 射程 15；追铁傀儡
        put(ModEntities.GUITAR_MONSTER.get(), new HeadProfile()
                .ability(new Ability(null, 0.0F, 0.0F, 80, 15.0F, true))
                .attackIronGolems());
        // 铃音：自爆，半径 8 格
        put(ModEntities.SUZUNE.get(), new HeadProfile()
                .trait(Trait.SELF_DESTRUCT)
                .selfDestruct(8.0F));

        // ---- 中立 / 被动系（被打才还手或不还手）----
        // 七海：不攻击；光环（4 格内生命恢复 II / 8 格内 I）作用于除自己外所有存活实体
        put(ModEntities.VILLAGE_GUARDIAN.get(), new HeadProfile()
                .aura(new Aura(20, 4.0D, 1, 8.0D, 0, MobEffects.REGENERATION, 100, false))
                .passive());
        // 叶月：水弹 v1.35 / 散布 0.1 / 间隔 30 / 射程 16
        put(ModEntities.WATER_SPIRIT.get(), new HeadProfile()
                .ability(ranged(WaterOrbEntity::new, 1.35F, 0.1F, 30, 16.0F)));
        // 里子：被动生物，无攻击行为
        put(ModEntities.SLEEPY_SPIRIT.get(), new HeadProfile().passive());

        // ---- 原版头部 ----
        put(EntityType.CREEPER, new HeadProfile().trait(Trait.SELF_DESTRUCT).selfDestruct(3.0F));
        // 原版骷髅：射箭（与原版一致的弹速 / 散布）
        put(EntityType.SKELETON, new HeadProfile()
                .ability(ranged((level, shooter) -> new Arrow(level, shooter, new ItemStack(Items.ARROW), null),
                        1.6F, 14.0F, DEFAULT_COOLDOWN, DEFAULT_RANGE)));
    }

    private static void put(EntityType<?> head, HeadProfile profile) {
        BY_HEAD.put(head, profile);
    }

    private static Ability ranged(ProjectileFactory factory, float velocity, float inaccuracy,
                                  int cooldownTicks, float range) {
        return new Ability(factory, velocity, inaccuracy, cooldownTicks, range, false);
    }

    /** 头部生物对应的完整档案；未收录返回默认（近战、无附加）。 */
    public static HeadProfile profileFor(EntityType<?> headType) {
        ensureBound();
        HeadProfile profile = BY_HEAD.get(headType);
        return profile == null ? DEFAULT : profile;
    }

    /** 头部生物对应的攻击能力；未收录返回近战。 */
    public static Ability abilityFor(EntityType<?> headType) {
        return profileFor(headType).ability();
    }

    /** 头部生物对应的非攻击行为；未收录返回空集。 */
    public static Set<Trait> traitsFor(EntityType<?> headType) {
        return profileFor(headType).traits();
    }

    /** 头部生物是否具备某项特性。 */
    public static boolean hasTrait(EntityType<?> headType, Trait trait) {
        return traitsFor(headType).contains(trait);
    }
}