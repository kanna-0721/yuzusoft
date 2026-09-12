package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FloatingSentinelEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FloatingSentinelRenderer extends HumanoidMobRenderer<FloatingSentinelEntity, HumanoidModel<FloatingSentinelEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/floating_sentinel/entity.png");

    public FloatingSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F);

    }

    @Override
    public ResourceLocation getTextureLocation(FloatingSentinelEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FloatingSentinelEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 普通人形生物：直接站在地面上渲染，无上浮偏移
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}

