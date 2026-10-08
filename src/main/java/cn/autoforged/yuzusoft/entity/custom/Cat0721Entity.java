package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * "0721猫"：由 SprinklerCreepEntity 召唤的战斗猫（外观固定为英国短毛猫）。
 * 继承 Monster：铁傀儡（Enemy 判定）与各模组敌对生物（Monster 判定）都会主动攻击它。
 * 血量 10、攻击力 4，属于 0721 族群：与其他 0721 生物互不攻击，并参与族群支援。
 * 渲染使用自定义 Cat0721Renderer（复用原版猫模型几何 + 英短贴图）。
 */
public class Cat0721Entity extends Monster {

    public Cat0721Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    /** 召唤者（SprinklerCreepEntity）的 UUID；按"每只召唤者最多同时存在 10 只猫"计数。 */
    @Nullable
    private UUID summonerId;

    public void setSummoner(@Nullable UUID summonerId) {
        this.summonerId = summonerId;
    }

    @Nullable
    public UUID getSummonerId() {
        return this.summonerId;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.summonerId != null) {
            compound.putUUID("Summoner", this.summonerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Summoner")) {
            this.summonerId = compound.getUUID("Summoner");
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        // 完全自定义战斗目标，不继承原版猫的宠物行为（如逃避玩家 / 觅食 / 睡觉）
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this));
        // 玩家 > 铁傀儡 > 村民；创造模式玩家被完全无视
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        // 族群支援：优先级在玩家/铁傀儡/村民之后，未锁定更高优先级目标时才前往支援
        this.targetSelector.addGoal(5, new GroupSupportTargetGoal(this));
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Group0721Helper.areAllied(this, living) || super.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    // ---- 以下覆盖把 Monster 基类默认行为恢复为"猫" ----

    /** Monster 默认禁止玩家睡觉；召唤的战斗猫不应挡床 */
    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return false;
    }

    /** Monster 默认会掉经验；召唤物不掉 */
    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAT_STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }
}
