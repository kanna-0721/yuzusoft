package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.potion.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID)
public class ModBrewingRecipes {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(
                Potions.AWKWARD,
                Items.POINTED_DRIPSTONE,
                ModPotions.VULNERABILITY_POTION
        );

        builder.addMix(
                ModPotions.VULNERABILITY_POTION,
                Items.REDSTONE,
                ModPotions.LONG_VULNERABILITY_POTION
        );

        builder.addMix(
                ModPotions.VULNERABILITY_POTION,
                Items.GLOWSTONE_DUST,
                ModPotions.STRONG_VULNERABILITY_POTION
        );
    }
}