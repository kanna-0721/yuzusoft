package cn.autoforged.yuzusoft.component;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
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
}