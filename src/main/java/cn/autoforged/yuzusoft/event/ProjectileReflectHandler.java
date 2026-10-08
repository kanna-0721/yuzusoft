package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.item.custom.WaterSpiritUmbrellaItem;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

/**
 * 全弹射物镜像反弹（服务端每 tick 扫描）：
 * - 水灵：自动反弹进入其判定半径、来自正面锥内的任意类型弹射物；
 * - 玩家副手雨伞：格挡状态下反弹正面来袭弹射物（范围同盾牌），每次反弹消耗耐久。
 * 反弹规则：速度大小不变、方向相反（v -> -v），并把弹射物归属改为反弹者，
 * 使其不会立刻伤到反弹者自己。
 */
public final class ProjectileReflectHandler {

    private ProjectileReflectHandler() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 副手雨伞反弹（水灵的反弹扫描在 WaterSpiritEntity#tick 中就地处理）
            for (ServerPlayer player : level.players()) {
                if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.OFF_HAND) {
                    continue;
                }
                if (player.getUseItem().getItem() instanceof WaterSpiritUmbrellaItem) {
                    reflectInbound(level, player, player);
                }
            }
        }
    }

    public static void reflectInbound(ServerLevel level, LivingEntity reflector, ServerPlayer umbrellaOwner) {
        double range = WaterSpiritConfig.REFLECT_RANGE.get();
        double minDot = WaterSpiritConfig.REFLECT_MIN_DOT.get();
        AABB area = reflector.getBoundingBox().inflate(range);
        List<Projectile> incoming = level.getEntitiesOfClass(Projectile.class, area);
        for (Projectile projectile : incoming) {
            if (projectile.getOwner() == reflector) {
                continue;
            }
            Vec3 velocity = projectile.getDeltaMovement();
            double speed = velocity.length();
            if (speed < 1.0E-3D || projectile.onGround()) {
                continue;
            }
            Vec3 toReflector = new Vec3(
                    reflector.getX() - projectile.getX(),
                    reflector.getEyeY() - projectile.getY(),
                    reflector.getZ() - projectile.getZ());
            double dist = toReflector.length();
            if (dist > range + 1.0D) {
                continue;
            }
            // 只要正面锥形（同盾牌的格挡角判定思路）
            double dot = velocity.dot(toReflector.scale(1.0D / Math.max(dist, 1.0E-3D))) / speed;
            if (dot < minDot) {
                continue;
            }
            // 玩家举伞额外要求：弹射物必须位于玩家视线前方 180 度半球内（正面 180 度才格挡）；
            // 水灵（umbrellaOwner == null）不做该判定，天然全方向免疫弹射物
            if (umbrellaOwner != null) {
                Vec3 look = umbrellaOwner.getLookAngle();
                Vec3 toProjectile = new Vec3(
                        projectile.getX() - reflector.getX(),
                        projectile.getEyeY() - reflector.getEyeY(),
                        projectile.getZ() - reflector.getZ());
                double len = toProjectile.length();
                if (len < 1.0E-3D) {
                    continue;
                }
                if (toProjectile.scale(1.0D / len).dot(look) < 0.0D) {
                    continue; // 弹射物在身后半球，不格挡
                }
            }
            projectile.setDeltaMovement(velocity.scale(-1.0D));
            projectile.setOwner(reflector);
            level.sendParticles(ParticleTypes.SPLASH,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
            if (umbrellaOwner != null) {
                int cost = WaterSpiritConfig.UMBRELLA_REFLECT_DURABILITY_COST.get();
                ItemStack off = umbrellaOwner.getOffhandItem();
                if (cost > 0 && !off.isEmpty()) {
                    off.hurtAndBreak(cost, umbrellaOwner, EquipmentSlot.OFFHAND);
                }
                umbrellaOwner.level().playSound(null, umbrellaOwner,
                        SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 1.3F);
                if (!(umbrellaOwner.getOffhandItem().getItem() instanceof WaterSpiritUmbrellaItem)) {
                    umbrellaOwner.releaseUsingItem();
                }
            } else {
                level.playSound(null, reflector, ModSounds.WATER_SPIRIT_TAKE.get(),
                        SoundSource.NEUTRAL, 0.8F, 1.4F);
            }
        }
    }
}
