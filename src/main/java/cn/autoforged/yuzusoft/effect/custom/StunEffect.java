package cn.autoforged.yuzusoft.effect.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class StunEffect extends MobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "effect.stun.movement_speed");

    public StunEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER_ID,
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            Vec3 delta = entity.getDeltaMovement();
            entity.setDeltaMovement(0.0, delta.y, 0.0);
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
                mob.getNavigation().stop();
            }
        }
        return true;
    }
}

