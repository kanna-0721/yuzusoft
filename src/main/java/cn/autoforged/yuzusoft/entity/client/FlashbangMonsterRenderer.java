package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FlashbangMonster;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FlashbangMonsterRenderer extends HumanoidMobRenderer<FlashbangMonster, HumanoidModel<FlashbangMonster>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/flashbang_monster.png");

    public FlashbangMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FlashbangMonster entity) {
        return TEXTURE;
    }

    public static class ModelLayers {
        public static final ModelLayerLocation FLASHBANG_MONSTER =
                new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "nabari_anju"), "main");
    }
}

