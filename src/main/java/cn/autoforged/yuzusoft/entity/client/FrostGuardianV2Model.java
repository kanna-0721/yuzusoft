package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FrostGuardianV2Entity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 冰霜守卫 V2 人形模型：基于标准细臂（Alex）体型构建（手臂 3px 宽）。
 * 防御模式双臂交叉抱于胸前，攻击模式抬起双臂作为状态反馈。
 */
@OnlyIn(Dist.CLIENT)
public class FrostGuardianV2Model<T extends FrostGuardianV2Entity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "frost_guardian_v2"), "main");

    public FrostGuardianV2Model(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // slim=true → 标准细臂(Alex)体型
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, true);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // 防御模式：双臂交叉抱于胸前；攻击模式：抬起双臂朝向目标
        if (entity.isDefenseMode()) {
            this.leftArm.xRot = -1.6F;
            this.rightArm.xRot = -1.6F;
            this.leftArm.zRot = 0.3F;
            this.rightArm.zRot = -0.3F;
        } else if (entity.getTarget() != null) {
            this.rightArm.xRot = -Mth.HALF_PI;
            this.leftArm.xRot = -Mth.HALF_PI;
        }
    }
}