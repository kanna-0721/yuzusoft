package cn.autoforged.yuzusoft.charm.ai;

import cn.autoforged.yuzusoft.charm.CharmHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 契约避障目标：防止魅惑生物主动走入火/岩浆/岩浆块等危险区域。
 *
 * 香草寻路把火方块当作"可通行"（非实心），所以任何副本生物追目标时都可能在经过处踩火；
 * 而魅惑是附着在任意现有生物身上的（事件注入），无法逐一切换它们的 Navigation / MoveControl，
 * 因此用一个目标在"前方即将踩到危险块"时把生物拉向远离危险的安全落点。
 *
 * 规则：
 * - 仅在已魅惑时生效；未魅惑零开销。
 * - 扫描生物周 4 格内的危险方块：岩浆流体（源/流淌）、火、灵魂火、岩浆块。
 * - 落点要求：非危险、脚下有实心支撑、身位与头顶各 1 格为空气。
 * - 打分偏向"离危险最远兼顾不跑太远"；若生物已浸入危险，则优先向上爬一层再水平逃离。
 * - 只对地面生物（GroundPathNavigation）真正移动；飞行/无寻路生物在 canUse 通过但不动，
 *   多为岩浆/火焰免疫的副本生物，无碍。
 */
public class CharmAvoidHazardGoal extends Goal {
    /** 危险扫描与逃生半径（格）。 */
    private static final int R = 4;

    private final Mob mob;
    /** 当前最近的危险方块，canUse 只在找到危险时置。 */
    @Nullable
    private BlockPos hazard;

    public CharmAvoidHazardGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!CharmHelper.isCharmActive(this.mob)) {
            return false;
        }
        this.hazard = this.findNearestHazard();
        return this.hazard != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!CharmHelper.isCharmActive(this.mob)) {
            return false;
        }
        // 危险仍在才继续；否则让位于战斗/漫游等原版目标
        this.hazard = this.findNearestHazard();
        return this.hazard != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockPos danger = this.hazard;
        if (danger == null || !(this.mob.getNavigation() instanceof GroundPathNavigation nav)) {
            return;
        }
        BlockPos safe = this.findEscapeSpot(danger);
        if (safe != null) {
            nav.moveTo(safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, 1.0D);
        } else if (isStandingInHazard()) {
            // 已浸入危险且无安全平落点：向上强力起跳，尝试爬出流体内
            nav.moveTo(this.mob.getX(), this.mob.getBoundingBox().maxY + 1.0D, this.mob.getZ(), 1.0D);
        }
    }

    /** 生物当前脚下是否已在危险中。 */
    private boolean isStandingInHazard() {
        return isHazard(this.mob.level(), this.mob.blockPosition());
    }

    /** 找生物周围最近的危险方块，没有返回 null。 */
    @Nullable
    private BlockPos findNearestHazard() {
        Level level = this.mob.level();
        BlockPos foot = this.mob.blockPosition();
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (int dx = -R; dx <= R; dx++) {
            for (int dy = -R; dy <= R; dy++) {
                for (int dz = -R; dz <= R; dz++) {
                    BlockPos p = foot.offset(dx, dy, dz);
                    if (!isHazard(level, p)) {
                        continue;
                    }
                    double d = foot.distSqr(p);
                    if (d < best) {
                        best = d;
                        nearest = p;
                    }
                }
            }
        }
        return nearest;
    }

    /** 找一个远离危险方块、可安全站立落点的坐标；找不到返回 null。 */
    @Nullable
    private BlockPos findEscapeSpot(BlockPos danger) {
        Level level = this.mob.level();
        BlockPos foot = this.mob.blockPosition();
        // 若已站在危险里，先尝试爬到更高的安全层；拳头层优先平逃
        int[] dyOrder = isStandingInHazard() ? new int[] {2, 1, 0} : new int[] {0};
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int dy : dyOrder) {
            for (int dx = -R; dx <= R; dx++) {
                for (int dz = -R; dz <= R; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    if (dx * dx + dz * dz > R * R) {
                        continue;
                    }
                    BlockPos cand = foot.offset(dx, dy, dz);
                    if (!candidateWalkable(level, cand)) {
                        continue;
                    }
                    double away = danger.distSqr(cand);   // 离危险越远越好
                    double near = foot.distSqr(cand);     // 也别跑太远
                    double score = away - near * 0.25D - dy * 60.0D; // 爬升有代价，仅不得已才用
                    if (score > bestScore) {
                        bestScore = score;
                        best = cand;
                    }
                }
            }
            if (best != null) {
                return best; // 当前层有解就不再升层
            }
        }
        return best;
    }

    /** 候选落点必须可站且不处于/紧贴危险。 */
    private static boolean candidateWalkable(Level level, BlockPos stand) {
        if (isHazard(level, stand) || isHazard(level, stand.below())) {
            return false;
        }
        return isStandable(level, stand);
    }

    /** 判定某格可站立：脚下实心支撑，身位与头顶 1 格都是空气。 */
    private static boolean isStandable(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isSolid()) {
            return false;
        }
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        return true;
    }

    /** 危险块白名单：岩浆流体（源/流淌）、火/灵魂火、岩浆块。 */
    private static boolean isHazard(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return true;
        }
        Block block = state.getBlock();
        return state.is(BlockTags.FIRE) || block == Blocks.FIRE || block == Blocks.SOUL_FIRE
                || block == Blocks.MAGMA_BLOCK;
    }
}