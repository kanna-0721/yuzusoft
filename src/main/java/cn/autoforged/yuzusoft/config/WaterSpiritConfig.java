package cn.autoforged.yuzusoft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 水灵生物玩法数值配置（SERVER，并入自 J 工程）。
 * 全部数值可在 configs/yuzusoft-server.toml 中调整。
 */
public final class WaterSpiritConfig {
    private WaterSpiritConfig() {
    }

    public static final ModConfigSpec SPEC;

    // 水位
    public static final ModConfigSpec.DoubleValue MAX_WATER_LEVEL;
    public static final ModConfigSpec.DoubleValue BUCKET_COST;
    public static final ModConfigSpec.DoubleValue BOTTLE_COST;
    // 吸收
    public static final ModConfigSpec.IntValue ABSORB_IDLE_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue ABSORB_ANGRY_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue ABSORB_RANGE_HORIZONTAL;
    public static final ModConfigSpec.IntValue ABSORB_RANGE_VERTICAL;
    public static final ModConfigSpec.DoubleValue ABSORB_GAIN;
    // 愤怒与战斗
    public static final ModConfigSpec.IntValue ANGER_DURATION_SECONDS;
    public static final ModConfigSpec.DoubleValue SHOT_INTERVAL_SECONDS;
    public static final ModConfigSpec.DoubleValue SHOT_WATER_COST;
    public static final ModConfigSpec.DoubleValue SHOT_DAMAGE;
    public static final ModConfigSpec.DoubleValue SHOT_KNOCKBACK;
    public static final ModConfigSpec.DoubleValue SHOT_MAX_RANGE;
    public static final ModConfigSpec.BooleanValue MELEE_WHEN_LOW_WATER;
    // 反弹
    public static final ModConfigSpec.BooleanValue REFLECT_PROJECTILES;
    public static final ModConfigSpec.DoubleValue REFLECT_RANGE;
    public static final ModConfigSpec.DoubleValue REFLECT_MIN_DOT;
    // 雨伞
    public static final ModConfigSpec.DoubleValue UMBRELLA_DROP_CHANCE;
    public static final ModConfigSpec.IntValue UMBRELLA_SHOT_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue UMBRELLA_REFLECT_DURABILITY_COST;
    // 显示
    public static final ModConfigSpec.BooleanValue SHOW_WATER_BAR;
    public static final ModConfigSpec.BooleanValue SHOW_WATER_NUMBER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("水位基础设置 / water level").push("water");
        MAX_WATER_LEVEL = b.comment("水灵的最大水位格数（水位条固定显示 5 格，按比例填充）").defineInRange("maxWaterLevel", 5.0D, 1.0D, 64.0D);
        BUCKET_COST = b.comment("玩家用空桶取水时扣除的水位").defineInRange("bucketCost", 1.0D, 0.0D, 64.0D);
        BOTTLE_COST = b.comment("玩家用玻璃瓶取水时扣除的水位（水位归 0 才禁瓶；不足一次用量时按剩余扣光）").defineInRange("bottleCost", 0.1D, 0.0D, 64.0D);
        b.pop();

        b.comment("水源吸收 / water source absorption").push("absorb");
        ABSORB_IDLE_COOLDOWN_SECONDS = b.comment("空闲时每多少秒扫描一次水源").defineInRange("idleCooldownSeconds", 60, 1, 3600);
        ABSORB_ANGRY_COOLDOWN_SECONDS = b.comment("愤怒时扫描水源的冷却（秒）").defineInRange("angryCooldownSeconds", 30, 1, 3600);
        ABSORB_RANGE_HORIZONTAL = b.comment("水平扫描半径（12 -> 25 格宽）").defineInRange("rangeHorizontal", 12, 1, 32);
        ABSORB_RANGE_VERTICAL = b.comment("垂直扫描半径（2 -> 5 格高）").defineInRange("rangeVertical", 2, 0, 16);
        ABSORB_GAIN = b.comment("每次吸收恢复的水位").defineInRange("gain", 1.0D, 0.1D, 16.0D);
        b.pop();

        b.comment("愤怒与战斗 / anger and combat").push("combat");
        ANGER_DURATION_SECONDS = b.comment("受击或取水受阻后的愤怒持续时间（秒）").defineInRange("angerDurationSeconds", 10, 1, 600);
        SHOT_INTERVAL_SECONDS = b.comment("发射水弹的间隔（秒）").defineInRange("shotIntervalSeconds", 1.5D, 0.1D, 10.0D);
        SHOT_WATER_COST = b.comment("每发水弹消耗的水位；水位低于该值时停射并改用近战").defineInRange("shotWaterCost", 0.2D, 0.0D, 5.0D);
        SHOT_DAMAGE = b.comment("水弹命中伤害（点）").defineInRange("shotDamage", 6.0D, 0.0D, 20.0D);
        SHOT_KNOCKBACK = b.comment("水弹命中击退强度").defineInRange("shotKnockback", 0.6D, 0.0D, 5.0D);
        SHOT_MAX_RANGE = b.comment("水弹锁敌射程（格）").defineInRange("shotMaxRange", 16.0D, 2.0D, 48.0D);
        MELEE_WHEN_LOW_WATER = b.comment("水位低于 0.2 时改为近战攻击（近战伤害为实体属性 3.0）").define("meleeWhenLowWater", true);
        b.pop();

        b.comment("弹射物反弹 / projectile reflection").push("reflect");
        REFLECT_PROJECTILES = b.comment("水灵是否自动反弹来袭弹射物").define("spiritReflect", true);
        REFLECT_RANGE = b.comment("反弹判定半径（格），约等于盾牌范围").defineInRange("reflectRange", 2.5D, 0.5D, 8.0D);
        REFLECT_MIN_DOT = b.comment("来袭方向与视线夹角的余弦门限（越大越要求正面）").defineInRange("reflectMinDot", 0.3D, -1.0D, 1.0D);
        b.pop();

        b.comment("雨伞 / umbrella").push("umbrella");
        UMBRELLA_DROP_CHANCE = b.comment("水灵死亡掉落雨伞的概率").defineInRange("dropChance", 0.2D, 0.0D, 1.0D);
        UMBRELLA_SHOT_COOLDOWN_TICKS = b.comment("主手右键发射水弹的冷却（tick）").defineInRange("shotCooldownTicks", 20, 0, 200);
        UMBRELLA_REFLECT_DURABILITY_COST = b.comment("副手反弹每挡一发弹射物消耗的耐久").defineInRange("reflectDurabilityCost", 1, 0, 10);
        b.pop();

        b.comment("显示 / display").push("display");
        SHOW_WATER_BAR = b.comment("是否在水灵头顶显示 5 格水位条").define("showWaterBar", true);
        SHOW_WATER_NUMBER = b.comment("水位条后是否附带数值").define("showWaterNumber", true);
        b.pop();

        SPEC = b.build();
    }

    public static int secondsToTicks(double seconds) {
        return Math.max(1, (int) Math.round(seconds * 20.0D));
    }

    public static float maxWater() {
        return MAX_WATER_LEVEL.get().floatValue();
    }

    public static boolean hasEnoughWater(float current, double cost) {
        return (double) current + 1.0E-4D >= cost;
    }
}
