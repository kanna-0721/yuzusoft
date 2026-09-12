package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.BloodSuckerZombieEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodSuckLayer extends RenderLayer<BloodSuckerZombieEntity, ZombieModel<BloodSuckerZombieEntity>> {

    private static final ResourceLocation BABY_LAYER = ResourceLocation.fromNamespaceAndPath(
            CycloneSwordMod.MODID, "textures/entity/blood_sucker_zombie_baby_layer.png");

    public BloodSuckLayer(RenderLayerParent<BloodSuckerZombieEntity, ZombieModel<BloodSuckerZombieEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BloodSuckerZombieEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        if (!entity.isBaby()) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BABY_LAYER));
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}