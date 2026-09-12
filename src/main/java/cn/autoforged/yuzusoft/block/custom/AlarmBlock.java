package cn.autoforged.yuzusoft.block.custom;

import cn.autoforged.yuzusoft.entity.custom.SuzuneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 警报器（PVZ 土豆地雷样式）：由 Suzune 钻地时安放在方块正上方。
 * 任何生物（LivingEntity）踩上后点亮引信（PRIMED=true），12 tick 后爆炸（半径 3，BLOCK 交互）。
 *
 * 碰撞箱与蜡烛一样细小（3×3×7 像素），高度 7/16 低于玩家步进高度（0.6），
 * 因此玩家无需跳跃即可直接走上去踩中触发。
 */
public class AlarmBlock extends Block {
    public static final BooleanProperty PRIMED = BooleanProperty.create("primed");
    /** 玩家亲手放置的警报器（友军设施），爆炸不伤害玩家及其宠物；Suzune 安放的不带此标记。 */
    public static final BooleanProperty BY_PLAYER = BooleanProperty.create("by_player");
    public static final int FUSE_TICKS = 12;
    public static final float EXPLOSION_RADIUS = 8.0F;
    private static final VoxelShape SHAPE = box(6.5, 0.0, 6.5, 9.5, 7.0, 9.5);

    public AlarmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(PRIMED, false)
            .setValue(BY_PLAYER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PRIMED, BY_PLAYER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player) {
            level.setBlock(pos, state.setValue(BY_PLAYER, true), 3);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * 经验球：方块被非精准采集工具破坏时掉落经验（附魔系统会自动把精准采集时的经验置 0，
     * 并对时运做加成）；方块本身是否掉落由战利品表（silk_touch 条件）控制。
     */
    @Override
    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos,
                          @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        return 5;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity && !state.getValue(PRIMED)) {
            level.setBlock(pos, state.setValue(PRIMED, true), 3);
            level.scheduleTick(pos, this, FUSE_TICKS);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide) {
            return;
        }
        // 引信期间方块被破坏/替换：取消引爆（防止 Suzune 自爆波及后二次爆炸）
        BlockState current = level.getBlockState(pos);
        if (!current.is(this) || !current.getValue(PRIMED)) {
            return;
        }
        // 1. 地雷引爆：把藏身其内的 Suzune 一并炸死（同归于尽），确保只爆炸一次
        for (SuzuneEntity suzune : level.getEntitiesOfClass(SuzuneEntity.class, new AABB(pos))) {
            if (suzune.isHidden()) {
                suzune.hurt(level.damageSources().explosion(null, null), 1000.0F);
            }
        }
        // 2. 警报器直接消失：先移除方块，避免爆炸把警报器破坏成掉落物留在地面
        level.removeBlock(pos, false);
        // 3. 玩家放置的警报器（友军设施）不伤害玩家及所有宠物
        List<LivingEntity> protect = List.of();
        if (current.getValue(BY_PLAYER)) {
            protect = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(EXPLOSION_RADIUS + 1.0)).stream()
                .filter(e -> e instanceof Player || (e instanceof TamableAnimal t && t.getOwnerUUID() != null))
                .toList();
            for (LivingEntity e : protect) {
                e.setInvulnerable(true);
            }
        }
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            EXPLOSION_RADIUS, Level.ExplosionInteraction.BLOCK);
        for (LivingEntity e : protect) {
            e.setInvulnerable(false);
        }
    }
}
