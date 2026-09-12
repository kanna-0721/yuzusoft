package cn.autoforged.yuzusoft.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * 浮游使者胸甲。
 * 被动效果通过事件实现：
 * - 穿戴后免疫飘浮效果（飘浮免疫，默认开启）
 * - 穿戴后获得跳跃提升 I（set_bonus）
 * - 攻击命中目标时施加飘浮（effect_on_attack）
 * - 按下 J 键发射潜影贝漂浮弹（ability_on_key_j，冷却 60 ticks）
 */
public class FloatingSentinelChestplateItem extends ArmorItem {

    public FloatingSentinelChestplateItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
}

