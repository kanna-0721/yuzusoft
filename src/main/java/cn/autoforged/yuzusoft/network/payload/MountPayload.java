package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MountPayload(int entityId) implements CustomPacketPayload {
    public static final Type<MountPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "mount"));

    public static final StreamCodec<ByteBuf, MountPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MountPayload::entityId,
                    MountPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MountPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof DualFormMobEntity mob && !mob.isAngry() && !player.isPassenger()) {
                player.startRiding(mob);
            }
        });
    }
}