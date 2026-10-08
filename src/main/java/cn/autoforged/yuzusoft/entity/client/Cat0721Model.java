package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.Cat0721Entity;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/**
 * 0721猫的模型：直接复用原版猫的几何（OcelotModel.createBodyMesh），
 * 泛型绑定改为 Cat0721Entity（原版 CatModel 只接受 Cat 子类，无法复用）。
 */
public class Cat0721Model extends OcelotModel<Cat0721Entity> {

    public Cat0721Model(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // 与原版 CatModel.createBodyLayer 完全一致的几何与贴图尺寸（64x32）
        return LayerDefinition.create(OcelotModel.createBodyMesh(CubeDeformation.NONE), 64, 32);
    }
}
