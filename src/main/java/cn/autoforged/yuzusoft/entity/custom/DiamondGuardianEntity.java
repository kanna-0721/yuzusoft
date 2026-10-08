package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 钻石感应守护者（小云雀来海）：曲奇驯服、安静跟随、不攻击、不能坐下/繁殖；
 * 感应主人周围 6 格内的钻石矿石与深层钻石矿石（60 秒冷却），下界则探测远古残骸；
 * 死亡时以主人为中心让 48 格内所有生物发光 30 秒。
 */
public class DiamondGuardianEntity extends TamableAnimal {

    /** 感应半径（格，按坐标轴取切比雪夫距离） */
    public static final int SCAN_RADIUS = 6;
    /** 感应冷却：60 秒 */
    public static final int ALERT_COOLDOWN_TICKS = 1200;
    /** 扫描间隔：每 10 tick 查一次环境 */
    private static final int SCAN_INTERVAL_TICKS = 10;
    /** 死亡保护半径：48 格 */
    public static final double DEATH_GLOW_RADIUS = 48.0;
    /** 发光时长：30 秒 */
    public static final int DEATH_GLOW_DURATION_TICKS = 600;
    /** 怪物探测冷却：30 秒（苦力怕与绫地宁宁各自独立计时） */
    public static final int MOB_ALERT_COOLDOWN_TICKS = 600;
    /** 驯服后感应冷却剩余 tick（运行时状态，不存盘） */
    private int alertCooldown = 0;
    /** 苦力怕探测冷却剩余 tick（独立计时） */
    private int creeperAlertCooldown = 0;
    /** 绫地宁宁（DetonatorThrowingMonsterEntity）探测冷却剩余 tick（独立计时） */
    private int detonatorAlertCooldown = 0;
    /** 死亡保护是否已触发，防止 die() 重入 */
    private boolean deathGlowApplied = false;

