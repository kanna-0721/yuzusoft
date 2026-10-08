package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ai.GuitarRangedAttackGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupHurtByTargetGoal;
import cn.autoforged.yuzusoft.entity.ai.GroupSupportTargetGoal;
import cn.autoforged.yuzusoft.entity.community.LimelightHelper;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GuitarMonsterEntity extends Monster implements RangedAttackMob {
    public GuitarMonsterEntity(EntityType<? extends GuitarMonsterEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 24;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GUITAR_WEAPON.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new GuitarRangedAttackGoal(this, 1.0, 80, 15.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new GroupHurtByTargetGoal(this, LimelightHelper::isLimelight));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new GroupSupportTargetGoal(this, LimelightHelper::isLimelight));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!this.level().isClientSide) {
            float damage = switch (this.level().getDifficulty()) {
                case EASY -> 3.0F;
                case NORMAL -> 4.0F;
                case HARD -> 6.0F;
                default -> 3.0F;
            };
            ServerLevel serverLevel = (ServerLevel) this.level();
            Vec3 origin = this.position().add(0.0, this.getEyeHeight() * 0.6, 0.0);
            Vec3 targetVec = target.getEyePosition().subtract(origin);
            Vec3 dir = targetVec.normalize();
            int steps = Mth.floor(targetVec.length()) + 7;
            for (int j = 1; j < steps; j++) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                        origin.x + dir.x * j, origin.y + dir.y * j, origin.z + dir.z * j,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            if (target.hurt(this.damageSources().sonicBoom(this), damage)) {
                double knockbackResist = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                double d1 = 0.5 * (1.0 - knockbackResist);
                double d0 = 2.5 * (1.0 - knockbackResist);
                target.push(dir.x * d0, dir.y * d1, dir.z * d0);
            }
            this.playSound(ModSounds.GUITAR_WEAPON_RANGED.get(), 3.0F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float scaled = switch (this.level().getDifficulty()) {
            case EASY -> damage * 0.75F;
            case HARD -> damage * 1.0F;
            default -> damage;
        };
        boolean hurt = target.hurt(this.damageSources().mobAttack(this), scaled);
        if (hurt) {
            this.swing(InteractionHand.MAIN_HAND);
            this.playSound(ModSounds.GUITAR_MONSTER_ATTACK.get(), 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (recentlyHit && this.random.nextFloat() < 0.025F) {
            this.spawnAtLocation(new ItemStack(ModItems.GUITAR_WEAPON.get()));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GUITAR_MONSTER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GUITAR_MONSTER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GUITAR_MONSTER_DEATH.get();
    }
}