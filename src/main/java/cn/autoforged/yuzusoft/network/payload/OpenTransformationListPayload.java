package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 服务端 -> 客户端：打开变身列表界面，携带该玩家已解锁的生物 id。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public record OpenTransformationListPayload(List<String> unlocked) implements CustomPacketPayload {
    public static final Type<OpenTransformationListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "open_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTransformationListPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.unlocked.size());
                for (String id : payload.unlocked) buf.writeUtf(id, 128);
            },
            buf -> {
                int size = Math.min(buf.readVarInt(), 512);
                java.util.ArrayList<String> ids = new java.util.ArrayList<>(size);
                for (int i = 0; i < size; i++) ids.add(buf.readUtf(128));
                return new OpenTransformationListPayload(List.copyOf(ids));
            });

    public OpenTransformationListPayload() { this(List.of()); }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}