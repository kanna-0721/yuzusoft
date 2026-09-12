package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.VillageGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class VillageGuardianRenderer
        extends LivingEntityRenderer<VillageGuardianEntity, VillageGuardianModel<VillageGuardianEntity>> {  // ← 泛型第二参数改回你自己的 model

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/village_guardian.png");

    public VillageGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new VillageGuardianModel<>(context.bakeLayer(VillageGuardianModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(VillageGuardianEntity entity) {
        return TEXTURE;
    }
    @Override
    protected boolean shouldShowName(VillageGuardianEntity entity) {
        return false;
    }

    @Override
    protected void scale(VillageGuardianEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);

        }
    }
}

