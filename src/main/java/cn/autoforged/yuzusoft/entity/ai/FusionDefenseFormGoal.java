package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 融合生物的防御形态：照抄真雪（{@code FrostGuardianV2Entity}）——
 * 目标贴近 8 格触发，进入防御形态 200 刻（激活冰霜屏障、攻速 -50%），
 * 到期后退回攻形态，冷却 300 刻。
 * <p>
 * 不占控制旗标；原生物用同步数据驱动客户端姿态，融合体是既有实体类型、
 * 加不了新的同步数据，故这里只保留服务端的属性 / 效果差异。
 */
public class FusionDefenseFormGoal extends Goal {
    private static final double TRIGGER_RANGE = 8.0D;
    private static final int DURATION_TICKS = 200;
    private static final int COOLDOWN_TICKS = 300;
    private static final ResourceLocation ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "fusion_defense_attack_speed_reduction");

    private final Mob mob;
    private int remainingTicks;
    private int nextAllowedTick;

    public FusionDefenseFormGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.remainingTicks > 0) {
            return true;
        }
        if (this.mob.tickCount < this.nextAllowedTick) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive()
                && this.mob.distanceToSqr(target) <= TRIGGER_RANGE * TRIGGER_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return this.remainingTicks > 0;
    }

    @Override
    public void start() {
        this.remainingTicks = DURATION_TICKS;
        enterDefense();
    }

    @Override
    public void tick() {
        if (this.remainingTicks > 0) {
            this.remainingTicks--;
        }
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        this.nextAllowedTick = this.mob.tickCount + COOLDOWN_TICKS;
        exitDefense();
    }

    private void enterDefense() {
        this.mob.addEffect(new MobEffectInstance(ModEffects.FROST_BARRIER, DURATION_TICKS, 0,
                false, false, true));
        AttributeInstance attackSpeed = this.mob.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null && !attackSpeed.hasModifier(ATTACK_SPEED_ID)) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID, -0.5D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (this.mob.level() instanceof ServerLevel level) {
            level.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    ModSounds.FROST_GUARDIAN_V2_MODE_SWITCH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.mob.getX(), this.mob.getY() + 1.0D,
                    this.mob.getZ(), 30, 3.0D, 1.0D, 3.0D, 0.0D);
        }
    }

    private void exitDefense() {
        this.mob.removeEffect(ModEffects.FROST_BARRIER);
        AttributeInstance attackSpeed = this.mob.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
        }
        if (this.mob.level() instanceof ServerLevel level) {
            level.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                    ModSounds.FROST_GUARDIAN_V2_MODE_SWITCH.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
        }
    }
}