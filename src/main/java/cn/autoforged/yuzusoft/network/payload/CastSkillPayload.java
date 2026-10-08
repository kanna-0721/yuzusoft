package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：请求释放当前变身形态的专属技能。
 * 无需携带数据，具体释放哪个技能由服务端依据玩家当前变身形态判定。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public record CastSkillPayload() implements CustomPacketPayload {
    public static final Type<CastSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "cast_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastSkillPayload> STREAM_CODEC =
            StreamCodec.unit(new CastSkillPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}