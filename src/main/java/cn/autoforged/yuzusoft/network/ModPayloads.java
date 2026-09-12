package cn.autoforged.yuzusoft.network;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.LevitationBulletEntity;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.network.payload.ChestplateAbilityPayload;
import cn.autoforged.yuzusoft.network.payload.DismountPayload;
import cn.autoforged.yuzusoft.network.payload.MountPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CycloneSwordMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModPayloads {

    private static final int COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ChestplateAbilityPayload.TYPE,
                ChestplateAbilityPayload.STREAM_CODEC,
                ModPayloads::handleAbility);
        registrar.playToServer(
                MountPayload.TYPE,
                MountPayload.STREAM_CODEC,
                MountPayload::handle);
        registrar.playToServer(
                DismountPayload.TYPE,
                DismountPayload.STREAM_CODEC,
                DismountPayload::handle);
    }

    private static void handleAbility(ChestplateAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!serverPlayer.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.FLOATING_SENTINEL_CHESTPLATE.get())) {
                return;
            }
            long now = serverPlayer.level().getGameTime();
            Long last = COOLDOWNS.get(serverPlayer.getUUID());
            if (last != null && now - last < COOLDOWN_TICKS) {
                return;
            }
            COOLDOWNS.put(serverPlayer.getUUID(), now);

            LevitationBulletEntity bullet = new LevitationBulletEntity(serverPlayer.level(), serverPlayer);
            bullet.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(), 0.0F, 1.0F, 0.3F);
            serverPlayer.level().addFreshEntity(bullet);
            serverPlayer.playSound(SoundEvents.SHULKER_SHOOT, 1.0F, 0.8F);
        });
    }
}

