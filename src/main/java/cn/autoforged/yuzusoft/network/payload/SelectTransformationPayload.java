package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：选择变身目标（生物 id；{@code minecraft:player} 表示变回玩家）。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public record SelectTransformationPayload(String entityId) implements CustomPacketPayload {
    public static final Type<SelectTransformationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "select"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectTransformationPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.entityId, 128),
            buf -> new SelectTransformationPayload(buf.readUtf(128)));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}