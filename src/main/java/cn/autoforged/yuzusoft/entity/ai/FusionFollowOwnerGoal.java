package cn.autoforged.yuzusoft.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 融合生物的跟随主人：照抄 {@code TamableAnimal} 的跟随行为——
 * 离主人超过 10 格开始跟随，2 格内停下，超过 16 格直接传送到主人身边。
 * <p>
 * 坐下状态用 {@code setNoAi(true)} 表示（融合生物没有 {@code isOrderedToSit}），
 * 而关 AI 后整个 goal 选择器都不跑，本 goal 自然也不会启动。
 */
public class FusionFollowOwnerGoal extends Goal {
    private static final double START_DISTANCE_SQR = 100.0D;    // 10 格
    private static final double STOP_DISTANCE_SQR = 4.0D;       // 2 格
    private static final double TELEPORT_DISTANCE_SQR = 256.0D; // 16 格

    private final Mob mob;
    @Nullable
    private LivingEntity owner;

    public FusionFollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity found = FusionTameHelper.ownerOf(this.mob);
        if (found == null || found.isSpectator()) {
            return false;
        }
        if (this.mob.distanceToSqr(found) < START_DISTANCE_SQR) {
            return false;
        }
        this.owner = found;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && this.owner.isAlive()
                && this.mob.distanceToSqr(this.owner) > STOP_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.owner, 1.0D);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.owner) > TELEPORT_DISTANCE_SQR) {
            this.mob.getNavigation().stop();
            this.mob.moveTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                    this.mob.getYRot(), this.mob.getXRot());
        } else {
            this.mob.getNavigation().moveTo(this.owner, 1.0D);
        }
    }
}