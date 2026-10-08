package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.BloodSuckerZombieEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 血族僵尸渲染器。
 *
 * <p>实体改继承 {@code Raider} 后不再是 {@code Zombie}，无法再使用
 * {@code AbstractZombieRenderer<T extends Zombie>}，改挂
 * {@code HumanoidMobRenderer}；模型部件取原版 {@code ModelLayers.ZOMBIE}，
 * 盔甲（含袭击必戴的铁头盔）由 {@code HumanoidMobRenderer} 自动叠加。
 */
@OnlyIn(Dist.CLIENT)
public class BloodSuckerZombieRenderer extends HumanoidMobRenderer<BloodSuckerZombieEntity, BloodSuckerZombieModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/blood_sucker_zombie.png");

    public BloodSuckerZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new BloodSuckerZombieModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.addLayer(new BloodSuckLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(BloodSuckerZombieEntity entity) {
        return TEXTURE;
    }
}
