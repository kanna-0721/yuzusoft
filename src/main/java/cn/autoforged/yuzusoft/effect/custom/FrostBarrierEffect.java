package cn.autoforged.yuzusoft.effect.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 冰霜屏障：不可见的防御屏障。效果存续期间，把以持有者为中心、半径内的其他实体
 * 持续向四周推开（弹开/驱散）。屏障本身不显示任何方块/模型（barrier_invisible=true）。
 *
 * 距离/半径：barrier_radius=4（标准范围）。可被卡片与冰霜守卫共用。
 */
public class FrostBarrierEffect extends MobEffect {
    public static final double BARRIER_RADIUS = 4.0;

    public FrostBarrierEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** 屏障效果开始时不做任何音效（卡片使用与守卫施放均不播，静默生效）。 */
    @Override
    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        super.onEffectStarted(livingEntity, amplifier);
    }

    /** 每 tick 应用：推开屏障内的其它实体。 */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().isClientSide()) {
            return true;
        }
        repelNearby(livingEntity);
        return true;
    }

    private void repelNearby(LivingEntity holder) {
        AABB area = holder.getBoundingBox().inflate(BARRIER_RADIUS);
        // 推开范围内所有实体：生物、弹射物、掉落物等。
        // 排除持有者本人、旁观者，以及持有者自己发射的弹射物（如守卫发射的寒冰弹不受自身屏障影响）。
        List<Entity> list = holder.level().getEntities(holder, area,
            e -> e != holder && !e.isSpectator() && !(e instanceof Projectile p && p.getOwner() == holder));
        Vec3 center = holder.position();
        for (Entity e : list) {
            Vec3 away = e.position().subtract(center);
            away = new Vec3(away.x, 0, away.z); // 仅水平方向（避免把目标举上天）
            double horizontal = away.length();
            if (horizontal < 1.0E-4) {
                // 几乎叠在持有者身上，随便给个方向推开
                away = new Vec3(holder.getRandom().nextDouble() - 0.5, 0.0, holder.getRandom().nextDouble() - 0.5);
                horizontal = away.length();
            }
            Vec3 dir = away.normalize();

            // 近身推得更猛烈：离持有者越近，推力越大。
            double falloff = 1.0 - Math.min(1.0, horizontal / BARRIER_RADIUS);
            double push = 1.6 + 3.0 * falloff; // 边缘 1.6 ~ 中心 4.6

            if (e instanceof ServerPlayer player) {
                // 玩家"自己"的位置由客户端移动包上报，服务端设速度、moveTo 都会被客户端盖回/触发反作弊。
                // 只能用服务端权威传送(teleportTo)把它顶回边界外——这是可靠挡住玩家的唯一办法。
                if (horizontal < BARRIER_RADIUS - 0.25) {
                    player.teleportTo(center.x + dir.x * (BARRIER_RADIUS + 0.5),
                        e.getY(), center.z + dir.z * (BARRIER_RADIUS + 0.5));
                }
            } else {
                // 其余实体：覆盖水平速度为强外向推力，竖直方向保留原有速度(不掉落/不悬停)。
                Vec3 vel = e.getDeltaMovement();
                e.setDeltaMovement(dir.x * push, vel.y, dir.z * push);
            }

            // 提示可见粒子（屏障不可见，但推开时给一点冰霜粒子反馈）
            if (holder.level() instanceof ServerLevel serverLevel && holder.getRandom().nextInt(4) == 0) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), 1, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }
}