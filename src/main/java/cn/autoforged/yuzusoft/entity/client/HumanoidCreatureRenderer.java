package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.HumanoidCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public class HumanoidCreatureRenderer extends MobRenderer<HumanoidCreatureEntity, HumanoidCreatureModel<HumanoidCreatureEntity>> {

    public HumanoidCreatureRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidCreatureModel<>(context.bakeLayer(HumanoidCreatureModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HumanoidCreatureEntity entity) {
        boolean isSitting = entity.isOrderedToSit() || (entity.getVehicle() instanceof Boat);
        if (isSitting) {
            return ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/humanoid_creature_sit.png");
        } else {
            return ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "textures/entity/humanoid_creature.png");
        }
    }

    @Override
    protected void scale(HumanoidCreatureEntity entity, PoseStack poseStack, float partialTick) {
        float scale = entity.isBaby() ? 0.6F : 1.0F;
        poseStack.scale(scale, scale, scale);
    }
}