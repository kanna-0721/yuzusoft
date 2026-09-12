package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, CycloneSwordMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.SWORDS).add(ModItems.CYCLONE_SWORD.get());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
        tag(ItemTags.ARMOR_ENCHANTABLE).add(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
        tag(ItemTags.TRIMMABLE_ARMOR).add(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
    }
}
