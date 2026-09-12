package cn.autoforged.yuzusoft;

import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.block.ModBlocks;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModArmorMaterials;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.potion.ModPotions;
import cn.autoforged.yuzusoft.sound.ModSounds;
import cn.autoforged.yuzusoft.ui.ModTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CycloneSwordMod.MODID)
public class CycloneSwordMod {
    public static final String MODID = "yuzusoft";

    public CycloneSwordMod(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModSounds.register(modEventBus);
        ModTabs.CREATIVE_TABS.register(modEventBus);
        ModPotions.register(modEventBus);
    }
}
