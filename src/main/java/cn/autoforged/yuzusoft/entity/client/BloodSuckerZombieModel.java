package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.BloodSuckerZombieEntity;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 血族僵尸的渲染模型。
 *
 * <p>实体改为继承 {@code Raider} 后不再是 {@code Zombie}，原版
 * {@code ZombieModel<T extends Zombie>} 无法直接使用；这里继承
 * {@code AbstractZombieModel<T extends Monster>} 保留僵尸的外观动画
 * （头部倾斜、攻击时双臂前伸），模型部件仍取原版 {@code ModelLayers.ZOMBIE}。
 */
@OnlyIn(Dist.CLIENT)
public class BloodSuckerZombieModel extends AbstractZombieModel<BloodSuckerZombieEntity> {

    public BloodSuckerZombieModel(ModelPart root) {
        super(root);
    }

    /** 与原版 {@code ZombieModel} 一致：是否处于攻击姿态（决定双臂前伸）。 */
    @Override
    public boolean isAggressive(BloodSuckerZombieEntity entity) {
        return entity.isAggressive();
    }
}
