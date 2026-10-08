package cn.autoforged.yuzusoft.block;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.block.custom.AlarmBlock;
import cn.autoforged.yuzusoft.block.custom.GrandPianoBlock;
import cn.autoforged.yuzusoft.block.custom.MobHeadBlock;
import cn.autoforged.yuzusoft.block.custom.MobSkullType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@EventBusSubscriber(modid = CycloneSwordMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CycloneSwordMod.MODID);

    public static final DeferredBlock<AlarmBlock> SUZUNE_ALARM = BLOCKS.register("suzune_alarm",
            () -> new AlarmBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** 三角钢琴：7 段横向多结构、61 键（C2~C7），并入自 PianoNeoForge 工程。 */
    public static final DeferredBlock<GrandPianoBlock> GRAND_PIANO = BLOCKS.register("grand_piano",
            () -> new GrandPianoBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

    /** 全部生物头颅方块：对应生物死亡 1% 掉落，被闪电苦力怕炸死必掉。 */
    public static final Map<MobSkullType, DeferredBlock<MobHeadBlock>> HEADS =
            Collections.unmodifiableMap(createHeads());

    private static Map<MobSkullType, DeferredBlock<MobHeadBlock>> createHeads() {
        Map<MobSkullType, DeferredBlock<MobHeadBlock>> heads = new EnumMap<>(MobSkullType.class);
        for (MobSkullType type : MobSkullType.values()) {
            // 每个方块各自 new 一份 Properties，避免多个方块共享同一实例
            heads.put(type, BLOCKS.register(type.getHeadName(), () -> new MobHeadBlock(type,
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .noOcclusion()
                            .sound(SoundType.STONE))));
        }
        return heads;
    }

    /** 按骷髅类型取对应的头颅方块。 */
    public static DeferredBlock<MobHeadBlock> head(MobSkullType type) {
        return HEADS.get(type);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    /**
     * 各头颅复用原版 SkullBlockEntity（BlockEntityType.SKULL），
     * 必须把方块追加进原版 BE 类型的合法方块集合，否则放置时
     * BlockEntity.validateBlockState 会抛 "Invalid block entity minecraft:skull"。
     */
    @SubscribeEvent
    public static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SKULL, HEADS.values().stream()
                .map(DeferredBlock::get)
                .toArray(Block[]::new));
    }
}
