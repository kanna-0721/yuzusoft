package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * 服务端 -> 客户端：同步某玩家的变身形态（用于客户端渲染其生物模型）。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public record SyncTransformationPayload(UUID playerId, String entityId) implements CustomPacketPayload {
    public static final Type<SyncTransformationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTransformationPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.playerId);
                buf.writeUtf(payload.entityId, 128);
            },
            buf -> new SyncTransformationPayload(buf.readUUID(), buf.readUtf(128)));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}