package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FrostGuardianV2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 冰霜守卫 V2 渲染器：贴图路径 textures/entity/frost_guardian_v2.png。
 */
@OnlyIn(Dist.CLIENT)
public class FrostGuardianV2Renderer extends MobRenderer<FrostGuardianV2Entity, FrostGuardianV2Model<FrostGuardianV2Entity>> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/frost_guardian_v2.png");

    public FrostGuardianV2Renderer(EntityRendererProvider.Context context) {
        super(context, new FrostGuardianV2Model<>(context.bakeLayer(FrostGuardianV2Model.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostGuardianV2Entity entity) {
        return TEXTURE;
    }
}