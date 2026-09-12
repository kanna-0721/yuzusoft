package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FrostGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrostGuardianRenderer extends LivingEntityRenderer<FrostGuardianEntity,FrostGuardianModel<FrostGuardianEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/frost_guardian.png");

    public FrostGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new FrostGuardianModel<>(context.bakeLayer(FrostGuardianModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostGuardianEntity entity) {
        return TEXTURE;
    }
    @Override
    protected boolean shouldShowName(FrostGuardianEntity entity) {
        return false;
    }

    @Override
    protected void scale(FrostGuardianEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);

        }
    }
}
