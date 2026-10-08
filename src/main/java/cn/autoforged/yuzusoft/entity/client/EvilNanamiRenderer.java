package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.EvilNanamiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 邪恶七海渲染器：贴图/模型与"在原七海"(arihara_nanami) 完全共用。
 */
public class EvilNanamiRenderer
        extends LivingEntityRenderer<EvilNanamiEntity, VillageGuardianModel<EvilNanamiEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/village_guardian.png");

    public EvilNanamiRenderer(EntityRendererProvider.Context context) {
        super(context, new VillageGuardianModel<>(context.bakeLayer(VillageGuardianModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(EvilNanamiEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(EvilNanamiEntity entity) {
        return false;
    }
}
