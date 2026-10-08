package cn.autoforged.yuzusoft.effect;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.custom.BlastProtectionEffect;
import cn.autoforged.yuzusoft.effect.custom.Effect0721;
import cn.autoforged.yuzusoft.effect.custom.Effect0721BadOmen;
import cn.autoforged.yuzusoft.effect.custom.Effect0721Hero;
import cn.autoforged.yuzusoft.effect.custom.Effect0721TrialOmen;
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
    public static final DeferredHolder<MobEffect, MobEffect> EFFECT_0721_BAD_OMEN =
            EFFECTS.register("effect_0721_bad_omen",
                    () -> new Effect0721BadOmen(MobEffectCategory.HARMFUL, 0x6B4A2B));
    /** "0721试炼之兆"：靠近试炼刷怪笼时由 0721 不祥之兆转换获得（镜像原版转换链路）。 */
    public static final DeferredHolder<MobEffect, MobEffect> EFFECT_0721_TRIAL_OMEN =
            EFFECTS.register("effect_0721_trial_omen",
                    () -> new Effect0721TrialOmen(MobEffectCategory.HARMFUL, 0x8B5A2B));
    /** "0721英雄"：击败 0721 袭击后获得，由 VillagerMixin 提供交易折扣。 */
    public static final DeferredHolder<MobEffect, MobEffect> EFFECT_0721_HERO =
            EFFECTS.register("effect_0721_hero",
                    () -> new Effect0721Hero(MobEffectCategory.BENEFICIAL, 0xFFD700));
    public static final DeferredHolder<MobEffect, MobEffect> STUN =
            EFFECTS.register("stun", () -> new StunEffect(MobEffectCategory.HARMFUL, 0x55FFFF)
                    .withSoundOnAdded(ModSounds.STUN_APPLY.get()));

    public static final DeferredHolder<MobEffect, MobEffect> FROST_BARRIER =
            EFFECTS.register("frost_barrier",
                    () -> new FrostBarrierEffect(MobEffectCategory.BENEFICIAL, 0x8EC8E8));

    /** "防爆"：免疫爆炸伤害（判定见 event.BlastProtectionHandler）。 */
    public static final DeferredHolder<MobEffect, MobEffect> BLAST_PROTECTION =
            EFFECTS.register("blast_protection",
                    () -> new BlastProtectionEffect(MobEffectCategory.BENEFICIAL, 0xD2601E));
}