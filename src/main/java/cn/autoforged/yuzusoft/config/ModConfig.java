package cn.autoforged.yuzusoft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置（CLIENT）。
 * 目前仅含白雪乃爱骑乘飞行的下降键；NeoForge 内置 ConfigurationScreen 会自动为这些配置项生成编辑界面。
 */
public class ModConfig {
    public static final ModConfigSpec SPEC;

    /** 下降键（InputConstants 键名，如 key.keyboard.n）。修改后重启游戏生效。 */
    public static final ModConfigSpec.ConfigValue<String> DUALFORM_DESCEND_KEY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("骑乘飞行控制键位").push("controls");
        DUALFORM_DESCEND_KEY = builder
            .comment("白雪乃爱骑乘飞行下降键。填写 InputConstants 键名，如 key.keyboard.n / key.keyboard.x。修改后重启生效。")
            .define("dualformDescendKey", "key.keyboard.n");
        builder.pop();
        SPEC = builder.build();
    }
}
