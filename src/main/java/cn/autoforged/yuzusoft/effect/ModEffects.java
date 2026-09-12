package cn.autoforged.yuzusoft.effect;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.custom.Effect0721;
import cn.autoforged.yuzusoft.effect.custom.FrostBarrierEffect;
import cn.autoforged.yuzusoft.effect.custom.StunEffect;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CycloneSwordMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> EFFECT_0721 =
            EFFECTS.register("effect_0721", () -> new Effect0721());
    public static final DeferredHolder<MobEffect, MobEffect> STUN =
            EFFECTS.register("stun", () -> new StunEffect(MobEffectCategory.HARMFUL, 0x55FFFF)
                    .withSoundOnAdded(ModSounds.STUN_APPLY.get()));

    public static final DeferredHolder<MobEffect, MobEffect> FROST_BARRIER =
            EFFECTS.register("frost_barrier",
                    () -> new FrostBarrierEffect(MobEffectCategory.BENEFICIAL, 0x8EC8E8));
}