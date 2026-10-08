package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.HumanoidCreatureEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

public class HumanoidCreatureModel<T extends HumanoidCreatureEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "tanikaze_amane"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart hat;
    private final ModelPart jacket;
    private final ModelPart leftSleeve;
    private final ModelPart rightSleeve;
    private final ModelPart leftPants;
    private final ModelPart rightPants;

// 构造函数内追加（名字必须和上面 addOrReplaceChild 的 name 完全一致）

    public HumanoidCreatureModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.hat        = root.getChild("hat");
        this.jacket     = root.getChild("jacket");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve= root.getChild("right_sleeve");
        this.leftPants  = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.5f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("jacket",
                CubeListBuilder.create()
                        .texOffs(16, 32)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create()
                        .texOffs(48, 48)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)),
                PartPose.offset(5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror()
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create()
                        .texOffs(40, 32).mirror()
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(2.0f, 12.0f, 0.0f));
        root.addOrReplaceChild("left_pants",
                CubeListBuilder.create()
                        .texOffs(0, 48)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)),
                PartPose.offset(2.0f, 12.0f, 0.0f));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror()
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-2.0f, 12.0f, 0.0f));
        root.addOrReplaceChild("right_pants",
                CubeListBuilder.create()
                        .texOffs(0, 32).mirror()
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)),
                PartPose.offset(-2.0f, 12.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        if (entity.isInSittingPose()) {
            this.body.y = 0f;
            this.head.y = 0f;
            this.rightArm.y = 0.6f;
            this.leftArm.y = 0.6f;
            this.rightArm.xRot = -0.5f;
            this.leftArm.xRot = -0.5f;
            this.rightLeg.y = 14.0f;
            this.leftLeg.y = 14.0f;
            this.rightLeg.xRot = -1.2f;
            this.leftLeg.xRot = -1.2f;
        } else {
            float walkSpeed = 0.6662f;
            this.rightArm.xRot = Mth.cos(limbSwing * walkSpeed) * 1.0f * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * walkSpeed + Mth.PI) * 1.0f * limbSwingAmount;
            this.rightLeg.xRot = Mth.cos(limbSwing * walkSpeed + Mth.PI) * 1.0f * limbSwingAmount;
            this.leftLeg.xRot = Mth.cos(limbSwing * walkSpeed) * 1.0f * limbSwingAmount;
        }
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftPants.copyFrom(this.leftLeg);
    }
}