package cn.autoforged.yuzusoft.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 通用生物头颅方块：完全复刻原版骷髅头（{@link SkullBlock}）管线。
 * 继承原版 SkullBlock → 支持 16 向旋转（ROTATION）、可放入头盔槽装备
 * （AbstractSkullBlock implements Equipable，Equipable.get() 自动识别 BlockItem 的方块）。
 * 方块实体复用原版 SkullBlockEntity（type=BlockEntityType.SKULL），由原版 SkullBlockRenderer 渲染；
 * 各 {@link MobSkullType} 的模型与贴图通过 CreateSkullModels 事件与 SKIN_BY_TYPE 注入。
 * 由对应生物死亡时概率掉落（见 HeadDropHandler）。
 */
public class MobHeadBlock extends SkullBlock {
    /**
     * 与原版 SkullBlock 一致：把 {@link MobSkullType} 的 serializedName 写入 "kind" 字段。
     * 注意不能用原版 SkullBlock.CODEC——它的 CODEC 只认 SkullBlock.Types 注册表里的原版类型。
     */
    public static final MapCodec<MobHeadBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(MobSkullType::values).fieldOf("kind").forGetter(MobHeadBlock::getSkullType),
            propertiesCodec()
    ).apply(instance, MobHeadBlock::new));

    private final MobSkullType skullType;

    public MobHeadBlock(MobSkullType skullType, BlockBehaviour.Properties properties) {
        super(skullType, properties);
        this.skullType = skullType;
    }

    public MobSkullType getSkullType() {
        return this.skullType;
    }

    @Override
    public MapCodec<? extends SkullBlock> codec() {
        return CODEC;
    }
}
