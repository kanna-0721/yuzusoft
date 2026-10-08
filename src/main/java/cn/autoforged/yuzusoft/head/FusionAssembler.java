package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.entity.ai.FusionAuraGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionDefenseFormGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionFollowOwnerGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionMeleeAttackGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionOwnerHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionRangedAttackGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionSelfDestructGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionSelfHealGoal;
import cn.autoforged.yuzusoft.entity.ai.FusionSummonGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.entity.custom.ShadowAssassinEntity;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * 融合生物组装：头与身不是同一种生物时，用「身体的实体类型 + 头颅物品」拼出。
 * <p>
 * 组装规则（对应需求）：
 * <ul>
 *   <li><b>外观</b>：直接用身体的 EntityType 生成实体，再把头颅物品放进 HEAD 槽——
 *       原版 {@code CustomHeadLayer} 会把它画在头顶，效果等同于「把头颅安放在该生物头上」；</li>
 *   <li><b>移动</b>：移速来自身体（属性基础值随身体 NBT 还原），身体原有 AI 全部清空，
 *       只注入通用的移动/环顾 goal；</li>
 *   <li><b>头部特性</b>：攻击力与阵营（是否主动攻击玩家）取头部生物，招牌远程能力取自
 *       {@link HeadAbilityTable}；</li>
 *   <li><b>血量 / 护甲</b>：取头身两边的平均值；</li>
 *   <li><b>名称</b>：身体在前、头在后，见 {@link FusionNames}。</li>
 * </ul>
 */
public final class FusionAssembler {
    private FusionAssembler() {}

    private static final double FALLBACK_HEALTH = 20.0D;
    private static final double FALLBACK_ATTACK = 2.0D;

    /**
     * 组装并放入世界。
     *
     * @param bodyType 身体的实体类型（死亡生物的类型）
     * @param bodyTag  身体死亡时的完整 NBT
     * @param headType 头部的实体类型
     * @param headStack 头颅物品（会被装进 HEAD 槽）
     * @param pos      生成位置（脚下中心）
     * @return 生成的融合生物；无法组装返回 {@code null}
     */
    @Nullable
    public static Entity assemble(ServerLevel level, @Nullable EntityType<?> bodyType, CompoundTag bodyTag,
                                  EntityType<?> headType, ItemStack headStack, Vec3 pos) {
        if (bodyType == null) {
            return null;
        }
        Entity rawBody = bodyType.create(level);
        if (!(rawBody instanceof PathfinderMob body)) {
            if (rawBody != null) {
                rawBody.discard();
            }
            return null;
        }

        // 身体完整 NBT：属性基础值（含移速）、魅惑契约等状态一并还原
        body.load(bodyTag);
        // 原实体已死亡，换新 UUID，避免与原记录冲突导致 addFreshEntity 被拒
        body.setUUID(UUID.randomUUID());
        body.moveTo(pos.x, pos.y, pos.z, body.getYRot(), 0.0F);
        body.setDeltaMovement(Vec3.ZERO);
        body.fallDistance = 0.0F;
        body.deathTime = 0;
        body.hurtTime = 0;
        // 身体只负责移动：装备不随融合体带走（死亡时该掉的已在死亡瞬间掉过，保留会重复产出）
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            body.setItemSlot(slot, ItemStack.EMPTY);
        }

        // 头部临时实体：只用于读取头部生物的属性与阵营，用完即弃（未入世界，discard 安全）
        Entity headEntity = headType.create(level);

        applyAttributes(body, headEntity);
        body.setHealth(body.getMaxHealth());

        // 头颅装进 HEAD 槽：原版 CustomHeadLayer 画在头顶；死亡时由 HeadDropHandler 保证掉落返还
        body.setItemSlot(EquipmentSlot.HEAD, headStack);
        // 标记为融合生物：交互层（如芳乃头交易）据此区分「融合体」与「普通生物戴头颅」
        body.setData(ModAttachments.FUSION_BODY.get(), Boolean.TRUE);
        // 音效同样只看头部：先静音身体自身音效，再由 FusionSoundEvents 按头颅改播头部音效
        body.setSilent(true);
        // 头部为乃爱这类「坐骑」时标记可骑乘（客户端 R/X 键与骑乘驱动据此生效）
        if (HeadAbilityTable.hasTrait(headType, HeadAbilityTable.Trait.RIDEABLE)) {
            body.setData(ModAttachments.FUSION_RIDEABLE.get(), Boolean.TRUE);
        }

