package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：上报"正在贴着墙攀爬"。
 * <p>
 * 1.21.1 的原版客户端只在骑乘时才发送 {@code ServerboundPlayerInputPacket}，
 * 步行时服务端拿不到 {@code xxa/zza}；且服务端是用客户端上报的位移做 {@code move()}，
 * 贴墙时位移已被墙消解，{@code horizontalCollision} 也不会置位。
 * 因此"是否贴墙按着移动键"只能由客户端判定后上报。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public record ClimbPayload(boolean climbing) implements CustomPacketPayload {
    public static final Type<ClimbPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "climb"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClimbPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ClimbPayload::climbing, ClimbPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}