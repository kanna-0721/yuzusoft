package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;
import java.util.stream.Collectors;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected void generate() {
        // 警报器只有精准采集工具破坏时才掉落方块本身；非精准采集只掉经验球（AlarmBlock#getExpDrop）
        this.add(ModBlocks.SUZUNE_ALARM.get(), this.createSilkTouchOnlyTable(ModBlocks.SUZUNE_ALARM.get()));
        // 生物头颅：任何方式破坏都掉落自身
        ModBlocks.HEADS.values().forEach(head ->
                this.add(head.get(), this.createSingleItemTable(head.get())));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.get())
                .collect(Collectors.toList());
    }
}
