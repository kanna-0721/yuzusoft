package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChestplateAbilityPayload() implements CustomPacketPayload {
    public static final Type<ChestplateAbilityPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "chestplate_ability"));

    public static final StreamCodec<ByteBuf, ChestplateAbilityPayload> STREAM_CODEC =
            StreamCodec.unit(new ChestplateAbilityPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

