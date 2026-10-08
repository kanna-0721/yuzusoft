package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.ShimagoeTsukumi;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/** 岛越月望渲染器：复用玩家细臂模型（PLAYER_SLIM），手持三角钢琴。 */
public class ShimagoeTsukumiRenderer extends HumanoidMobRenderer<ShimagoeTsukumi, HumanoidModel<ShimagoeTsukumi>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/shimagoe_tsukumi.png");

    public ShimagoeTsukumiRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ShimagoeTsukumi entity) {
        return TEXTURE;
    }
}