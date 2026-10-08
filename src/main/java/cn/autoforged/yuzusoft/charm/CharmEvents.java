package cn.autoforged.yuzusoft.charm;

import cn.autoforged.yuzusoft.charm.ai.CharmAvoidHazardGoal;
import cn.autoforged.yuzusoft.charm.ai.CharmFollowOwnerGoal;
import cn.autoforged.yuzusoft.charm.ai.CharmOwnerHurtByTargetGoal;
import cn.autoforged.yuzusoft.charm.ai.CharmOwnerHurtTargetGoal;
import cn.autoforged.yuzusoft.charm.ai.CharmRetaliateGoal;
import cn.autoforged.yuzusoft.charm.ai.CharmStateGoal;
import cn.autoforged.yuzusoft.entity.custom.Cat0721Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 魅惑系统全部服务端事件挂点（NeoForge 主事件总线）。
 */
public final class CharmEvents {
    private CharmEvents() {}

    /** 注入魅惑之瓶的原版战利品表：废弃矿井箱子、地牢箱子、普通试炼宝库。 */
    private static final Set<ResourceLocation> CHARM_BOTTLE_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/reward"));

    /**
     * 每个生物进场时注入契约目标 AI。
     * 所有目标都以"魅惑效果存在"为 canUse 前提，未魅惑时零开销；
     * 魅惑结束目标自然让位，原版 AI 完整保留，无需任何"恢复"操作。
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        mob.targetSelector.addGoal(1, new CharmOwnerHurtByTargetGoal(mob));
        mob.targetSelector.addGoal(2, new CharmOwnerHurtTargetGoal(mob));
        if (mob instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
            mob.targetSelector.addGoal(3, new CharmRetaliateGoal(pathfinderMob));
        }
        mob.goalSelector.addGoal(2, new CharmFollowOwnerGoal(mob));
        mob.goalSelector.addGoal(3, new CharmAvoidHazardGoal(mob));
        mob.goalSelector.addGoal(4, new CharmStateGoal(mob));
        // 继承魅惑：若本生物是由某个"被魅惑的召唤者"召出（带主人引用），则一并魅惑并继承契约
        Mob summoner = resolveSummoner(event.getEntity());
        if (summoner != null && CharmHelper.isCharmActive(summoner) && !CharmHelper.isCharmActive(mob)) {
            inheritCharmFromSummoner(mob, summoner);
        }
    }

    /**
     * 反查"召唤者"实体：
     * 1) 实现 {@link OwnableEntity} 的生物（标准主人引用），取 getOwner()；
     * 2) yuzusoft 的 Cat0721Entity，取自定义 summonerId；
     * 3) 恼鬼 Vex 无主人引用：仅在 MOB_SUMMONED 生成时，探测 8 格内被魅惑且在施法的唤魔者系。
     * 只在召唤者本身也是一个在场的 Mob 时返回。
     */
    @Nullable
    private static Mob resolveSummoner(Entity entity) {
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof Mob ownerMob) {
            return ownerMob;
        }
        if (entity instanceof Cat0721Entity cat && cat.getSummonerId() != null
                && entity.level() instanceof ServerLevel level
                && level.getEntity(cat.getSummonerId()) instanceof Mob summoner) {
            return summoner;
        }
        // 恼鬼：无主人引用，只能近身派生。唤魔者施法结束瞬间生成的恼鬼一定在唤魔者身边，
        // 且此时唤魔者仍处于施法态（isCastingSpell）；误判面收敛在这 8 格、施法中、被魅惑三条件同时命中。
        if (entity instanceof net.minecraft.world.entity.monster.Vex) {
            return entity.level().getEntitiesOfClass(
                            net.minecraft.world.entity.monster.SpellcasterIllager.class,
                            entity.getBoundingBox().inflate(8.0),
                            caster -> CharmHelper.isCharmActive(caster) && caster.isCastingSpell())
                    .stream().findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 子生物继承召唤者的魅惑：等时长（永久契约保持永久），主人转移到召唤者所属的主人。
     * 直接对效果做 addEffect + 写契约数据，与 {@link #onEffectAdded} 共用数据模型。
     */
    private static void inheritCharmFromSummoner(Mob child, Mob summoner) {
        net.minecraft.world.effect.MobEffectInstance summonerEffect =
                summoner.getEffect(CharmRegistry.CHARM_EFFECT);
        if (summonerEffect == null) {
            return;
        }
        int duration = summonerEffect.isInfiniteDuration()
                ? net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
                : summonerEffect.getDuration();
        // 移动速度基线沿用召唤者现值的原值（未压缩的原始速度），否则会在此被再次压缩
        CharmState summonerState = CharmHelper.getState(summoner);
        MobEffectInstance childEffect =
                new MobEffectInstance(CharmRegistry.CHARM_EFFECT, duration, 0, false, true);
        // 先记录契约骨架（速度恢复用），再写永久令牌与主人
        CharmState proto = new CharmState(
                summonerState != null ? summonerState.owner() : null,
                duration == net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION,
                CharmHelper.applyCharmSpeed(child, null));
        child.addEffect(childEffect, summoner);
        child.setData(CharmRegistry.CHARM_STATE.get(), proto);
        if (summonerState == null) {
            CharmHelper.celebrate(child);
        }
    }

    /**
     * 免疫入口：玩家与 BOSS 类生物不受任何来源的魅惑（瓶子/箭/喷溅/滞留/喝药）。
     * 用 Applicable（对应 canBeAffected）而非 Added——Added 不可取消，Applicable 可强制不施加。
     */
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        net.minecraft.world.effect.MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || !instance.is(CharmRegistry.CHARM_EFFECT)) {
            return;
        }
        if (CharmHelper.isCharmImmune(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /**
     * 统一契约落点：任何来源（瓶子、箭、喷溅、滞留云）的魅惑效果加入时，
     * 解析来源玩家并写实体 NBT 契约数据；永久标记随无限时长实例。
     */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEffectInstance().is(CharmRegistry.CHARM_EFFECT)) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        boolean permanent = event.getEffectInstance().isInfiniteDuration();
        CharmState old = CharmHelper.getState(mob);
        if (old != null && old.permanent() && !permanent) {
            // 已缔结永久契约的生物，时限版不降级、不抢主人，只刷新效果
            return;
        }
        Player source = CharmHelper.resolveOwnerFromEntitySource(event.getEffectSource());
        UUID owner = source != null ? source.getUUID() : (old != null ? old.owner() : null);
        // 统一移速：首次记录原值并压到 0.32，重复施加沿用原值
        double originalSpeed = CharmHelper.applyCharmSpeed(mob, old);
        mob.setData(CharmRegistry.CHARM_STATE.get(), new CharmState(owner, permanent, originalSpeed));
        if (old == null) {
            CharmHelper.celebrate(mob);
        }
    }

    /**
     * 魅惑期间禁止攻击玩家，也只允许攻击"契约合法目标"（攻敌/护主/自卫）。
     * 原版目标选择器照常运行，非法目标在这里被挡下，效果消失后自然恢复。
     * 挡非法替换时保留已锁定的合法目标，而不是清空——否则原版敌对 AI 每刻
     * 尝试锁定主人、被清空后导致远程/范围生物断断续续地停火。
     */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        LivingEntity target = event.getNewAboutToBeSetTarget();
        // 任何主动索敌的生物都不得主动攻击魅惑生物（魅惑生物全体是盟友，
        // 含铁傀儡/驯服宠物等"会主动攻击敌对生物"的生物）；
        // 仅当该魅惑生物正在攻击自己时才允许反击（自我保护）。
        if (target instanceof Mob targetMob && CharmHelper.isCharmActive(targetMob)) {
            if (targetMob.getTarget() != mob) {
                event.setNewAboutToBeSetTarget(null);
                return;
            }
        }
        if (target == null) {
            // 目标被清空：原版敌对索敌目标扫描半径很短(约8-16格)，战斗目标一旦漂出该半径，
            // goal 就会 stop() → setTarget(null)，弓拉满也被打断。若当前锁定仍是合法战斗目标
            // 且在粘性射程(32格)内，则恢复锁定，防止远程攻击突然中断。
            if (CharmHelper.isCharmActive(mob)) {
                LivingEntity current = mob.getTarget();
                if (current != null && current.isAlive() && CharmHelper.isAllowedCharmTarget(mob, current)) {
                    event.setNewAboutToBeSetTarget(current);
                }
            }
            return;
        }
        if (!CharmHelper.isCharmActive(mob)) {
            return;
        }
        if (!CharmHelper.isAllowedCharmTarget(mob, target)) {
            // 保留当前粘性目标；无合法目标时才清空
            LivingEntity current = mob.getTarget();
            if (current != null && current.isAlive() && CharmHelper.isAllowedCharmTarget(mob, current)) {
                event.setNewAboutToBeSetTarget(current);
            } else {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    /** 魅惑期间不回血（契约生物不享受任何回复）。 */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Mob mob && CharmHelper.isCharmActive(mob)) {
            event.setCanceled(true);
        }
    }

    /** 魅惑生物不自然消失；契约结束自动恢复原版消失规则。 */
    @SubscribeEvent
    public static void onDespawn(MobDespawnEvent event) {
        if (CharmHelper.isCharmActive(event.getEntity())) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }

    /**
     * 魅惑生物死亡时向所有玩家广播死亡消息。
     * 原版只有玩家与驯服动物有聊天死亡消息，普通怪物只记服务器日志；
     * 魅惑生物用原版 CombatTracker 生成消息（如"僵尸被玩家杀死了"），广播给全服。
     */
    @SubscribeEvent
    public static void onCharmedMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return; // 玩家与 Mob 是 LivingEntity 的兄弟类，此处不会接到玩家
        }
        if (mob instanceof TamableAnimal tameable && tameable.isTame()) {
            return; // 驯服动物已有原版死亡消息，避免重复
        }
        if (!CharmHelper.isCharmActive(mob)) {
            return;
        }
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            return;
        }
        Component message = mob.getCombatTracker().getDeathMessage();
        if (message != null) {
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    /** 主人死亡解除其全部契约。 */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer dead)) {
            return;
        }
        UUID id = dead.getUUID();
        List<Mob> mobs = dead.level().getEntitiesOfClass(Mob.class,
                new AABB(-30000000.0, -40000000.0, -30000000.0, 30000000.0, 40000000.0, 30000000.0));
        for (Mob mob : mobs) {
            CharmState state = CharmHelper.getState(mob);
            if (CharmHelper.ownedBy(state, id)) {
                CharmHelper.releaseCharm(mob);
            }
        }
    }

    /**
     * 双向友伤保护，都在伤害结算层统一取消：
     * 1) 魅惑生物发起的伤害（近战 / 远程箭矢 / 范围爆炸 / 范围 effect）跳过
     *    玩家本人、玩家的宠物、以及其他魅惑生物——目标拦截只挡 setTarget，
     *    而箭矢命中、爆炸半径内的 hurt() 不看目标，所以这里兜底取消。
     * 2) 玩家发起的远程 / 范围伤害（箭矢、投掷药水、烟花、TNT 爆炸等）不误伤
     *    魅惑生物——用 getDirectEntity() != getEntity() 区分：近战直接接触
     *    (direct==responsible) 不拦，刻意殴打仍有效；间接弹射物/范围波才取消。
     */
    @SubscribeEvent
    public static void onDamageIncoming(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        Entity direct = source.getDirectEntity();
        Entity responsible = source.getEntity();

        // 1) 魅惑生物 → 玩家阵营：一律取消
        if (responsible instanceof Mob attacker && CharmHelper.isCharmActive(attacker)) {
            if (CharmHelper.isPlayerSide(victim)) {
                event.setCanceled(true);
            }
            return;
        }
        // 2) 玩家 → 魅惑生物：仅远程/范围（间接伤害）取消，近战不拦
        if (responsible instanceof Player && direct != responsible) {
            if (victim instanceof Mob mob && CharmHelper.isCharmActive(mob)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 负面效果（中毒/缓慢/虚弱等，经喷溅药水、滞留云、范围效果施加）的阵营隔离：
     * 1) 玩家或玩家驯服宠物发起的负面效果不落在魅惑生物身上；
     * 2) 魅惑生物发起的负面效果不落在玩家、玩家驯服宠物、其他魅惑生物身上。
     * 只拦 {@link MobEffectCategory#HARMFUL}；增益效果（回血、力量等）正常施加。
     * 魅惑效果本身是 NEUTRAL 且由 {@link #onEffectApplicable} 单独处理，不会被误拦。
     * 伤害层已有 onDamageIncoming 兜底，这里补效果层（伤害与负面效果是两条独立管线）。
     */
    @SubscribeEvent
    public static void onNegativeEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        LivingEntity target = event.getEntity();
        LivingEntity actor = resolveEffectActor(event.getEffectSource());
        if (actor == null) {
            return; // 客户端包/无来源施加等一律放行，服务端才是裁决者
        }
        // 1) 玩家阵营 → 魅惑生物
        if (actor instanceof Player || CharmHelper.isPlayerTamed(actor)) {
            if (target instanceof Mob mob && CharmHelper.isCharmActive(mob)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
            return;
        }
        // 2) 魅惑生物 → 玩家阵营（含近战附带效果，比"仅范围"更彻底一致）
        if (actor instanceof Mob attacker && CharmHelper.isCharmActive(attacker)) {
            if (CharmHelper.isPlayerSide(target)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    /** 解析效果来源的真正归属者：玩家/生物直投，或喷溅弹射物/滞留云的投掷者/主人。 */
    @Nullable
    private static LivingEntity resolveEffectActor(@Nullable Entity source) {
        Entity actor = source;
        if (actor instanceof net.minecraft.world.entity.projectile.Projectile projectile && projectile.getOwner() != null) {
            actor = projectile.getOwner();
        } else if (actor instanceof AreaEffectCloud cloud && cloud.getOwner() != null) {
            actor = cloud.getOwner();
        }
        return actor instanceof LivingEntity living ? living : null;
    }

    /**
     * 睡觉免疫：魅惑怪物不阻止睡觉。
     * vanilla 只要床周围(床头为中心 ±8 水平、±5 垂直)存在任一 Monster 就判 NOT_SAFE 拦睡觉；
     * 魅惑只改攻击 AI 不改实体类，所以被魅惑的僵尸/骷髅仍会被当作敌对生物。这里在 NOT_SAFE
     * 时重扫同一检测区域，若区域内怪物全部是魅惑生物就放行；还有真实敌对怪则仍按原版阻止。
     */
    @SubscribeEvent
    public static void onCanPlayerSleep(CanPlayerSleepEvent event) {
        if (event.getProblem() != Player.BedSleepingProblem.NOT_SAFE) {
            return;
        }
        AABB box = new AABB(event.getPos()).inflate(8.0D, 5.0D, 8.0D);
        boolean onlyCharmed = event.getLevel()
                .getEntitiesOfClass(Monster.class, box, EntitySelector.NO_SPECTATORS)
                .stream()
                .allMatch(m -> m instanceof Mob mob && CharmHelper.isCharmActive(mob));
        if (onlyCharmed) {
            event.setProblem(null);
        }
    }

    /** 契约生物不可坐下（屏蔽空手右键驯服动物的交互）。 */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().isEmpty()
                && event.getTarget() instanceof TamableAnimal animal
                && CharmHelper.isCharmActive(animal)) {
            if (animal.isOrderedToSit()) {
                animal.setOrderedToSit(false);
            }
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
        }
    }

    /**
     * 往原版战利品表注入魅惑之瓶：每次抽取 1 次，25% 概率出 1-2 瓶。
     * 只追加一个命名池，不动原表内容；想调概率/数量改这里即可。
     */
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!CHARM_BOTTLE_LOOT_TABLES.contains(event.getName())) {
            return;
        }
        LootPool pool = new LootPool.Builder()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(0.25F))
                .add(LootItem.lootTableItem(CharmRegistry.CHARM_BOTTLE.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                .build();
        event.getTable().addPool(pool);
    }

    /** 酿造链：粗制药水 + 红/棕蘑菇 = 魅惑药水(45s)；+ 红石 = 魅惑药水(1m30s)。喷溅/滞留由原版通用规则自动衔接。 */
    @SubscribeEvent
    public static void onRegisterBrewing(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.RED_MUSHROOM, CharmRegistry.CHARM_POTION);
        builder.addMix(Potions.AWKWARD, Items.BROWN_MUSHROOM, CharmRegistry.CHARM_POTION);
        builder.addMix(CharmRegistry.CHARM_POTION, Items.REDSTONE, CharmRegistry.LONG_CHARM_POTION);
    }
}
