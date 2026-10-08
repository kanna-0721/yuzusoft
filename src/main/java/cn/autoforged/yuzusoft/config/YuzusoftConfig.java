package cn.autoforged.yuzusoft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Yuzusoft 总配置（SERVER 型）。
 * 目前只有一个选项：是否在自然生成中刷 yuzusoft 怪物。
 */
public class YuzusoftConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_NATURAL_SPAWNS = BUILDER
            .comment("是否在自然生成中刷出 yuzusoft 模组的生物（新建世界时生效）。",
                    "关闭后：自然刷怪、结构/巡逻等生成 yuzusoft 生物都会被屏蔽；",
                    "刷怪蛋与 /summon 指令不受影响。",
                    "默认：true")
            .define("enableNaturalSpawns", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
