package cn.autoforged.yuzusoft;

import cn.autoforged.yuzusoft.block.custom.MobSkullType;
import cn.autoforged.yuzusoft.charm.client.CharmClient;
import cn.autoforged.yuzusoft.entity.client.UmbrellaFirstPersonHandler;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.item.client.DecapitatedBodyItemRenderer;
import cn.autoforged.yuzusoft.item.client.MobMeatItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 客户端入口：为模组注册配置界面，并挂载魅惑系统客户端表现（染色/粒子）。
 * 仅客户端加载；NeoForge 内置 ConfigurationScreen 会自动为 ModConfigSpec 生成编辑界面。
 */
@Mod(value = CycloneSwordMod.MODID, dist = Dist.CLIENT)
public class CycloneSwordModClient {

    public CycloneSwordModClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        CharmClient.register(modEventBus);
        // 水灵（J 工程并入）：副手举伞的第一人称渲染 + blocking 谓词注册
        NeoForge.EVENT_BUS.addListener(UmbrellaFirstPersonHandler::onRenderHand);
        // 各生物头颅：注册实体贴图（方块 BE 渲染与玩家穿戴渲染共用，经 SkullBlockRenderer.SKIN_BY_TYPE）
        for (MobSkullType type : MobSkullType.values()) {
            SkullBlockRenderer.SKIN_BY_TYPE.put(type,
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, type.getTexturePath()));
        }
        modEventBus.addListener(this::clientSetup);
        // 生物肉（并入自 knife 工程）：builtin/entity 自定义渲染器，显示来源生物的迷你模型
        modEventBus.addListener(this::registerItemExtensions);
    }

    /**
     * 为生物肉注册 builtin/entity 自定义渲染器，读取组件中的来源实体画“迷你生物”。
     */
    private void registerItemExtensions(final RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return MobMeatItemRenderer.INSTANCE;
            }
        }, ModItems.MOB_MEAT.get());
        // 无头身体（手术刀）：builtin/entity 自定义渲染器，显示来源生物的躯体（隐藏头部）
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DecapitatedBodyItemRenderer.INSTANCE;
            }
        }, ModItems.DECAPITATED_BODY.get());
    }

    /**
     * 注册水精灵伞的 blocking 谓词（与盾牌一致）：实体使用中且使用物品为伞时返回 1，
     * 驱动模型 override 切换到开伞模型。需在注册表冻结后的客户端启动阶段执行。
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.WATER_SPIRIT_UMBRELLA.get(),
                ResourceLocation.withDefaultNamespace("blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));
    }
}
