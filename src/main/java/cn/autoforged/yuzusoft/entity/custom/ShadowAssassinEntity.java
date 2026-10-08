package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import cn.autoforged.yuzusoft.worldgen.ModSpawnPlacements;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.server.level.ServerLevel;

public class ShadowAssassinEntity extends Raider implements RangedAttackMob {

    /** 0721 前哨站 / 自然巡逻队队长专用旗帜图案。 */
    private static final ResourceKey<BannerPattern> BANNER_0721 =
            ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath("yuzusoft", "0721"));

    /** 是否为袭击波次生成（MobSpawnType.EVENT）。用于死亡掉落时区分"袭击队长"与"自然巡逻队长"。 */
    private boolean raidSpawned;

    public ShadowAssassinEntity(EntityType<? extends ShadowAssassinEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SHADOW_DART.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals(); // Raider：袭击旗帜/寻路/穿村/庆祝
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 50, 15.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class,true));
        // 村民目标：优先级在玩家之后
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        // 族群支援：优先级在玩家/铁傀儡/村民之后，未锁定更高优先级目标时才前往支援
        this.targetSelector.addGoal(5, new GroupSupportTargetGoal(this));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ShadowDartEntity dart = new ShadowDartEntity(this.level(), this);
        double aimX = target.getX();
        double aimY = target.getY(0.5D);
        double aimZ = target.getZ();
        double dx = aimX - this.getX();
        double dy = aimY - this.getEyeY();
        double dz = aimZ - this.getZ();
        dart.shoot(dx, dy, dz, 2.0F, distanceFactor * 0.6F);
        this.playSound(ModSounds.SHADOW_DART_THROW.get(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(dart);
    }

    /** 构造头戴的 0721 队长旗帜（与前哨站 / 自然巡逻队队长共用）。 */
    private ItemStack make0721LeaderBanner() {
        ItemStack banner = new ItemStack(Items.WHITE_BANNER);
        Holder<BannerPattern> holder = this.registryAccess().holderOrThrow(BANNER_0721);
        banner.set(DataComponents.BANNER_PATTERNS,
                new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(holder, DyeColor.BLACK))));
        return banner;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) return false;
        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity && this.random.nextFloat() < 0.6F) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D;
            double y = this.getY() + (double) (this.random.nextInt(8) - 4);
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D;
            if (this.randomTeleport(x, y, z, true)) {
                this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = switch (this.level().getDifficulty()) {
            case EASY -> 3.0F;
            case HARD -> 6.0F;
            default -> 4.0F;
        };
        boolean flag = target.hurt(this.damageSources().mobAttack(this), damage);
        if (flag && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        return flag;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Group0721Helper.areAllied(this, living)
                    || entity instanceof Raider // 女巫/劫掠兽等袭击者：视为盟友，不互攻、不反击
                    || super.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        // 记录是否由袭击波次生成（Raid.joinRaid 以 MobSpawnType.EVENT 生成）：
        // Raider.die() 会在掉落前先 removeFromRaid 清空 currentRaid，故不能用 getCurrentRaid() 区分袭击成员。
        this.raidSpawned = spawnType == MobSpawnType.EVENT;
        // 哨站 spawn_overrides(STRUCTURE) 刷出的刺客有 6% 概率升级为巡逻队长（与原版掠夺者一致）。
        // 原版 PatrollingMonster 明确排除 STRUCTURE，这里单独放行以实现"前哨站也偶发队长"。
        if (spawnType == MobSpawnType.STRUCTURE && !this.isPatrolLeader() && this.random.nextFloat() < 0.06F) {
            this.setPatrolLeader(true);
        }
        // 继承 Raider(PatrollingMonster) 后，自主自然生成（含哨站 spawn_overrides 以 NATURAL 触发的持续刷新）
        // 有概率被自动设为巡逻队长；
        // 这里只保留巡逻队(PATROL)、袭击(EVENT)、哨站(STRUCTURE 或位于 outpost_0721 内) 生成的队长，
        // 其余自主自然生成取消，保持"队长必掉 0721 不祥之瓶"的设计：队长只由受控的生成方式产生。
        if (this.isPatrolLeader()
                && spawnType != MobSpawnType.PATROL
                && spawnType != MobSpawnType.EVENT
                && spawnType != MobSpawnType.STRUCTURE
                && !ModSpawnPlacements.isInOutpost0721(level, this.blockPosition())) {
            this.setPatrolLeader(false);
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
        // 刚升级为队长但还没戴旗（例如上方 STRUCTURE 分支）→ 补戴 0721 队长旗
        if (this.isPatrolLeader() && this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.setItemSlot(EquipmentSlot.HEAD, make0721LeaderBanner());
            this.setDropChance(EquipmentSlot.HEAD, 2.0F);
        }
        // 野生/结构等自主生成的 0721 生物不允许被任何袭击征召入波（避免普通袭击波次总数被地下自然刷新的生物拉高）。
        // 袭击自身 EntityType.create(Level) 生成不走 finalizeSpawn，且 spawnGroup 会显式 setCanJoinRaid(true)，不受影响。
        if (spawnType != MobSpawnType.EVENT) {
            this.setCanJoinRaid(false);
        }
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // 仅"非袭击"队长掉 0721 不祥之瓶（自然巡逻队 / 未来的 0721 前哨站队长）：
        // 袭击波次队长也会被 Raid.spawnGroup 置为 patrol leader，若不加判断，
        // 会造成"杀队长 → 拿瓶 → 再触发袭击"的无限循环。
        // 注意：不能用 getCurrentRaid()==null 判断——Raider.die() 会先 removeFromRaid
        // 把 currentRaid 置空再触发本方法，须用 finalizeSpawn 记录的生成类型区分。
        if (this.isPatrolLeader() && !this.raidSpawned) {
            ItemStack bottle = new ItemStack(ModItems.OMENS_BOTTLE_0721.get());
            // 随机掉 1-5 级：等级存在数据组件，等级越高触发的 0721 袭击越强
            bottle.set(ModDataComponents.BOTTLE_0721_LEVEL, this.random.nextInt(1, 6));
            this.spawnAtLocation(bottle);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("RaidSpawned", this.raidSpawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("RaidSpawned")) {
            this.raidSpawned = compound.getBoolean("RaidSpawned");
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    /** 袭击增益：与唤魔者一致，空实现（不附魔武器）。 */
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean p_37844_) {
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHADOW_ASSASSIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SHADOW_ASSASSIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHADOW_ASSASSIN_DEATH.get();
    }

}