    public DiamondGuardianEntity(EntityType<? extends DiamondGuardianEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    /**
     * 只有 Float/跟随/漫步/张望，没有攻击目标选择器，也没有坐下与繁殖相关 goal，
     * 保证“安静陪同、不主动攻击、不能坐下、不能繁殖”。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.0, 4.0F, 2.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !this.isTame()) {
            return;
        }
        if (this.alertCooldown > 0) {
            this.alertCooldown--;
        } else if (this.tickCount % SCAN_INTERVAL_TICKS == 0 && this.getOwner() instanceof ServerPlayer owner) {
            if (owner.level().dimension().equals(Level.NETHER)) {
                this.scanForAncientDebris(owner);
            } else {
                this.scanForDiamonds(owner);
            }
        }
        // 怪物探测：苦力怕 / 绫地宁宁，冷却各自独立计时（均 30 秒）
        if (this.creeperAlertCooldown > 0) {
            this.creeperAlertCooldown--;
        } else if (this.tickCount % SCAN_INTERVAL_TICKS == 0 && this.getOwner() instanceof ServerPlayer owner) {
            this.scanForCreeper(owner);
        }
        if (this.detonatorAlertCooldown > 0) {
            this.detonatorAlertCooldown--;
        } else if (this.tickCount % SCAN_INTERVAL_TICKS == 0 && this.getOwner() instanceof ServerPlayer owner) {
            this.scanForDetonator(owner);
        }
    }

    /** 主人右键：未驯服时用曲奇尝试驯服；不追加坐下/回血/繁殖等交互分支。 */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame() && stack.is(Items.COOKIE)) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    this.playSound(ModSounds.DIAMOND_GUARDIAN_TAME.get(), 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level().isClientSide || this.deathGlowApplied || !this.isTame()) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        this.deathGlowApplied = true;
        AABB area = owner.getBoundingBox().inflate(DEATH_GLOW_RADIUS, DEATH_GLOW_RADIUS, DEATH_GLOW_RADIUS);
        for (Entity entity : this.level().getEntities(owner, area,
                e -> e instanceof LivingEntity && e.isAlive())) {
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, DEATH_GLOW_DURATION_TICKS, 0, false, true));
            }
        }
    }

    /** 唯一认可的"食物"是曲奇，仅用于驯服；本实体没有繁殖 goal，不会因此繁殖。 */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKIE);
    }

    /** 本生物不能繁殖：没有 Breeding goal，此方法不会被调用，返回 null 与骡子一致。 */
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    /** 驯服后不会因为远离玩家被清理。 */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame();
    }

    /** 主世界扫描：钻石/深层钻石矿石。 */
    private void scanForDiamonds(ServerPlayer owner) {
        scanForOre(owner, "message.yuzusoft.kohibari_kurumi.diamond_nearby", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    /** 下界扫描：远古残骸。 */
    private void scanForAncientDebris(ServerPlayer owner) {
        scanForOre(owner, "message.yuzusoft.kohibari_kurumi.ancient_debris_nearby", Blocks.ANCIENT_DEBRIS);
    }

    /** 扫描主人周围 6 格内的指定矿石，命中则播报方向并给出粒子提示（钻石/远古残骸共用逻辑）。 */
    private void scanForOre(ServerPlayer owner, String messageKey, Block... targets) {
        BlockPos center = owner.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    cursor.setWithOffset(center, dx, dy, dz);
                    if (!this.level().isLoaded(cursor)) {
                        continue;
                    }
                    net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(cursor);
                    for (Block target : targets) {
                        if (state.is(target)) {
                            double distSq = Vec3.atCenterOf(cursor).distanceToSqr(owner.position());
                            if (distSq < bestDistSq) {
                                bestDistSq = distSq;
                                best = cursor.immutable();
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (best == null) {
            return;
        }
        this.alertCooldown = ALERT_COOLDOWN_TICKS;

        // 矿石相对玩家的位置向量，取绝对值最大的坐标轴作为方向
        String directionKey = directionKeyOf(Vec3.atCenterOf(best).subtract(owner.position()));

        Component message = Component.translatable(messageKey, Component.translatable(directionKey));
        owner.displayClientMessage(message, false);
        this.playSound(ModSounds.DIAMOND_GUARDIAN_ALERT.get(), 1.0F, 1.0F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    best.getX() + 0.5, best.getY() + 0.5, best.getZ() + 0.5,
                    16, 0.2, 0.2, 0.2, 0.0);
        }
    }

    /** 苦力怕探测：玩家周围 16x3x16 格（x/z 各 ±8，y ±1），冷却 30 秒独立计时。 */
    private void scanForCreeper(ServerPlayer owner) {
        Creeper target = findNearestMob(owner, Creeper.class, 8.0, 1.0, 8.0);
        if (target != null) {
            this.creeperAlertCooldown = MOB_ALERT_COOLDOWN_TICKS;
            reportMob(owner, target, "message.yuzusoft.kohibari_kurumi.creeper_nearby");
        }
    }

    /** 绫地宁宁探测：玩家周围 24x11x24 格（x/z 各 ±12，y ±5），冷却 30 秒独立计时。 */
    private void scanForDetonator(ServerPlayer owner) {
        DetonatorThrowingMonsterEntity target = findNearestMob(owner, DetonatorThrowingMonsterEntity.class, 12.0, 5.0, 12.0);
        if (target != null) {
            this.detonatorAlertCooldown = MOB_ALERT_COOLDOWN_TICKS;
            reportMob(owner, target, "message.yuzusoft.kohibari_kurumi.detonator_nearby");
        }
    }

    /** 在指定 AABB（以玩家为中心）内找最近的存活目标。 */
    private <T extends Mob> T findNearestMob(ServerPlayer owner, Class<T> clazz, double hx, double hy, double hz) {
        AABB area = owner.getBoundingBox().inflate(hx, hy, hz);
        T best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (T e : this.level().getEntitiesOfClass(clazz, area, Mob::isAlive)) {
            double distSq = e.distanceToSqr(owner);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = e;
            }
        }
        return best;
    }

    /** 向主人聊天框播报最近目标的方位。 */
    private void reportMob(ServerPlayer owner, Mob target, String messageKey) {
        String directionKey = directionKeyOf(target.position().subtract(owner.position()));
        Component message = Component.translatable(messageKey, Component.translatable(directionKey));
        owner.displayClientMessage(message, false);
        this.playSound(ModSounds.DIAMOND_GUARDIAN_ALERT.get(), 1.0F, 1.0F);
    }

    /** 把相对位置向量换算成方位翻译 key（取绝对值最大的坐标轴）。 */
    private static String directionKeyOf(Vec3 diff) {
        double ax = Math.abs(diff.x);
        double ay = Math.abs(diff.y);
        double az = Math.abs(diff.z);
        if (ax >= ay && ax >= az) {
            return diff.x >= 0 ? "direction.yuzusoft.east" : "direction.yuzusoft.west";
        } else if (az >= ax && az >= ay) {
            return diff.z >= 0 ? "direction.yuzusoft.south" : "direction.yuzusoft.north";
        } else {
            return diff.y >= 0 ? "direction.yuzusoft.up" : "direction.yuzusoft.down";
        }
    }
    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DIAMOND_GUARDIAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.DIAMOND_GUARDIAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DIAMOND_GUARDIAN_DEATH.get();
    }
}
