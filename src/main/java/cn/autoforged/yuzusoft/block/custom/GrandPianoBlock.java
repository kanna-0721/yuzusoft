package cn.autoforged.yuzusoft.block.custom;

import com.mojang.serialization.MapCodec;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.util.NoteUtil;
import cn.autoforged.yuzusoft.util.PianoLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 7 段横向多结构的三角钢琴本体。
 *
 * <p>每一段只保存 {@link #PART}（0..6）与 {@link #FACING}，由此可反推锚点与其他 6 段的位置。
 * 空手（或手持非方块）右键琴键发声；踩在琴键上也会按脚下位置发声。</p>
 *
 * <p>放好的钢琴还会持续输出琴声领域：以中间段为中心，每 2 秒对 15x15x15 方块内所有
 * {@code MONSTER} 类别的生物造成 {@value #STRIKE_DAMAGE} 点伤害（无视护甲）。计时只挂在中间段，
 * 见 {@link #onPlace} 与 {@link #tick}。</p>
 */
public class GrandPianoBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<GrandPianoBlock> CODEC = simpleCodec(GrandPianoBlock::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, PianoLayout.SEGMENTS - 1);

    /** 放置时点击的位置落在中间段。 */
    private static final int MID = PianoLayout.SEGMENTS / 2;

    /** 静默放置/静默拆除：只同步客户端 + 抑制邻居形状更新，避免触发自身完整性自检。 */
    private static final int SILENT = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /** 琴声领域：15x15x15 => 以中间段为心、切比雪夫距离 ±7。 */
    private static final int STRIKE_RADIUS = 7;
    private static final int STRIKE_INTERVAL_TICKS = 40;
    private static final float STRIKE_DAMAGE = 2.0F;

    /** 琴声领域附带的眩晕时长：1 秒。 */
    private static final int STUN_TICKS = 20;

    /** 规范空间（未旋转）的碰撞/轮廓盒：琴身 + 键盘台。 */
    private static final AABB[] CANON_BOXES = {
            new AABB(0.0D, 0.0D, 9.0D, 16.0D, 25.0D, 16.0D),
            new AABB(0.0D, 0.0D, 1.0D, 16.0D, 14.0D, 9.0D)
    };

    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public GrandPianoBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, MID));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    // ----- 形状 -----

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            for (AABB box : CANON_BOXES) {
                shape = Shapes.or(shape, Shapes.create(rotate(box, facing)));
            }
            map.put(facing, shape);
        }
        return map;
    }

    /** 绕方块中心 (8, ·, 8) 旋转规范盒，使其与 blockstate 的 y 旋转一致。 */
    private static AABB rotate(AABB box, Direction facing) {
        double u0 = box.minX - 8.0D;
        double u1 = box.maxX - 8.0D;
        double v0 = box.minZ - 8.0D;
        double v1 = box.maxZ - 8.0D;
        double dx0;
        double dx1;
        double dz0;
        double dz1;
        switch (facing) {
            case EAST -> {
                dx0 = -v1;
                dx1 = -v0;
                dz0 = u0;
                dz1 = u1;
            }
            case SOUTH -> {
                dx0 = -u1;
                dx1 = -u0;
                dz0 = -v1;
                dz1 = -v0;
            }
            case WEST -> {
                dx0 = v0;
                dx1 = v1;
                dz0 = -u1;
                dz1 = -u0;
            }
            default -> {
                dx0 = u0;
                dx1 = u1;
                dz0 = v0;
                dz1 = v1;
            }
        }
        return new AABB(
                (8.0D + dx0) / 16.0D, box.minY / 16.0D, (8.0D + dz0) / 16.0D,
                (8.0D + dx1) / 16.0D, box.maxY / 16.0D, (8.0D + dz1) / 16.0D);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    // ----- 放置 / 拆除 -----

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos anchor = context.getClickedPos().subtract(PianoLayout.segmentOffset(facing, MID));
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            BlockPos p = anchor.offset(PianoLayout.segmentOffset(facing, i));
            if (!level.isInWorldBounds(p) || !level.getBlockState(p).canBeReplaced()) {
                return null;
            }
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, MID);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos anchor = anchorOf(pos, state);
        BlockState base = defaultBlockState();
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            BlockPos p = anchor.offset(PianoLayout.segmentOffset(facing, i));
            if (!p.equals(pos)) {
                level.setBlock(p, base.setValue(FACING, facing).setValue(PART, i), SILENT);
            }
        }
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            level.updateNeighborsAt(anchor.offset(PianoLayout.segmentOffset(facing, i)), this);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            clearOthers(level, pos, state);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && !isIntact(level, pos, state)) {
            removeAll(level, pos, state);
            return;
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            removeAll(level, pos, level.getBlockState(pos));
        }
    }

    private static BlockPos anchorOf(BlockPos pos, BlockState state) {
        return pos.subtract(PianoLayout.segmentOffset(state.getValue(FACING), state.getValue(PART)));
    }

    private boolean isIntact(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos anchor = anchorOf(pos, state);
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            BlockState s = level.getBlockState(anchor.offset(PianoLayout.segmentOffset(facing, i)));
            if (s.getBlock() != this || s.getValue(PART) != i || s.getValue(FACING) != facing) {
                return false;
            }
        }
        return true;
    }

    /** 清掉其余 6 段，保留 pos（交由正常破坏流程移除并掉落）。 */
    private void clearOthers(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos anchor = anchorOf(pos, state);
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            BlockPos p = anchor.offset(PianoLayout.segmentOffset(facing, i));
            if (!p.equals(pos) && level.getBlockState(p).getBlock() == this) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), SILENT);
            }
        }
    }

    private void removeAll(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() != this) {
            return;
        }
        Direction facing = state.getValue(FACING);
        BlockPos anchor = anchorOf(pos, state);
        for (int i = 0; i < PianoLayout.SEGMENTS; i++) {
            BlockPos p = anchor.offset(PianoLayout.segmentOffset(facing, i));
            if (level.getBlockState(p).getBlock() == this) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), SILENT);
            }
        }
    }

    // ----- 交互：右键弹键 -----

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof BlockItem) {
            // 手持方块时不发声、也不允许把方块放到钢琴上。
            // 必须返回 CONSUME（consumesAction），返回 FAIL 会继续走 ItemStack#useOn 而放下方块。
            return ItemInteractionResult.CONSUME;
        }
        playNote(level, pos, state, hit.getLocation(), 3.0F);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        playNote(level, pos, state, hit.getLocation(), 3.0F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ----- 交互：踩在琴键上 -----

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide || !(entity instanceof Player player) || player.isSpectator()) {
            return;
        }
        // walkDist 每 tick 累积水平位移；整数值变化即约走过一格，借此做步频节流
        if ((int) entity.walkDist == (int) entity.walkDistO) {
            return;
        }
        playNote(level, pos, state, new Vec3(entity.getX(), entity.getY(), entity.getZ()), 1.0F);
    }

    // ----- 琴声领域：持续伤害怪物 -----

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 只有中间段当发条，7 段一起跑会把伤害叠成 7 倍
        if (!level.isClientSide && state.getValue(PART) == MID) {
            level.scheduleTick(pos, this, STRIKE_INTERVAL_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != MID) {
            return;
        }
        strikeMonsters(level, pos);
        level.scheduleTick(pos, this, STRIKE_INTERVAL_TICKS);
    }

    private void strikeMonsters(ServerLevel level, BlockPos center) {
        AABB area = new AABB(center).inflate(STRIKE_RADIUS);
        // 按类别筛而不是 instanceof Monster，史莱姆/幻翼这类不是 Monster 子类的敌对生物也要吃到
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                candidate -> candidate.isAlive() && candidate.getType().getCategory() == MobCategory.MONSTER)) {
            if (chebyshev(center, entity.blockPosition()) <= STRIKE_RADIUS) {
                entity.hurt(level.damageSources().magic(), STRIKE_DAMAGE);
                // 琴声领域附带 1 秒眩晕
                entity.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_TICKS, 0));
            }
        }
    }

    private static int chebyshev(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    private void playNote(Level level, BlockPos pos, BlockState state, Vec3 at, float volume) {
        if (level.isClientSide) {
            return;
        }
        // PianoLayout 用的是每段 0..16 像素的布局空间，命中点相对方块最小角只是 0..1 的分数
        double fx = (at.x - pos.getX()) * PianoLayout.SEGMENT_PX;
        double fz = (at.z - pos.getZ()) * PianoLayout.SEGMENT_PX;
        int id = PianoLayout.keyAtWorld(state.getValue(FACING), state.getValue(PART), fx, fz);
        if (id < 0) {
            return;
        }
        level.playSound(null, at.x, at.y, at.z, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.RECORDS,
                volume, NoteUtil.pitchFromId(id));
    }
}