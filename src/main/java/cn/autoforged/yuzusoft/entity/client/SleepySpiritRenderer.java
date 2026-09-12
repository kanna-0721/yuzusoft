package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.SleepySpiritEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class SleepySpiritRenderer
        extends LivingEntityRenderer<SleepySpiritEntity, SleepySpiritModel<SleepySpiritEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/sleepy_spirit_entity.png");

    public SleepySpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SleepySpiritModel<>(context.bakeLayer(SleepySpiritModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SleepySpiritEntity entity) {
        return TEXTURE;
    }
    @Override
    protected boolean shouldShowName(SleepySpiritEntity entity) {
        return entity.hasCustomName() && entity.shouldShowName();
    }
    @Override
    protected void scale(SleepySpiritEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);

        }
    }
}