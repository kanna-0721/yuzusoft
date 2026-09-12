package cn.autoforged.yuzusoft.block;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.custom.AlarmBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CycloneSwordMod.MODID);

    public static final DeferredBlock<AlarmBlock> SUZUNE_ALARM = BLOCKS.register("suzune_alarm",
            () -> new AlarmBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
