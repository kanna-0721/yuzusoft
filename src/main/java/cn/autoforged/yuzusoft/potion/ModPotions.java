package cn.autoforged.yuzusoft.potion;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.effect.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, CycloneSwordMod.MODID);

    public static final DeferredHolder<Potion, Potion> VULNERABILITY_POTION =
            POTIONS.register("vulnerability", () -> new Potion(
                    new MobEffectInstance(ModEffects.EFFECT_0721, 900, 0)
            ));

    public static final DeferredHolder<Potion, Potion> LONG_VULNERABILITY_POTION =
            POTIONS.register("long_vulnerability", () -> new Potion(
                    new MobEffectInstance(ModEffects.EFFECT_0721, 1800, 0)
            ));

    public static final DeferredHolder<Potion, Potion> STRONG_VULNERABILITY_POTION =
            POTIONS.register("strong_vulnerability", () -> new Potion(
                    new MobEffectInstance(ModEffects.EFFECT_0721, 600, 1)
            ));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}