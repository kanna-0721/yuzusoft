package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.Cat0721Entity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 0721猫的渲染器：模型烘培层复用原版 ModelLayers.CAT，贴图固定为英国短毛猫。
 * 替代原版 CatRenderer（其要求实体为 Cat 子类，改继承 Monster 后无法再复用）。
 */
public class Cat0721Renderer extends MobRenderer<Cat0721Entity, Cat0721Model> {

    private static final ResourceLocation CAT_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/cat/british_shorthair.png");

    public Cat0721Renderer(EntityRendererProvider.Context context) {
        super(context, new Cat0721Model(context.bakeLayer(ModelLayers.CAT)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(Cat0721Entity entity) {
        return CAT_TEXTURE;
    }
}
