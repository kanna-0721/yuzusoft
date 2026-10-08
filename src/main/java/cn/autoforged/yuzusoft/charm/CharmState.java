package cn.autoforged.yuzusoft.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.UUID;

/**
 * 魅惑契约数据。写入实体 NBT（AttachmentType 序列化），跨存档保留。
 *
 * @param owner         契约主人（玩家 UUID），可为 null（药水/喷溅等来源没有明确主人时）
 * @param permanent     是否由魅惑之瓶施加的永久契约
 * @param originalSpeed 魅惑前的基础移速；-1 表示未记录（无 MOVEMENT_SPEED 属性时不改动）
 */
public record CharmState(UUID owner, boolean permanent, double originalSpeed) {

    private static final Codec<Optional<UUID>> OWNER_CODEC =
            Codec.STRING.xmap(
                    s -> s.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(s)),
                    opt -> opt.map(UUID::toString).orElse(""));

    public static final Codec<CharmState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OWNER_CODEC.optionalFieldOf("owner", Optional.empty()).forGetter(state -> Optional.ofNullable(state.owner())),
            Codec.BOOL.fieldOf("permanent").orElse(false).forGetter(CharmState::permanent),
            Codec.DOUBLE.optionalFieldOf("original_speed", -1.0).forGetter(CharmState::originalSpeed)
    ).apply(instance, (owner, permanent, originalSpeed) -> new CharmState(owner.orElse(null), permanent, originalSpeed)));
}
