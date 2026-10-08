package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class DetonatorThrowingMonsterEntity extends Monster implements RangedAttackMob {

    public DetonatorThrowingMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.DETONATOR.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, 30, 60, 15.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new IgnoreAllyHurtGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !Group0721Helper.isIgnoredByGroup(target)));
        // 村民目标：优先级在玩家之后
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        // 族群支援：优先级在玩家/村民之后（爆破怪不额外索敌铁傀儡），未锁定更高优先级目标时才前往支援
        this.targetSelector.addGoal(4, new GroupSupportTargetGoal(this));
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Group0721Helper.areAllied(this, living) || super.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    static class IgnoreAllyHurtGoal extends GroupHurtByTargetGoal {
        public IgnoreAllyHurtGoal(Mob mob) {
            super(mob);
        }

        @Override
        public boolean canUse() {
            // 执行父类原有判断逻辑
            if (!super.canUse()) {
                return false;
            }
            // 获取伤害来源
            LivingEntity attacker = mob.getLastHurtByMob();
            // 如果攻击者是自身同类 → 不启动仇恨AI（不会反击）
            return !(attacker instanceof DetonatorThrowingMonsterEntity);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        DetonatorProjectileEntity projectile = new DetonatorProjectileEntity(level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - projectile.getY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 1.2f, 2.0f);
        level().addFreshEntity(projectile);
    }

    public static boolean checkSpawnRules(EntityType<DetonatorThrowingMonsterEntity> entityType,
                                           ServerLevelAccessor level,
                                           MobSpawnType spawnType,
                                           BlockPos pos,
                                           RandomSource random) {
        return pos.getY() < level.getSeaLevel() && level.getRawBrightness(pos, 0) == 0
                && Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }
    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DETONATOR_MONSTER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.DETONATOR_MONSTER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DETONATOR_MONSTER_DEATH.get();
    }

    @Override
    protected int getBaseExperienceReward() {
        return 20;
    }
}
