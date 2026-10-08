package cn.autoforged.yuzusoft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 生物肉组件数据：来源实体 ID + 营养值（等于来源生物最大生命值）。
 */
public record MobMeatData(ResourceLocation entity, int nutrition) {
    public static final Codec<MobMeatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(MobMeatData::entity),
            Codec.intRange(1, 4096).fieldOf("nutrition").forGetter(MobMeatData::nutrition)
    ).apply(instance, MobMeatData::new));

    public static final StreamCodec<ByteBuf, MobMeatData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}