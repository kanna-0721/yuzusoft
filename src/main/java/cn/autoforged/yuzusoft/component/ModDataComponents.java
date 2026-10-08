package cn.autoforged.yuzusoft.component;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CycloneSwordMod.MODID);

    /** 血包当前存储的血量（单位：生命值点数，可为小数） */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> STORED_HEALTH =
            DATA_COMPONENTS.registerComponentType("stored_health",
                    builder -> builder
                            .persistent(Codec.FLOAT)
                            .networkSynchronized(ByteBufCodecs.FLOAT));

    /** 0721 不详之瓶的等级 1-5（对应 0721 袭击等级 1-5）；缺省视为 1 级。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BOTTLE_0721_LEVEL =
            DATA_COMPONENTS.registerComponentType("bottle_0721_level",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT));

    /** 魅惑之心内封存的生物实体 NBT（由 Entity#save 产出，含 id / Health / 效果 / 附件）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> STORED_MOB =
            DATA_COMPONENTS.registerComponentType("stored_mob",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /** 生物肉（并入自 knife 工程）的来源实体与营养值。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MobMeatData>> MOB_MEAT =
            DATA_COMPONENTS.registerComponentType("mob_meat",
                    builder -> builder
                            .persistent(MobMeatData.CODEC)
                            .networkSynchronized(MobMeatData.STREAM_CODEC));

    /** 生锈菜刀恢复完成时的游戏 tick 时刻（服务端 gameTime + 1200）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> RUST_UNTIL =
            DATA_COMPONENTS.registerComponentType("rust_until",
                    builder -> builder
                            .persistent(Codec.LONG)
                            .networkSynchronized(ByteBufCodecs.VAR_LONG));
}