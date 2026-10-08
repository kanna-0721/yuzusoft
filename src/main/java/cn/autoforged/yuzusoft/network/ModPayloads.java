package cn.autoforged.yuzusoft.network;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.entity.custom.LevitationBulletEntity;
import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.network.payload.CastSkillPayload;
import cn.autoforged.yuzusoft.network.payload.ChestplateAbilityPayload;
import cn.autoforged.yuzusoft.network.payload.ClimbPayload;
import cn.autoforged.yuzusoft.network.payload.DismountPayload;
import cn.autoforged.yuzusoft.network.payload.MountPayload;
import cn.autoforged.yuzusoft.network.payload.OpenTransformationListPayload;
import cn.autoforged.yuzusoft.network.payload.SelectTransformationPayload;
import cn.autoforged.yuzusoft.network.payload.SyncTransformationPayload;
import cn.autoforged.yuzusoft.transformation.SkillSystem;
import cn.autoforged.yuzusoft.transformation.TransformationSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

        // ===== 变身法杖（并入自 mod_92152f0a 工程）=====
        registrar.playToClient(
                OpenTransformationListPayload.TYPE,
                OpenTransformationListPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> cn.autoforged.yuzusoft.transformation.client.TransformationScreen.open(payload.unlocked())));
        registrar.playToClient(
                SyncTransformationPayload.TYPE,
                SyncTransformationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> cn.autoforged.yuzusoft.transformation.client.ClientTransformation.set(payload.playerId(), payload.entityId())));
        registrar.playToServer(
                SelectTransformationPayload.TYPE,
                SelectTransformationPayload.STREAM_CODEC,
                ModPayloads::handleSelect);
        registrar.playToServer(
                CastSkillPayload.TYPE,
                CastSkillPayload.STREAM_CODEC,
                ModPayloads::handleCast);
        registrar.playToServer(
                ClimbPayload.TYPE,
                ClimbPayload.STREAM_CODEC,
                ModPayloads::handleClimb);
    }

    /** 选择变身目标：校验已解锁 + 手持法杖 + 未处于该形态，然后扣耐久并变身。 */
    private static void handleSelect(SelectTransformationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            var list = player.getPersistentData().getList("transformation_unlocked", Tag.TAG_STRING);
            boolean unlocked = payload.entityId().equals("minecraft:player");
            for (int i = 0; i < list.size(); i++) {
                if (list.getString(i).equals(payload.entityId())) unlocked = true;
            }
            ItemStack wand = player.getMainHandItem().is(ModItems.TRANSFORMATION_WAND.get())
                    ? player.getMainHandItem() : player.getOffhandItem();
            if (unlocked && !payload.entityId().equals(TransformationSystem.current(player))
                    && wand.is(ModItems.TRANSFORMATION_WAND.get())) {
                if (wand.getDamageValue() < wand.getMaxDamage()) {
                    wand.hurtAndBreak(1, level, player, stack -> {});
                    TransformationSystem.transform(level, player, payload.entityId());
                }
            }
        });
    }

    /** 释放当前变身形态的专属技能（服务端含手持法杖与冷却校验）。 */
    private static void handleCast(CastSkillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) SkillSystem.cast(player);
        });
    }

    /** 上报攀爬状态（贴墙按移动键时每刻续期）。 */
    private static void handleClimb(ClimbPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) SkillSystem.setClimbing(player, payload.climbing());
        });
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

