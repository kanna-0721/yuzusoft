package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.SleepySpiritEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

public class SleepInBedGoal extends Goal {
    private static final int SEARCH_RANGE = 24;
    private static final int SEARCH_INTERVAL = 60;
    private final SleepySpiritEntity spirit;
    @Nullable
    private BlockPos bedHead;
    private long lastSearchTime = -SEARCH_INTERVAL;
    public SleepInBedGoal(SleepySpiritEntity spirit) {
        this.spirit = spirit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.spirit.getWakeCooldown() > 0) {
            return false;
        }
        if (this.spirit.isSleeping()) {
            return false;
        }
        if (this.spirit.getTarget() != null) {
            return false;
        }
        if (!(this.spirit.level() instanceof ServerLevel)) {
            return false;
        }
        if (!this.spirit.level().isNight()) {
            return false;
        }
        if (this.spirit.isDeadOrDying()) {
            return false;
        }
        this.bedHead = this.getOrFindBed();
        return this.bedHead != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.spirit.isDeadOrDying()) {
            return false;
        }
        if (this.spirit.getTarget() != null) {
            return false;
        }
        if (this.spirit.isSleeping()) {
            return true;
        }
        if (this.spirit.getWakeCooldown() > 0) {
            return false;
        }
        if (!this.spirit.level().isNight()) {
            return false;
        }
        if (this.bedHead == null) {
            return false;
        }
        return this.isBedHeadAvailable(this.bedHead);
    }

    @Override
    public void start() {
    }
    @Override
    public void stop() {
        this.bedHead = null;
    }
    @Override
    public void tick() {
        if (this.spirit.isSleeping()) {
            return;
        }
        if (this.bedHead == null) {
            this.bedHead = this.getOrFindBed();
            return;
        }
        if (this.isBedHeadAvailable(this.bedHead)) {
            double distSqr = this.spirit.distanceToSqr(Vec3.atBottomCenterOf(this.bedHead));
            if (distSqr < 2.25) {
                this.spirit.startSleeping(this.bedHead);
                this.spirit.onSleepStarted(this.bedHead);
            } else {
                this.spirit.getNavigation().moveTo(
                        this.bedHead.getX() + 0.5, this.bedHead.getY(), this.bedHead.getZ() + 0.5, 1.0);
                this.spirit.getLookControl().setLookAt(
                        this.bedHead.getX() + 0.5, this.bedHead.getY() + 0.6, this.bedHead.getZ() + 0.5, 10.0f, 20.0f);
            }
        } else {
            this.bedHead = this.getOrFindBed();
        }
    }

    @Nullable
    private BlockPos getOrFindBed() {
        Level level = this.spirit.level();
        long now = level.getGameTime();
        if (this.bedHead != null && now - this.lastSearchTime < SEARCH_INTERVAL) {
            return this.isBedHeadAvailable(this.bedHead) ? this.bedHead : null;
        }
        if (now - this.lastSearchTime < SEARCH_INTERVAL) {
            return this.bedHead;
        }
        this.lastSearchTime = now;
        this.bedHead = this.searchNearbyBed();
        return this.bedHead;
    }

    @Nullable
    private BlockPos searchNearbyBed() {
        Level level = this.spirit.level();
        BlockPos center = this.spirit.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -SEARCH_RANGE; dx <= SEARCH_RANGE; dx++) {
            for (int dz = -SEARCH_RANGE; dz <= SEARCH_RANGE; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockPos head = this.getBedHead(pos);
                    if (head != null && this.isBedHeadAvailable(head)) {
                        double d = center.distSqr(head);
                        if (d < bestDist) {
                            bestDist = d;
                            best = head;
                        }
                    }
                }
            }
        }
        return best;
    }

    @Nullable
    private BlockPos getBedHead(BlockPos pos) {
        Level level = this.spirit.level();
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.BEDS)) {
            return null;
        }
        if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING));
    }

    private boolean isBedHeadAvailable(BlockPos head) {
        Level level = this.spirit.level();
        if (!level.hasChunkAt(head)) {
            return false;
        }
        BlockState state = level.getBlockState(head);
        return state.is(BlockTags.BEDS)
                && state.getValue(BedBlock.PART) == BedPart.HEAD
                && !state.getValue(BedBlock.OCCUPIED);
    }
}