package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.FrostGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


public class FrostGuardianModel<T extends FrostGuardianEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "frost_guardian"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart hat;
    private final ModelPart jacket;
    private final ModelPart leftSleeve;
    private final ModelPart rightSleeve;
    private final ModelPart leftPants;
    private final ModelPart rightPants;

    public FrostGuardianModel(ModelPart root) {
        this.root = root;
        this.head      = root.getChild("head");
        this.body      = root.getChild("body");
        this.rightArm  = root.getChild("right_arm");
        this.leftArm   = root.getChild("left_arm");
        this.rightLeg  = root.getChild("right_leg");
        this.leftLeg   = root.getChild("left_leg");
        this.hat        = root.getChild("hat");
        this.jacket     = root.getChild("jacket");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve= root.getChild("right_sleeve");
        this.leftPants  = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;
        float walkSpeed = 0.6f;
        this.rightArm.xRot = Mth.cos(limbSwing * walkSpeed) * 1.4f * limbSwingAmount;
        this.leftArm.xRot  = Mth.cos(limbSwing * walkSpeed + Mth.PI) * 1.4f * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * walkSpeed + Mth.PI) * 1.4f * limbSwingAmount;
        this.leftLeg.xRot  = Mth.cos(limbSwing * walkSpeed) * 1.4f * limbSwingAmount;
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftPants.copyFrom(this.leftLeg);
    }
}

