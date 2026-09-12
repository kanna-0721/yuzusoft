package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.SprinklerCreepEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SprinklerCreepRenderer extends HumanoidMobRenderer<SprinklerCreepEntity, HumanoidModel<SprinklerCreepEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CycloneSwordMod.MODID, "textures/entity/sprinkler_creep.png");

    public SprinklerCreepRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SprinklerCreepEntity entity) {
        return TEXTURE;
    }

}

