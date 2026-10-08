package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.DiamondGuardianEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 来海的 Alex 细臂人形模型（手臂宽 3px，对齐 PlayerModel slim 分支）。
 */
public class DiamondGuardianModel extends HumanoidModel<DiamondGuardianEntity> {

    public DiamondGuardianModel(ModelPart root) {
        super(root);
    }

    /** 细臂(Alex)人形网格：手臂宽 3px，布局/UV 对齐原版 PlayerModel 的 slim 分支。 */
    public static MeshDefinition mesh() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        // Alex 细臂：宽 3px，y 偏移 2.5F，UV 与 PlayerModel.createMesh(..., true) 的 slim 分支一致
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
        return mesh;
    }

    /** 直接烘焙出根 ModelPart，供渲染器构造使用。 */
    public static ModelPart bakeRoot() {
        return LayerDefinition.create(mesh(), 64, 64).bakeRoot();
    }
}
