package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 水灵渲染器：人形模型 + 用户可替换贴图；
 * 名称牌常显（配合实体 getDisplayName 输出的 5 格水位条）。
 */
public class WaterSpiritRenderer extends MobRenderer<WaterSpiritEntity, WaterSpiritModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    CycloneSwordMod.MODID,
                    "textures/entity/water_spirit.png");

    public WaterSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterSpiritModel(context.bakeLayer(WaterSpiritModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterSpiritEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(WaterSpiritEntity entity) {
        return entity.isAlive() && WaterSpiritConfig.SHOW_WATER_BAR.get();
    }
}
