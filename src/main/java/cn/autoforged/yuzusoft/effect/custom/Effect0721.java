package cn.autoforged.yuzusoft.effect.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Effect0721 extends MobEffect {
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "effect_0721_damage_reduction");

    public Effect0721() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, -0.4,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
