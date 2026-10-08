package cn.autoforged.yuzusoft.util;

/**
 * 音高换算。公式沿用 PianoCraft 的 NoteUtil：
 * <pre>pitch = 2^((note - 6) / 12) * 2^(octave - 4)</pre>
 * 其中 note = id % 12（0 = C），octave = 2 + id / 12，故 id 从 0（C2）到 60（C7）。
 */
public final class NoteUtil {

    /** 整个键盘的半音跨度：61 键，id 0..60。 */
    public static final int KEY_COUNT = 61;

    private NoteUtil() {
    }

    public static int noteOf(int id) {
        return Math.floorMod(id, 12);
    }

    public static int octaveOf(int id) {
        return 2 + Math.floorDiv(id, 12);
    }

    /** 半音编号 -> 音高倍率（0.1768 ~ 5.6569）。 */
    public static float pitchFromId(int id) {
        return (float) (Math.pow(2.0D, (noteOf(id) - 6) / 12.0D) * Math.pow(2.0D, octaveOf(id) - 4));
    }
}