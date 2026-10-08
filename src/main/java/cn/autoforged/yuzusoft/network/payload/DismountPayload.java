package cn.autoforged.yuzusoft.network.payload;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.entity.custom.DualFormMobEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DismountPayload() implements CustomPacketPayload {
    public static final Type<DismountPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "dismount"));

    public static final StreamCodec<ByteBuf, DismountPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {
            }, buf -> new DismountPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DismountPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!player.isPassenger()) {
                return;
            }
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof DualFormMobEntity
                    || (vehicle instanceof Mob mob
                    && Boolean.TRUE.equals(mob.getExistingDataOrNull(ModAttachments.FUSION_RIDEABLE)))) {
                player.stopRiding();
            }
        });
    }
}