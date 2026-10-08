package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.WaterSpiritEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * 水灵人形模型框架：基于玩家模型（PlayerModel，细臂 Alex 体型），
 * 支持 64x64 皮肤贴图的内层 + 外层（layer）渲染：
 * - 内层：头/身体/手臂/腿（贴图上半 0-32 行）
 * - 外层：hat/jacket/sleeves/pants（贴图下半 32-64 行），皮肤自带外套/袖子/裤腿
 * 头部两侧与背后另加三片"水流鳍"作为轮廓特征。
 */
public class WaterSpiritModel extends PlayerModel<WaterSpiritEntity> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "nijouin_hazuki"), "main");

    public WaterSpiritModel(ModelPart root) {
        super(root, true); // slim=true：细臂 Alex 体型
        // 玩家耳朵部件与披风部件非水灵特征，隐藏；
        // hat/jacket/sleeves/pants 保持可见以显示皮肤外层 layer
        root.getChild("ear").visible = false;
        root.getChild("cloak").visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        // 玩家模型网格（slim Alex）：含内层 + 全套外层部件（hat/jacket/sleeves/pants）
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        // 左右水流鳍
        head.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-2.5F, -7.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(0, 48)
                .addBox(1.5F, -7.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.ZERO);
        PartDefinition body = root.getChild("body");
        // 背后垂尾
        body.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(8, 48)
                .addBox(-1.0F, 0.0F, 2.0F, 2.0F, 10.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
