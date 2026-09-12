package cn.autoforged.yuzusoft.entity.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.ai.AllyHurtTargetGoal;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HumanoidCreatureEntity extends TamableAnimal {

    public HumanoidCreatureEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1, Ingredient.of(Items.BREAD), false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new AllyHurtTargetGoal(this));
    }

    private boolean isSittingState = false;



    @Override
    public void tick() {
        super.tick();
        boolean sitOrBoat = this.isOrderedToSit() || (this.getVehicle() instanceof Boat);
        // 标记当前尺寸状态，避免重复刷新
        if(sitOrBoat != this.isSittingState){
            this.isSittingState = sitOrBoat;
            this.refreshDimensions(); // 关键：刷新实体碰撞箱
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 先执行原版受伤逻辑
        boolean hurtSuccess = super.hurt(source, amount);
        if (!hurtSuccess) return false;

        // 获取攻击者，排除环境伤害、虚空、火焰等无攻击者场景
        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker) || attacker == this.getOwner()) {
            return true;
        }
        // 主人旗下其它驯服宠物视为盟友：被误伤不记仇、不广播反击
        if (livingAttacker instanceof TamableAnimal other && other.isTame()
                && other.getOwnerUUID() != null && other.getOwnerUUID().equals(this.getOwnerUUID())) {
            return true;
        }

        // 搜索半径 20 格内所有同类型 HumanoidCreatureEntity
        Level level = this.level();
        List<HumanoidCreatureEntity> nearbyAllies = level.getEntitiesOfClass(
                HumanoidCreatureEntity.class,
                this.getBoundingBox().inflate(20), // 搜寻半径20格，可自行调整
                ally -> ally != this // 排除自己
        );

        // 给每一只同类设置仇恨目标
        for (HumanoidCreatureEntity ally : nearbyAllies) {
            // 不攻击自己主人
            if (ally.getOwner() != livingAttacker) {
                ally.setTarget(livingAttacker);
            }
        }
        return true;
    }



    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level().isClientSide()) {
            boolean ownedOrTamed = isOwnedBy(player) || isTame();
            return ownedOrTamed ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (isTame()) {
            if (stack.is(Items.BREAD) && getHealth() < getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                heal(4.0f);
                playSound(ModSounds.HUMANOID_CREATURE_EAT.get(), 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }

            InteractionResult result = super.mobInteract(player, hand);
            if ((!result.consumesAction() || isBaby()) && isOwnedBy(player)) {
                setOrderedToSit(!isOrderedToSit());
                navigation.stop();
                setTarget(null);
                return InteractionResult.SUCCESS;
            }
            return result;
        } else if (stack.is(Items.BREAD)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (random.nextInt(3) == 0) {
                tame(player);
                navigation.stop();
                setTarget(null);
                setOrderedToSit(true);
                level().broadcastEntityEvent(this, (byte) 7);
                playSound(ModSounds.HUMANOID_CREATURE_TAME_SUCCESS.get(), 1.0f, 1.0f);
            } else {
                level().broadcastEntityEvent(this, (byte) 6);
                playSound(ModSounds.HUMANOID_CREATURE_TAME_FAIL.get(), 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BREAD);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        HumanoidCreatureEntity baby = ModEntities.HUMANOID_CREATURE.get().create(level);
        if (baby != null && isTame()) {
            baby.setOwnerUUID(getOwnerUUID());
            baby.setTame(true, true);
        }
        return baby;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && isAlive() && isTame() && isOrderedToSit()) {
            getNavigation().stop();
        }
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HUMANOID_CREATURE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.HUMANOID_CREATURE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HUMANOID_CREATURE_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        // 判断幼年，返回更高音调实现尖细叫声
        if (this.isBaby()) {
            // 1.5~1.7 推荐，数值越大声音越尖
            return 1.6F;
        }
        // 成年保留原版微小随机波动，不生硬固定1.0
        return (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            this.spawnHeartParticles();
        } else if (id == 6) {
            this.spawnSmokeParticles();
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void spawnHeartParticles() {
        for (int i = 0; i < 7; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.HEART,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
        }
    }

    private void spawnSmokeParticles() {
        for (int i = 0; i < 7; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwner() == owner) {
            return false;
        }
        if (target instanceof Player player && owner instanceof Player && !player.getAbilities().instabuild) {
            return true;
        }
        return super.wantsToAttack(target, owner);
    }
}