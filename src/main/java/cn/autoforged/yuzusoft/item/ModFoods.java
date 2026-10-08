package cn.autoforged.yuzusoft.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import java.util.List;
import java.util.Optional;

public class ModFoods {
    public static final FoodProperties TAMAGOYAKI = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.6f)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1), 1.0f)
            .build();
    public static final FoodProperties DUMPLINGS = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0f)
            .build();

    // 血包不提供饱食度与饱和度，仅保留 alwaysEdible 以便满饥饿也能食用（用于恢复血量与吸收）
    public static final FoodProperties BLOOD_PACK_BASIC = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0f)
            .alwaysEdible()
            .build();

    public static final FoodProperties BLOOD_PACK_MID = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0f)
            .alwaysEdible()
            .build();

    public static final FoodProperties BLOOD_PACK_HIGH = new FoodProperties.Builder()
            .nutrition(0)
            .saturationModifier(0f)
            .alwaysEdible()
            .build();

    // 生物肉（并入自 knife 工程）：真实营养值在 MobMeatItem#finishUsingItem 中按
    // “先补饥饿、余量转饱和”动态结算，这里占位 0；isMeat=true 保留“肉”属性。
    public static final FoodProperties MOB_MEAT = new FoodProperties(0, 0.0F, true, 1.6F, Optional.empty(), List.of());
}
