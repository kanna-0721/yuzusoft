package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.DiamondGuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 来海渲染器：贴图路径
 * assets/yuzusoft/textures/entity/diamond_guardian.png。
 */
public class DiamondGuardianRenderer extends HumanoidMobRenderer<DiamondGuardianEntity, DiamondGuardianModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/diamond_guardian.png");

    public DiamondGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new DiamondGuardianModel(DiamondGuardianModel.bakeRoot()), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DiamondGuardianEntity entity) {
        return TEXTURE;
    }
}
