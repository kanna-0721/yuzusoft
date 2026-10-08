package cn.autoforged.yuzusoft.entity.custom;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.LimelightHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import cn.autoforged.yuzusoft.sound.ModSounds;

public class FlashbangMonster extends Monster implements RangedAttackMob {
    public FlashbangMonster(EntityType<? extends FlashbangMonster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 60, 60, 15.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this, LimelightHelper::isLimelight));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new GroupSupportTargetGoal(this, LimelightHelper::isLimelight));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        FlashbangProjectile projectile = new FlashbangProjectile(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - projectile.getY();
        double dz = target.getZ() - this.getZ();
        double d3 = Math.sqrt(dx * dx + dz * dz);
        double speed = 1.6D;
        // shoot() 会把 (dx, vy, dz) 归一化后再乘 speed，因此竖向分量取
        // vy = dy + 0.5*g*d3²/speed²（g=0.03 为 ThrowableItemProjectile 重力）即可精确命中
        double vy = dy + 0.5D * 0.03D * d3 * d3 / (speed * speed);
        projectile.shoot(dx, vy, dz, (float) speed, 0.0F);
        this.level().addFreshEntity(projectile);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FLASHBANG_MONSTER_AMBIENT.get();
    }
    @Override
    public int getAmbientSoundInterval() {
        int baseTick = 80;
        return baseTick + this.random.nextInt(baseTick);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.FLASHBANG_MONSTER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FLASHBANG_MONSTER_DEATH.get();
    }
}

