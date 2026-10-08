package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.custom.MobSkullType;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = CycloneSwordMod.MODID)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GUARDIAN.get(), GuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_TRADER.get(), GuardianTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.SHADOW_ASSASSIN.get(), ShadowAssassinRenderer::new);
        event.registerEntityRenderer(ModEntities.SHADOW_DART.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DETONATOR_THROWING_MONSTER.get(), DetonatorMonsterRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_GUARDIAN.get(), FrostGuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.DETONATOR_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_HEAL_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GUITAR_MONSTER.get(), GuitarMonsterRenderer::new);
        event.registerEntityRenderer(ModEntities.SLEEPY_SPIRIT.get(), SleepySpiritRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_BOLT.get(),
                context -> new ThrownItemRenderer<>(context, 1.0F, true));
        event.registerEntityRenderer(ModEntities.DUAL_FORM_MOB.get(), DualFormMobRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMANOID_CREATURE.get(), HumanoidCreatureRenderer::new);
        event.registerEntityRenderer(ModEntities.SUZUNE.get(), SuzuneRenderer::new);
        event.registerEntityRenderer(ModEntities.CAT_0721.get(), Cat0721Renderer::new);
        // ---- 来海（kurumi 工程并入）----
        event.registerEntityRenderer(ModEntities.DIAMOND_GUARDIAN.get(), DiamondGuardianRenderer::new);
        // ---- 水灵（J 工程并入）----
        event.registerEntityRenderer(ModEntities.WATER_SPIRIT.get(), WaterSpiritRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_ORB.get(), WaterOrbRenderer::new);
        // ---- 岛越月望（PianoNeoForge 工程并入）----
        event.registerEntityRenderer(ModEntities.SHIMAGOE_TSUKUMI.get(), ShimagoeTsukumiRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FrostGuardianModel.LAYER_LOCATION, FrostGuardianModel::createBodyLayer);
        event.registerLayerDefinition(SleepySpiritModel.LAYER_LOCATION, SleepySpiritModel::createBodyLayer);
        event.registerLayerDefinition(HumanoidCreatureModel.LAYER_LOCATION, HumanoidCreatureModel::createBodyLayer);
        event.registerLayerDefinition(WaterSpiritModel.LAYER, WaterSpiritModel::createBodyLayer);
    }

    /**
     * 所有生物头颅：向原版 SkullBlockRenderer 注册自定义骷髅类型的模型
     * （统一复用僵尸头模型 ZOMBIE_HEAD 的人形头形状），同时覆盖方块、实体与玩家穿戴渲染。
     */
    @SubscribeEvent
    public static void registerSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        for (MobSkullType type : MobSkullType.values()) {
            event.registerSkullModel(type,
                    new SkullModel(event.getEntityModelSet().bakeLayer(ModelLayers.ZOMBIE_HEAD)));
        }
    }
}
