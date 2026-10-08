package cn.autoforged.yuzusoft;

import cn.autoforged.yuzusoft.charm.CharmEvents;
import cn.autoforged.yuzusoft.charm.CharmRegistry;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import cn.autoforged.yuzusoft.block.ModBlocks;
import cn.autoforged.yuzusoft.config.ModConfig;
import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.config.YuzusoftConfig;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.event.CleaverEvents;
import cn.autoforged.yuzusoft.event.NaturalSpawnGate;
import cn.autoforged.yuzusoft.event.ProjectileReflectHandler;
import cn.autoforged.yuzusoft.event.SpiritInteractionHandler;
import cn.autoforged.yuzusoft.item.ModArmorMaterials;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.potion.ModPotions;
import cn.autoforged.yuzusoft.recipe.ModRecipes;
import cn.autoforged.yuzusoft.sound.ModSounds;
import cn.autoforged.yuzusoft.ui.ModTabs;
import cn.autoforged.yuzusoft.worldgen.ModSpawnPlacements;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CycloneSwordMod.MODID)
public class CycloneSwordMod {
    public static final String MODID = "yuzusoft";

    public CycloneSwordMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 CLIENT 配置（配置界面由客户端类 CycloneSwordModClient 提供）
        modContainer.registerConfig(Type.CLIENT, ModConfig.SPEC);
        // 水灵（J 工程并入）：SERVER 配置（水弹/吸水/格挡参数）
        modContainer.registerConfig(Type.SERVER, WaterSpiritConfig.SPEC);
        // Yuzusoft 总配置（SERVER 型）：是否自然生成模组生物
        // 需指定独立文件名，避免与同为 SERVER 型的 WaterSpiritConfig 撞名（yuzusoft-server.toml）
        modContainer.registerConfig(Type.SERVER, YuzusoftConfig.SPEC, "yuzusoft-natural-spawns.toml");
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModSounds.register(modEventBus);
        ModTabs.CREATIVE_TABS.register(modEventBus);
        ModPotions.register(modEventBus);
        ModRecipes.register(modEventBus);
        // 阴影刺客专属刷怪判定（亮度=0）——实际登记在 RegisterSpawnPlacementsEvent（EntityAttributeHandler）
        ModSpawnPlacements.register();
        // 魅惑系统（并入自 bottle_of_charm）：注册对象 + 服务端事件
        CharmRegistry.init(modEventBus);
        NeoForge.EVENT_BUS.register(CharmEvents.class);
        // 水灵（J 工程并入）：服务端事件——弹射物反弹扫描 + 玩家与水灵交互（桶/瓶取水）
        NeoForge.EVENT_BUS.addListener(ProjectileReflectHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(SpiritInteractionHandler::onEntityInteract);
        // 自然刷怪门控（YuzusoftConfig.enableNaturalSpawns）
        NeoForge.EVENT_BUS.addListener(NaturalSpawnGate::onFinalizeSpawn);
        // 菜刀（并入自 knife 工程）：击杀掉肉 / 生锈与恢复 / 创造栏
        NeoForge.EVENT_BUS.addListener(CleaverEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(CleaverEvents::onPlayerTick);
        modEventBus.addListener(CleaverEvents::buildCreativeTabs);
    }
}
