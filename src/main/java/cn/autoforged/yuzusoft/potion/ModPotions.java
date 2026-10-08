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

    /** "防爆药水"：免疫爆炸伤害，时长对齐原版抗火（普通 3600 tick / 3:00）。 */
    public static final DeferredHolder<Potion, Potion> BLAST_PROTECTION_POTION =
            POTIONS.register("blast_protection", () -> new Potion(
                    new MobEffectInstance(ModEffects.BLAST_PROTECTION, 3600, 0)
            ));

    /** "延长型防爆药水"：时长对齐原版延长抗火（9600 tick / 8:00）。 */
    public static final DeferredHolder<Potion, Potion> LONG_BLAST_PROTECTION_POTION =
            POTIONS.register("long_blast_protection", () -> new Potion(
                    new MobEffectInstance(ModEffects.BLAST_PROTECTION, 9600, 0)
            ));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}