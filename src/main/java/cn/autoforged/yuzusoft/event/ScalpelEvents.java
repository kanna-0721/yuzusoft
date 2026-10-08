package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.entity.ai.FusionTameHelper;
import cn.autoforged.yuzusoft.head.FusionAssembler;
import cn.autoforged.yuzusoft.head.FusionMerchant;
import cn.autoforged.yuzusoft.head.HeadAbilityTable;
import cn.autoforged.yuzusoft.head.MobHeadRegistry;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.item.custom.ScalpelItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collection;

/**
 * 手术刀玩法的服务端逻辑：
 * <ul>
 *   <li><b>击杀</b>：用手术刀近战击杀「有头生物」→ 掉落一份「无头躯体」物品
 *       （携带死亡时的完整实体 NBT）。头颅由 {@link HeadDropHandler} 统一掉落：
 *       普通生物掉自身头；融合生物只掉「接上去的那个头」，身体原旧头不掉落；</li>
 *   <li><b>放置</b>：见 {@link cn.autoforged.yuzusoft.item.custom.DecapitatedBodyItem}——
 *       在 2 格高空间生成一只冻结的真实生物实体（客户端隐藏头部）；</li>
 *   <li><b>交互</b>：空手右键身体 → 拾回躯体物品；手持头颅右键 → 头颅与身体同种则满血复活
 *       并保留死亡时状态（驯服/魅惑等），不同种则由 {@link FusionAssembler} 组装融合生物。</li>
 * </ul>
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public final class ScalpelEvents {
    private ScalpelEvents() {}

    /** 手术刀近战击杀有头生物 → 必掉头 + 掉落无头躯体物品。 */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide) {
            return;
        }
        // 只认近战击杀（弹射物击杀不算「用手术刀击杀」）
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ScalpelItem)) {
            return;
        }
        if (!MobHeadRegistry.hasHead(victim.getType())) {
            return;
        }

        // 头颅统一由 HeadDropHandler 掉落：普通生物掉自身头；融合生物只掉「接上去的那个头」，
        // 身体自身的原旧头不掉落。这里只负责留下「无头躯体」物品（携带死亡时的完整 NBT）。
        CompoundTag tag = new CompoundTag();
        if (!victim.save(tag)) {
            return; // 极少数实体（如乘客）无法序列化，只掉头不留身
        }
        ItemStack bodyStack = new ItemStack(ModItems.DECAPITATED_BODY.get());
        bodyStack.set(ModDataComponents.STORED_MOB.get(), tag);
        addDrop(level, victim, bodyStack, event.getDrops());
    }

    private static void addDrop(Level level, LivingEntity victim, ItemStack stack, Collection<ItemEntity> drops) {
        ItemEntity drop = new ItemEntity(level,
                victim.getX() + (level.random.nextFloat() - 0.5F) * 0.4D,
                victim.getY() + 0.4D,
                victim.getZ() + (level.random.nextFloat() - 0.5F) * 0.4D,
                stack);
        drop.setPickUpDelay(10);
        drops.add(drop);
    }

    /**
     * 冻结无头躯体：取消其每 tick 逻辑。
     * <p>
     * 比「关 AI」更彻底——不移动、不下坠、被推也不漂、白天不燃烧、不被其他生物索敌，
     * 且不会随时间改变外观。客户端照常 tick（渲染动画正常），故只在服务端取消。
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (entity.hasData(ModAttachments.DECAPITATED_BODY)) {
            event.setCanceled(true);
            return;
        }
        // 头部为乃爱的融合体 = 坐骑：由服务端按骑手输入驱动（见 driveRideableFusion）
        if (entity instanceof Mob mob
                && Boolean.TRUE.equals(mob.getExistingDataOrNull(ModAttachments.FUSION_RIDEABLE))) {
            driveRideableFusion(mob);
        }
    }

    /**
     * 融合坐骑的服务端驱动。
     * <p>
     * 融合体的实体类型是「身体」的，无法像
     * {@link cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity} 那样覆写 {@code travel}
     * 直接读取骑手输入；而玩家乘坐时原版每 tick 都会把 WASD
     * （{@code ServerboundPlayerInputPacket} → {@code ServerPlayer#setPlayerInput}）和朝向
     * （{@code ServerboundMovePlayerPacket.Rot}）同步到服务端，所以这里在服务端外部驱动即可：
     * 暂停身体 AI → 用骑手输入算出位移 → 直接 {@code move}。
     * <p>
     * 特性全部来自头部（乃爱 = 飞行坐骑）：抬头上升、低头下降、WASD 平移；移速取身体。
     */
    private static void driveRideableFusion(Mob mob) {
        Player rider = mob.getFirstPassenger() instanceof Player player ? player : null;
        if (rider == null) {
            // 无人乘坐：恢复身体自身的 AI 与重力
            if (mob.isNoAi()) {
                mob.setNoAi(false);
            }
            mob.setNoGravity(false);
            return;
        }

        // 乘坐中：暂停身体 AI，改为纯手动驱动
        if (!mob.isNoAi()) {
            mob.setNoAi(true);
        }
        mob.setNoGravity(true);
        mob.setTarget(null);
        mob.getNavigation().stop();

        // 朝向跟随骑手（俯仰只取一半，与乃爱一致）
        mob.setYRot(rider.getYRot());
        mob.yRotO = mob.getYRot();
        mob.setXRot(rider.getXRot() * 0.5F);
        mob.xRotO = mob.getXRot();
        mob.setYHeadRot(mob.getYRot());

        // 移速取身体（所有 Mob 都有 MOVEMENT_SPEED），骑乘时放大 2 倍便于赶路
        AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        double speed = (moveSpeed == null ? 0.23D : moveSpeed.getValue()) * 2.0D;

        // 水平：骑手的 WASD；竖直：抬头上升 / 低头下降
        Vec3 horizontal = new Vec3(rider.xxa * speed, 0.0D, rider.zza * speed)
                .yRot((float) Math.toRadians(-mob.getYRot()));
        double vertical = -Math.sin(Math.toRadians(rider.getXRot())) * speed;
        Vec3 movement = new Vec3(horizontal.x, vertical, horizontal.z);

        mob.setDeltaMovement(movement);
        mob.move(MoverType.SELF, movement);
        mob.fallDistance = 0.0F;
        rider.fallDistance = 0.0F;
    }

    // ------------------------------------------------------------------
    //  右键身体：空手拾回 / 手持头颅复活或融合
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Entity target = event.getTarget();
        // 可骑乘的融合体（乃爱头）：潜行 + 空手右键 = 骑上去（与乃爱本体一致）
        if (Boolean.TRUE.equals(target.getExistingDataOrNull(ModAttachments.FUSION_RIDEABLE))
                && event.getItemStack().isEmpty()
                && event.getEntity().isShiftKeyDown()
                && !event.getEntity().isPassenger()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if (!event.getLevel().isClientSide) {
                event.getEntity().startRiding(target);
            }
            return;
        }
        CompoundTag bodyTag = target.getExistingDataOrNull(ModAttachments.DECAPITATED_BODY);
        if (bodyTag == null) {
            return;
        }
        ItemStack held = event.getItemStack();
        EntityType<?> headType = MobHeadRegistry.entityFor(held.getItem());
        if (headType == null && !held.isEmpty()) {
            return; // 既不是空手也不是头颅：交给原版
        }

        // 拦下原版交互（避免挤奶/喂食之类的原版行为）
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        Player player = event.getEntity();
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 spawnPos = target.position();

        if (headType == null) {
            // 空手拾回躯体
            ItemStack drop = new ItemStack(ModItems.DECAPITATED_BODY.get());
            drop.set(ModDataComponents.STORED_MOB.get(), bodyTag.copy());
            target.discard();
            if (!player.addItem(drop)) {
                player.drop(drop, false);
            }
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 8, 0.3D, 0.5D, 0.3D, 0.05D);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.ARMOR_STAND_BREAK,
                    SoundSource.PLAYERS, 0.7F, 1.0F);
            return;
        }

        // 手持头颅：同种复活，异种融合
        EntityType<?> bodyType = EntityType.by(bodyTag).orElse(null);
        ItemStack headStack = held.copyWithCount(1);
        boolean matched = bodyType != null && bodyType == headType;
        target.discard();
        Entity created = matched
                ? revive(serverLevel, bodyTag, spawnPos)
                : FusionAssembler.assemble(serverLevel, bodyType, bodyTag, headType, headStack, spawnPos);
        if (created == null) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (matched) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 10, 0.3D, 0.5D, 0.3D, 0.0D);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE,
                    SoundSource.PLAYERS, 0.7F, 1.0F);
        } else {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 14, 0.3D, 0.5D, 0.3D, 0.02D);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                    SoundSource.PLAYERS, 0.8F, 0.8F);
        }
    }

    /**
     * 融合生物的「芳乃头」：右键打开交易界面（与芳乃原生物同一套报价）。
     * <p>
     * 身体实体类型（僵尸/骷髅……）本身不是 {@code Merchant}，故只在这里把交互转交给
     * {@link FusionMerchant}，由它借用 {@code Merchant#openTradingScreen} 打开原版交易菜单。
     */
    @SubscribeEvent
    public static void onTraderInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        if (!Boolean.TRUE.equals(target.getExistingDataOrNull(ModAttachments.FUSION_BODY))) {
            return;
        }
        EntityType<?> headType = MobHeadRegistry.entityFor(living.getItemBySlot(EquipmentSlot.HEAD).getItem());
        if (headType == null || !HeadAbilityTable.hasTrait(headType, HeadAbilityTable.Trait.TRADER)) {
            return;
        }
        // 拦下身体自身的右键行为（喂食/挤奶/转化之类的原版交互）
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) {
            return;
        }
        Player player = event.getEntity();
        FusionMerchant merchant = target.getExistingDataOrNull(ModAttachments.FUSION_MERCHANT);
        if (merchant == null) {
            merchant = new FusionMerchant(target);
            target.setData(ModAttachments.FUSION_MERCHANT, merchant);
        }
        merchant.setTradingPlayer(player);
        merchant.openTradingScreen(player, target.getDisplayName(), 1);
    }

    /**
     * 融合生物的驯服交互：照抄头部原生物——
     * 惠（面包）/ 天音（面包）/ 来海（曲奇）用手持对应食物右键即 1/3 概率驯服；
     * 已驯服后用食物回血，空手右键切换坐下 / 起身。
     * <p>
     * 融合体是既有实体类型，实现不了 {@code TamableAnimal}，归属只记在
     * {@link ModAttachments#FUSION_OWNER} 上（会话内）。
     */
    @SubscribeEvent
    public static void onFusionTameInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof PathfinderMob mob)) {
            return;
        }
        if (!Boolean.TRUE.equals(mob.getExistingDataOrNull(ModAttachments.FUSION_BODY))) {
            return;
        }
        EntityType<?> headType = MobHeadRegistry.entityFor(mob.getItemBySlot(EquipmentSlot.HEAD).getItem());
        if (headType == null) {
            return;
        }
        Item food = HeadAbilityTable.profileFor(headType).tameFood();
        if (food == null) {
            return;
        }

        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        Level level = event.getLevel();

        if (FusionTameHelper.isTamed(mob)) {
            LivingEntity owner = FusionTameHelper.ownerOf(mob);
            if (owner != null && owner != player) {
                return; // 已有别的主人，不抢
            }
            // 空手切坐下 / 起身（关 AI 即原地不动）
            if (held.isEmpty()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                if (!level.isClientSide) {
                    mob.setNoAi(!mob.isNoAi());
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }
                return;
            }
            // 主人用食物回血
            if (held.is(food) && mob.getHealth() < mob.getMaxHealth()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                if (!level.isClientSide) {
                    mob.heal(4.0F);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    ((ServerLevel) level).sendParticles(ParticleTypes.HEART,
                            mob.getX(), mob.getY() + 1.0D, mob.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.0D);
                }
            }
            return;
        }

        // 未驯服：手持对应食物 → 1/3 概率驯服
        if (!held.is(food)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (player.getRandom().nextInt(3) == 0) {
            FusionTameHelper.setOwner(mob, player);
            mob.setTarget(null);
            mob.getNavigation().stop();
            serverLevel.sendParticles(ParticleTypes.HEART,
                    mob.getX(), mob.getY() + 1.0D, mob.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.0D);
        } else {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    mob.getX(), mob.getY() + 1.0D, mob.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    /** 满血复活：按 NBT 还原实体（保留驯服/魅惑等死亡时状态），血量补满。 */
    private static Entity revive(ServerLevel level, CompoundTag tag, Vec3 pos) {
        final boolean[] added = {true};
        Entity restored = EntityType.loadEntityRecursive(tag, level, entity -> {
            entity.moveTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());
            entity.setDeltaMovement(Vec3.ZERO);
            if (!level.addFreshEntity(entity)) {
                added[0] = false;
            }
            return entity;
        });
        if (restored == null || !added[0]) {
            return null;
        }
        if (restored instanceof LivingEntity living) {
            living.deathTime = 0;
            living.hurtTime = 0;
            living.setHealth(living.getMaxHealth());
        }
        return restored;
    }
}