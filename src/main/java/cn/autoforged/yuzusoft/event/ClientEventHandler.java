package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.client.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GUARDIAN.get(), GuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_TRADER.get(), GuardianTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.FLOATING_SENTINEL.get(), FloatingSentinelRenderer::new);
        event.registerEntityRenderer(ModEntities.LEVITATION_BULLET.get(), LevitationBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.VILLAGE_GUARDIAN.get(), VillageGuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.SPRINKLER_CREEP.get(), SprinklerCreepRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_BALL.get(), WaterBallRenderer::new);
        event.registerEntityRenderer(ModEntities.FLASHBANG_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context, 1.5F, true));
        event.registerEntityRenderer(ModEntities.FLASHBANG_MONSTER.get(),
                FlashbangMonsterRenderer::new);
        event.registerEntityRenderer(ModEntities.HAMMER_WIELDER.get(), HammerWielderRenderer::new);
        event.registerEntityRenderer(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), BloodSuckerZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.EVIL_NANAMI.get(), EvilNanamiRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_GUARDIAN_V2.get(), FrostGuardianV2Renderer::new);
        event.registerEntityRenderer(ModEntities.FROST_BOLT_V2.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GuardianModel.LAYER_LOCATION, GuardianModel::createBodyLayer);
        event.registerLayerDefinition(guardiansModel.LAYER_LOCATION, guardiansModel::createBodyLayer);
        event.registerLayerDefinition(FloatingSentinelModel.LAYER_LOCATION, FloatingSentinelModel::createBodyLayer);
        event.registerLayerDefinition(VillageGuardianModel.LAYER_LOCATION, VillageGuardianModel::createBodyLayer);
        event.registerLayerDefinition(SprinklerCreepModel.LAYER_LOCATION, SprinklerCreepModel::createBodyLayer);
        event.registerLayerDefinition(FlashbangMonsterRenderer.ModelLayers.FLASHBANG_MONSTER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(HammerWielderModel.LAYER_LOCATION, HammerWielderModel::createBodyLayer);
        event.registerLayerDefinition(FrostGuardianV2Model.LAYER_LOCATION, FrostGuardianV2Model::createBodyLayer);
    }
}
