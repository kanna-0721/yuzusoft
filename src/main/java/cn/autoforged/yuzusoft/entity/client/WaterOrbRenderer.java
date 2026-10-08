package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.WaterOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 水弹渲染器：以史莱姆球模型表现（ThrownItemRenderer 泛型限制为
 * ThrowableItemProjectile，水弹继承 ThrowableProjectile，故手写等价渲染）。
 */
public class WaterOrbRenderer extends EntityRenderer<WaterOrbEntity> {

    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;

    private final ItemRenderer itemRenderer;

    public WaterOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(WaterOrbEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.tickCount >= 2
                || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < MIN_CAMERA_DISTANCE_SQUARED)) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            this.itemRenderer.renderStatic(new ItemStack(Items.SLIME_BALL), ItemDisplayContext.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WaterOrbEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
