package cn.autoforged.yuzusoft.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * 洒水苦力怕「矿工形态」的破块逻辑（沿用之前追踪者 TrackerBreakBlockGoal 一期相同的框架）：
 * 1. 检测到目标在自己范围内、自己又连续 N 秒原地无法前进（明显被卡住）时拆墙；
 * 2. 只破坏「自己正前方、迈向目标那一步」的墙（脚部/头部两格），绝不扫描整圈去挑无关方块；
 * 3. 破坏采用「渐进式挖掘」：每 tick 累积与玩家手持下界合金镐相同的破坏进度，到 1.0 才真正破坏方块，
 *    即破坏速率与玩家使用下界合金镐一致（而非瞬间破坏）。
 */
public class MinerBreakBlockGoal extends Goal {
    /** 判定「卡住」的连续不动 tick 数（1 秒 = 20 tick） */
    private static final int STUCK_TICKS = 20;
    /** 拆掉一个方块后的短暂冷却 tick（只在下一次破坏前生效，不拖慢挖掘进度本身） */
    private static final int BREAK_INTERVAL = 4;
    /** 触发范围：与目标距离的平方需在 (4.0, 100.0] 内 */
    private static final double MIN_DIST_SQR = 4.0D;
    private static final double MAX_DIST_SQR = 100.0D;

    private final PathfinderMob mob;

    private BlockPos lastPos;
    private int immobileTicks;
    private int breakCooldown;

    /** 当前正在挖掘的方块与已累积的破坏进度（0~1） */
    @Nullable
    private BlockPos miningPos;
    private float digProgress;

    public MinerBreakBlockGoal(PathfinderMob mob) {
        this.mob = mob;
        // 本目标不主动抢占移动（自身不执行 moveTo），让导航/躲避等 MOVE 目标正常运作
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distSqr = this.mob.distanceToSqr(target);
        return distSqr > MIN_DIST_SQR && distSqr <= MAX_DIST_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.lastPos = this.mob.blockPosition();
        this.immobileTicks = 0;
        this.breakCooldown = 0;
        this.miningPos = null;
        this.digProgress = 0.0F;
    }

    @Override
    public void stop() {
        this.miningPos = null;
        this.digProgress = 0.0F;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        // 1. 跟踪是否还在移动
        BlockPos pos = this.mob.blockPosition();
        if (!pos.equals(this.lastPos)) {
            this.lastPos = pos;
            this.immobileTicks = 0;
        } else {
            this.immobileTicks++;
        }

        // 2. 没卡够时间就继续正常导航接近目标
        if (this.immobileTicks < STUCK_TICKS) {
            this.miningPos = null;
            this.digProgress = 0.0F;
            return;
        }

        // 3. 连续卡住（即便仍在尝试寻路也原地不动）→ 挖掘
        // 刚拆完一个方块后的短暂冷却：跳过本次挖掘，但不影响下一次正常累计进度
        if (this.breakCooldown > 0) {
            this.breakCooldown--;
            return;
        }

        BlockPos toMine = this.findBlockToBreak(target);
        if (toMine == null) {
            this.miningPos = null;
            this.digProgress = 0.0F;
            return;
        }

        Level level = this.mob.level();
        // 目标方块切换后重置进度
        if (!toMine.equals(this.miningPos)) {
            this.miningPos = toMine;
            this.digProgress = 0.0F;
        }
        // 每 tick 都按「玩家手持下界合金镐」的速率累计破坏进度
        BlockState state = level.getBlockState(toMine);
        this.digProgress += this.miningProgressPerTick(level, toMine, state);
        if (this.digProgress >= 1.0F) {
            level.destroyBlock(toMine, true, this.mob, 0);
            this.miningPos = null;
            this.digProgress = 0.0F;
            this.breakCooldown = BREAK_INTERVAL;
        }
    }

    /**
     * 只挖掘「自己迈向目标那一步」的墙（脚部/头部两格）。
     * 前面是黑曜石/基岩等不可拆方块时则什么都不拆（会停在那里），绝不挖脚边或旁边的地形。
     */
    @Nullable
    private BlockPos findBlockToBreak(LivingEntity target) {
        Level level = this.mob.level();
        BlockPos mobPos = this.mob.blockPosition();

        int stepX = Integer.signum(target.getBlockX() - mobPos.getX());
        int stepZ = Integer.signum(target.getBlockZ() - mobPos.getZ());
        if (stepX == 0 && stepZ == 0) {
            return null;
        }

        // 先看正前方一步、脚部那一格（两格高怪物被挡住通常是这里）
        BlockPos frontFoot = new BlockPos(mobPos.getX() + stepX, mobPos.getY(), mobPos.getZ() + stepZ);
        if (this.isBreakable(level, frontFoot)) {
            return frontFoot;
        }
        // 脚部可通行、但头部被挡（两格高的墙）→ 拆上面的
        BlockPos frontHead = frontFoot.above();
        if (this.isBreakable(level, frontHead)) {
            return frontHead;
        }
        return null;
    }

    private boolean isBreakable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getFluidState().isSource()) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) == -1.0F) {
            return false;
        }
        if (state.getBlock().getExplosionResistance() >= 1000.0F) {
            return false;
        }
        return CommonHooks.canEntityDestroy(level, pos, this.mob);
    }

    /**
     * 每 tick 的破坏进度，等价于玩家手持下界合金镐的破坏速率：
     * 进度 = 下界合金镐挖掘速度 / 方块硬度 / (30 或 100)。
     */
    private float miningProgressPerTick(Level level, BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness == -1.0F) {
            return 0.0F;
        }
        ItemStack netheritePickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        // 下界合金镐的挖掘速度：对镐类适用的方块返回镐本身的挖掘速度（tier 9.0），否则返回 1.0。
        float digSpeed = netheritePickaxe.getDestroySpeed(state);
        // 采用原版破坏速率公式的「正确工具」分母 30（下界合金镐本就是该类怪的正手工具）,
        // 使矿工破坏速率与玩家手持下界合金镐一致。
        return digSpeed / hardness / 30.0F;
    }
}