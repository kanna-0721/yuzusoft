package cn.autoforged.yuzusoft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * 61 键（C2~C7）键盘的几何布局与「世界坐标 <-> 键位」换算。
 *
 * <p>布局空间（像素）：长轴 lx 从 0 到 112（7 段 × 16px），深度 lz 从 0 到 16。
 * 白键宽 3px、黑键宽 2px；白键 i 占 <code>[2 + 3i, 5 + 3i)</code>，
 * 其右侧黑键（若存在）占 <code>[4 + 3i, 6 + 3i)</code>，深 <code>lz ∈ [4, 8)</code>。</p>
 *
 * <p>模型局部 +x 方向对站在琴前的玩家而言是「左手边」，因此换算时对长轴做镜像，
 * 使音高沿玩家视线自左向右递增。</p>
 */
public final class PianoLayout {

    /** 多结构段数（格）。 */
    public static final int SEGMENTS = 7;
    public static final int SEGMENT_PX = 16;
    public static final int TOTAL_PX = SEGMENTS * SEGMENT_PX;

    /** 白键数量：5 个八度 + 收尾的 C，共 36 个。 */
    public static final int WHITE_KEYS = 36;
    public static final int WHITE_W = 3;
    public static final int WHITE_X0 = 2;
    public static final int BLACK_W = 2;
    /** 白键可用深度。 */
    public static final int KEY_Z0 = 2;
    public static final int KEY_Z1 = 8;
    /** 黑键只占键盘后半段。 */
    public static final int BLACK_Z0 = 4;

    private static final int[] WHITE_SEMITONE = {0, 2, 4, 5, 7, 9, 11};

    private PianoLayout() {
    }

    /** 白键 i 对应的半音编号（0 = C2，60 = C7）。 */
    public static int semitoneOfWhite(int i) {
        return 12 * (i / 7) + WHITE_SEMITONE[i % 7];
    }

    /** 白键 i 右侧是否还有黑键（即右侧是否还有下一个白键）。 */
    public static boolean hasSharpAfter(int i) {
        if (i < 0 || i >= WHITE_KEYS - 1) {
            return false;
        }
        int m = i % 7;
        return m == 0 || m == 1 || m == 3 || m == 4 || m == 5;
    }

    /** 白键 i 右侧黑键的左边界（布局空间像素）。 */
    public static int blackKeyX(int i) {
        return WHITE_X0 + WHITE_W * i + 2;
    }

    /** 布局空间坐标（像素）-> 半音 id；未命中任何键返回 -1。 */
    public static int keyAt(double lx, double lz) {
        if (lz >= BLACK_Z0 && lz < KEY_Z1) {
            int i = (int) Math.floor((lx - (WHITE_X0 + 2)) / (double) WHITE_W);
            if (hasSharpAfter(i)) {
                double bx = blackKeyX(i);
                if (lx >= bx && lx < bx + BLACK_W) {
                    return semitoneOfWhite(i) + 1;
                }
            }
        }
        if (lz >= KEY_Z0 && lz < KEY_Z1) {
            int i = (int) Math.floor((lx - WHITE_X0) / (double) WHITE_W);
            i = Math.max(0, Math.min(WHITE_KEYS - 1, i));
            return semitoneOfWhite(i);
        }
        return -1;
    }

    /** 朝向 -> blockstate 的模型 y 旋转值。 */
    public static int modelYRot(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    /** 第 segment 段相对锚点（第 SEGMENTS/2 段）的方块偏移。 */
    public static BlockPos segmentOffset(Direction facing, int segment) {
        return switch (facing) {
            case EAST -> new BlockPos(0, 0, segment);
            case SOUTH -> new BlockPos(-segment, 0, 0);
            case WEST -> new BlockPos(0, 0, -segment);
            default -> new BlockPos(segment, 0, 0);
        };
    }

    /**
     * 段内分数坐标 -> 布局空间坐标（像素）。
     *
     * @param segment 该方块所属的段序号（来自 PART）
     * @param fx      命中点相对该方块最小角的 X 分数（0..16）
     * @param fz      命中点相对该方块最小角的 Z 分数（0..16）
     */
    public static double[] toLayoutSpace(Direction facing, int segment, double fx, double fz) {
        double mx;
        double mz;
        switch (facing) {
            case EAST -> {
                mx = fz;
                mz = SEGMENT_PX - fx;
            }
            case SOUTH -> {
                mx = SEGMENT_PX - fx;
                mz = SEGMENT_PX - fz;
            }
            case WEST -> {
                mx = SEGMENT_PX - fz;
                mz = fx;
            }
            default -> {
                mx = fx;
                mz = fz;
            }
        }
        double lx = (double) SEGMENT_PX * (SEGMENTS - 1 - segment) + (SEGMENT_PX - mx);
        return new double[]{lx, mz};
    }

    /** 段内分数坐标 -> 半音 id；未命中任何键返回 -1。 */
    public static int keyAtWorld(Direction facing, int segment, double fx, double fz) {
        double[] p = toLayoutSpace(facing, segment, fx, fz);
        return keyAt(p[0], p[1]);
    }
}