        installAi(body, headType, headEntity);
        if (headEntity != null) {
            headEntity.discard();
        }

        // 名称规则：身体在前、头在后（见 FusionNames）
        body.setCustomName(FusionNames.of(bodyType, headType));
        body.setCustomNameVisible(true);

        if (!level.addFreshEntity(body)) {
            return null;
        }
        return body;
    }

    // ------------------------------------------------------------------
    //  属性：血量/护甲取平均，攻击力取头部
    // ------------------------------------------------------------------

    private static void applyAttributes(Mob body, @Nullable Entity headEntity) {
        double bodyMax = attribute(body, Attributes.MAX_HEALTH, FALLBACK_HEALTH);
        double headMax = attribute(headEntity, Attributes.MAX_HEALTH, bodyMax);
        setBase(body, Attributes.MAX_HEALTH, (bodyMax + headMax) / 2.0D);

        double bodyArmor = attribute(body, Attributes.ARMOR, 0.0D);
        double headArmor = attribute(headEntity, Attributes.ARMOR, bodyArmor);
        setBase(body, Attributes.ARMOR, (bodyArmor + headArmor) / 2.0D);

        // 攻击力取头部；头部没有该属性（被动型）则退回身体的值。
        // 注意：动物类身体的属性表里没有 ATTACK_DAMAGE，这里 setBase 会是空操作，
        // 真正的近战伤害由 FusionMeleeAttackGoal 携带（见 meleeDamage）。
        setBase(body, Attributes.ATTACK_DAMAGE, meleeDamage(body, headEntity));
    }

    /** 融合体的近战伤害：优先头部，其次身体，最后兜底。 */
    private static double meleeDamage(Mob body, @Nullable Entity headEntity) {
        double headAttack = attribute(headEntity, Attributes.ATTACK_DAMAGE, -1.0D);
        if (headAttack > 0.0D) {
            return headAttack;
        }
        return attribute(body, Attributes.ATTACK_DAMAGE, FALLBACK_ATTACK);
    }

    private static double attribute(@Nullable Entity entity, Holder<Attribute> attribute, double fallback) {
        if (!(entity instanceof LivingEntity living)) {
            return fallback;
        }
        AttributeInstance instance = living.getAttribute(attribute);
        return instance == null ? fallback : instance.getBaseValue();
    }

    private static void setBase(LivingEntity entity, Holder<Attribute> attribute, double value) {
        // 属性只能在实体类型注册时（AttributeSupplier）声明，运行时补不了
        // （AttributeMap 没有 registerAttribute，AttributeSupplier#createInstance
        // 对未声明的属性返回 null）。属性表里没有该项时直接跳过即可。
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    // ------------------------------------------------------------------
    //  AI：移动通用件 + 头部特性
    // ------------------------------------------------------------------

    /**
     * 装配 AI。除「移动通用件」外，一切行为/阵营都取自头部档案
     * （{@link HeadAbilityTable.HeadProfile}）：融合体就是「头部对应的那只原生物」，
     * 攻击方式、命中附加、光环、自愈、召唤、防御形态、驯服归属全部照抄头部；
     * 身体不贡献任何行为，只贡献移速（移速随身体 NBT 还原，这里不动它）。
     */
    private static void installAi(PathfinderMob body, EntityType<?> headType, @Nullable Entity headEntity) {
        body.goalSelector.removeAllGoals(goal -> true);
        body.targetSelector.removeAllGoals(goal -> true);
        body.setTarget(null);

        HeadAbilityTable.HeadProfile profile = HeadAbilityTable.profileFor(headType);
        Set<HeadAbilityTable.Trait> traits = profile.traits();
        HeadAbilityTable.Ability ability = profile.ability();
        HeadAbilityTable.OnHit onHit = profile.onHit();
        float melee = (float) meleeDamage(body, headEntity);
        boolean tameable = profile.tameFood() != null;

        body.goalSelector.addGoal(0, new FloatGoal(body));

        if (!profile.isPassive()) {
            if (traits.contains(HeadAbilityTable.Trait.SELF_DESTRUCT)) {
                // 苦力怕 / 铃音：靠到目标身边蓄力自爆
                body.goalSelector.addGoal(1, new FusionSelfDestructGoal(body, profile.selfDestructRadius()));
            } else if (ability.isRanged()) {
                // 混合近远战（惠 6 格、绫濑 2.55 格）：近身时切近战
                if (profile.hybridMeleeRange() > 0.0F) {
                    body.goalSelector.addGoal(1, new FusionMeleeAttackGoal(body, 1.0D, true, melee, onHit,
                            profile.hybridMeleeRange()));
                }
                body.goalSelector.addGoal(2, new FusionRangedAttackGoal(body, ability));
            } else {
                body.goalSelector.addGoal(2, new FusionMeleeAttackGoal(body, 1.0D, true, melee, onHit, 0.0F));
            }
        }

        // 可驯服头部：跟随主人
        if (tameable) {
            body.goalSelector.addGoal(4, new FusionFollowOwnerGoal(body));
        }
        body.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(body, 1.0D));
        body.goalSelector.addGoal(6, new LookAtPlayerGoal(body, Player.class, 8.0F));
        body.goalSelector.addGoal(7, new RandomLookAroundGoal(body));

        // 常驻行为（无旗标、canUse 恒真，只做定时效果，不与移动争旗标）
        if (profile.aura() != null) {
            body.goalSelector.addGoal(8, new FusionAuraGoal(body, profile.aura()));
        }
        if (profile.selfHealAmount() > 0.0F) {
            body.goalSelector.addGoal(8,
                    new FusionSelfHealGoal(body, profile.selfHealAmount(), profile.selfHealInterval()));
        }
        if (profile.specials().contains(HeadAbilityTable.Special.DEFENSE_FORM)) {
            body.goalSelector.addGoal(8, new FusionDefenseFormGoal(body));
        }
        if (profile.specials().contains(HeadAbilityTable.Special.SUMMON_GUARDIAN)) {
            body.goalSelector.addGoal(8, new FusionSummonGoal(body));
        }

        // 0721 系头部：融合体并入 0721 族群——照原样攻击玩家/铁傀儡/村民，参与族群反击与支援；
        // 族员一侧由 Group0721Helper 把「顶 0721 头颅的融合体」认作自己人，故不会互攻。
        if (Group0721Helper.isGroup0721Head(headType)) {
            body.targetSelector.addGoal(1, new GroupHurtByTargetGoal(body));
            body.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(body, Player.class, true,
                    target -> !Group0721Helper.isIgnoredByGroup(target)));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, IronGolem.class, true));
            body.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(body, Villager.class, true));
            body.targetSelector.addGoal(5, new GroupSupportTargetGoal(body));
            return;
        }

        // 完全被动的头部（七海 / 来海 / 里子）：原生物的目标选择器是空的
        if (profile.isPassive()) {
            return;
        }

        // 可驯服头部：主人被打时护主
        if (tameable) {
            body.targetSelector.addGoal(0, new FusionOwnerHurtByTargetGoal(body));
        }
        body.targetSelector.addGoal(1, new HurtByTargetGoal(body));
        // 性格（阵营/主动攻击玩家）只看头部：敌对生物的头 → 主动攻击玩家。
        // 身体只负责移动，不影响敌意。
        if (headEntity instanceof Enemy) {
            body.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(body, Player.class, true));
            if (profile.attacksIronGolems()) {
                body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, IronGolem.class, true));
            }
        }
        // 「守卫型」头部（芳乃 / 丛雨 / 乃爱）：主动清理怪物。
        // 目标表与原生物一致；0721 族群互不攻击由各实体的 isAlliedTo 自动保证，
        // 属 0721 的身体接上守卫头后依旧不会打自己人（原版索敌会跳过盟友）。
        if (traits.contains(HeadAbilityTable.Trait.HOSTILE_TO_MONSTERS)) {
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, ShadowAssassinEntity.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, Monster.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, Slime.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, MagmaCube.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, Hoglin.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, Shulker.class, true));
            body.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(body, Phantom.class, true));
        }
    }
}