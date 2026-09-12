package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.ModEntities;
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
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FrostGuardianModel.LAYER_LOCATION, FrostGuardianModel::createBodyLayer);
        event.registerLayerDefinition(SleepySpiritModel.LAYER_LOCATION, SleepySpiritModel::createBodyLayer);
        event.registerLayerDefinition(HumanoidCreatureModel.LAYER_LOCATION, HumanoidCreatureModel::createBodyLayer);
    }
}
