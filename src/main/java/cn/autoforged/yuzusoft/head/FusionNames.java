package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;

/**
 * 融合生物的名称规则：<b>身体在前，头在后</b>。
 * <ul>
 *   <li>原版身体 + 原版头 → 两个原版名相接（僵尸 + 苦力怕 = 僵尸苦力怕）；</li>
 *   <li>原版身体 + 柚子头 → 原版名 + 角色名（僵尸 + 宁宁 = 僵尸宁宁）；</li>
 *   <li>柚子身体 + 原版头 → 角色姓 + 原版名（绫地 + 僵尸 = 绫地僵尸）；</li>
 *   <li>柚子身体 + 柚子头 → 身体的姓 + 头的名（绫地 + 茉子 = 绫地茉子）。</li>
 * </ul>
 * 柚子社角色的「姓 / 名」拆开放在语言文件里（键按实体注册名生成，
 * 如 {@code yuzusoft.fusion.family.ayachi_nene} / {@code yuzusoft.fusion.given.ayachi_nene}），
 * 因此服务端只发翻译键、由客户端按自身语言渲染。
 */
public final class FusionNames {
    private FusionNames() {}

    /** 拼接左右两半（中文直接相接，英文带空格，见 lang 中的值）。 */
    private static final String JOINED = "yuzusoft.fusion.joined";
    private static final String FAMILY_PREFIX = "yuzusoft.fusion.family.";
    private static final String GIVEN_PREFIX = "yuzusoft.fusion.given.";

    /** 融合生物的名字：身体当「姓/前半」，头当「名/后半」。 */
    public static Component of(EntityType<?> bodyType, EntityType<?> headType) {
        return Component.translatable(JOINED, part(bodyType, FAMILY_PREFIX), part(headType, GIVEN_PREFIX));
    }

    /** 柚子社生物取「姓」或「名」，其余（原版生物）取原版实体名。 */
    private static Component part(EntityType<?> type, String yuzusoftPrefix) {
        String path = yuzusoftPath(type);
        return path == null ? type.getDescription() : Component.translatable(yuzusoftPrefix + path);
    }

    /** 该实体是否本模组的柚子社生物；是则返回注册名，否则 null。 */
    @Nullable
    private static String yuzusoftPath(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null && CycloneSwordMod.MODID.equals(key.getNamespace()) ? key.getPath() : null;
    }
}