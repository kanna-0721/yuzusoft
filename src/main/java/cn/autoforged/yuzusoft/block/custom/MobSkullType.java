package cn.autoforged.yuzusoft.block.custom;

import net.minecraft.world.level.block.SkullBlock;

/**
 * yuzusoft 全部生物头颅的自定义骷髅类型。
 * <p>
 * 每种类型对应一个「头颅方块 + 头颅物品」，命名规则统一为 {@code <id>_head}
 * （如 {@code murasame_head}），实体贴图为 {@code textures/entity/<id>_head.png}。
 * 与原版 {@link SkullBlock.Types} 不同，本枚举不注册进 {@code SkullBlock.Type.TYPES} 映射——
 * {@link MobHeadBlock} 使用自带 "kind" 字段的 MapCodec 序列化，不依赖原版映射表。
 */
public enum MobSkullType implements SkullBlock.Type {
    MURASAME("murasame"),
    TOMOTAKE_YOSHINO("tomotake_yoshino"),
    HITACHI_MAKO("hitachi_mako"),
    NENE("nene"),
    INABA_MEGURU("inaba_meguru"),
    SHIKIBE_MAYU("shikibe_mayu"),
    MITSUKASA_AYASE("mitsukasa_ayase"),
    ARIHARA_NANAMI("arihara_nanami"),
    AKIZUKI_KANNA("akizuki_kanna"),
    HARUMI_ENA("harumi_ena"),
    FUTAMIHARA_RIRIKO("futamihara_ririko"),
    NABARI_ANJU("nabari_anju"),
    SHIIBA_TSUMUGI("shiiba_tsumugi"),
    YARAI_MIU("yarai_miu"),
    MIKADO_TAKANORI("mikado_takanori"),
    SHIRAYUKI_NOA("shirayuki_noa"),
    TANIKAZE_AMANE("tanikaze_amane"),
    SHIOYAMA_SUZUNE("shioyama_suzune"),
    KOHIBARI_KURUMI("kohibari_kurumi"),
    EVIL_NANAMI("evil_nanami"),
    NIJOUIN_HAZUKI("nijouin_hazuki"),
    SHIMAGOE_TSUKUMI("shimagoe_tsukumi");

    private final String id;

    MobSkullType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    /** 头颅方块 / 物品的注册名（如 {@code nene_head}）。 */
    public String getHeadName() {
        return this.id + "_head";
    }

    /** 实体贴图路径（相对 {@code assets/yuzusoft/}，如 {@code textures/entity/nene_head.png}）。 */
    public String getTexturePath() {
        return "textures/entity/" + this.id + "_head.png";
    }
}
