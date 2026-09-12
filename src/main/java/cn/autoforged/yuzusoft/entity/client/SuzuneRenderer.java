package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.SuzuneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Suzune 渲染：复用原版玩家模型（ModelLayers.PLAYER），
 * 自爆蓄力时完全复刻苦力怕的膨胀缩放 + 白色闪光覆盖。
 */
public class SuzuneRenderer extends MobRenderer<SuzuneEntity, HumanoidModel<SuzuneEntity>> {
    private static final ResourceLocation SUZUNE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/suzune.png");

    public SuzuneRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SuzuneEntity entity) {
        return SUZUNE_TEXTURE;
    }

    /**
     * 钻地隐藏期间完全不渲染：原版对 setInvisible(true) 的实体仍会绘制半透明幽灵模型，
     * 表现为「站在警报器上的贴图」；这里直接跳过渲染，只有真正钻出后才可见。
     */
    @Override
    public boolean shouldRender(SuzuneEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return !entity.isHidden() && super.shouldRender(entity, camera, camX, camY, camZ);
    }

    @Override
    protected void scale(SuzuneEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        float f = livingEntity.getSwelling(partialTickTime);
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        poseStack.scale(f2, f3, f2);
    }

    @Override
    protected float getWhiteOverlayProgress(SuzuneEntity livingEntity, float partialTicks) {
        float f = livingEntity.getSwelling(partialTicks);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }
}
