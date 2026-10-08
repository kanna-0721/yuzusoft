package cn.autoforged.yuzusoft.charm.ai;

import cn.autoforged.yuzusoft.charm.CharmHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 魅惑契约的"宠物跟随"目标：跟随主人，太远时传送过去。
 * 参照原版 FollowOwnerGoal，但不可坐下、无需驯服标记，契约结束自动失效。
 */
public class CharmFollowOwnerGoal extends Goal {
    private static final float START_DISTANCE = 6.0F;
    private static final float STOP_DISTANCE = 3.0F;
    private static final int TELEPORT_DISTANCE_SQ = 12 * 12;

    private final Mob mob;
    private final double speedModifier;
    private final PathNavigation navigation;
    @Nullable
    private Player owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public CharmFollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.speedModifier = 1.1;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!CharmHelper.hasCharmEffect(this.mob)) {
            return false;
        }
        Player owner = CharmHelper.resolveOwner(this.mob);
        if (owner == null || owner.isSpectator()) {
            return false;
        }
        // 战斗中不跟随：避免跟随目标每 10 tick 覆盖"走向战斗目标"的导航路径，
        // 导致生物边打边往玩家方向跑、甚至超 12 格被传送回主人身边弃战。
        if (CharmHelper.isAllowedCharmTarget(this.mob, this.mob.getTarget())) {
            return false;
        }
        if (this.mob.distanceToSqr(owner) < (double) (START_DISTANCE * START_DISTANCE)) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        Player owner = this.owner;
        if (owner == null || owner.isRemoved()) {
            return false;
        }
        if (!CharmHelper.hasCharmEffect(this.mob)) {
            return false;
        }
        // 跟随途中转入战斗（如护主/反击）立即让位
        if (CharmHelper.isAllowedCharmTarget(this.mob, this.mob.getTarget())) {
            return false;
        }
        if (this.navigation.isDone() && this.mob.distanceToSqr(owner) > (double) (STOP_DISTANCE * STOP_DISTANCE)) {
            return true; // 太远时 tick 里会传送，不能因寻路结束直接停
        }
        return this.mob.distanceToSqr(owner) > (double) (STOP_DISTANCE * STOP_DISTANCE);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        Player owner = this.owner;
        if (owner == null || owner.isRemoved()) {
            return;
        }
        this.mob.getLookControl().setLookAt(owner, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (this.mob.distanceToSqr(owner) >= (double) TELEPORT_DISTANCE_SQ) {
                teleportToOwner(owner);
            } else {
                this.navigation.moveTo(owner, this.speedModifier);
            }
        }
    }

    private void teleportToOwner(Player owner) {
        if (owner.level().dimension() != this.mob.level().dimension()) {
            return;
        }
        BlockPos ownerPos = owner.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = this.mob.getRandom().nextInt(11) - 5;
            int dz = this.mob.getRandom().nextInt(11) - 5;
            // 在同一水平偏移列上从高到低搜索可站立足点，适应坡度
            for (int dy = 2; dy >= -2; dy--) {
                BlockPos pos = ownerPos.offset(dx, dy, dz);
                if (!isStandable(pos)) {
                    continue;
                }
                if (this.mob.onGround() && this.mob.distanceToSqr(owner) < (double) (8 * 8)) {
                    return; // 主人已足够近，走过去即可，无需传送
                }
                this.mob.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                this.mob.getNavigation().stop();
                return;
            }
        }
    }

    /** 落点必须满足：脚下有实心顶面支撑，且落点与头顶一格均可通行，避免传进墙里。 */
    private boolean isStandable(BlockPos pos) {
        Level level = this.mob.level();
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        BlockState here = level.getBlockState(pos);
        if (!here.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        return above.getCollisionShape(level, pos.above()).isEmpty();
    }
}
