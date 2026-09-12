package cn.autoforged.yuzusoft.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

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
}
