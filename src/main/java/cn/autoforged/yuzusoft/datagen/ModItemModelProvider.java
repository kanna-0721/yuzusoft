package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CycloneSwordMod.MODID, existingFileHelper);
    }


    @Override
    protected void registerModels() {
        handheldItem(ModItems.CYCLONE_SWORD.get());
        withExistingParent(ModItems.GUARDIAN_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        getBuilder("murasame_spawn_egg")
                .parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg"))
                .texture("layer0", modLoc("item/murasame_spawn_egg"))
                .texture("layer1", modLoc("item/murasame_spawn_egg_overlay"));
        basicItem(ModItems.SHADOW_DART.get());
        withExistingParent(ModItems.SHADOW_ASSASSIN_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.TAMAGOYAKI.get());
        basicItem(ModItems.FLOATING_SENTINEL_CHESTPLATE.get());
        withExistingParent(ModItems.FLOATING_SENTINEL_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.DETONATOR.get());
        withExistingParent(ModItems.DETONATOR_MONSTER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.FROST_GUARDIAN_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.DUMPLINGS.get());
        withExistingParent(ModItems.VILLAGE_GUARDIAN_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.EVIL_NANAMI_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        handheldItem(ModItems.SPRINKLER.get());
        withExistingParent(ModItems.SPRINKLER_CREEP_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        handheldItem(ModItems.GUITAR_WEAPON.get());
        handheldItem(ModItems.GUITAR_WEAPON_FIXED.get());
        basicItem(ModItems.GUITAR_UPGRADE_TEMPLATE.get());
        withExistingParent(ModItems.GUITAR_MONSTER_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.SLEEPY_SPIRIT_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.FLASHBANG.get());
        handheldItem(ModItems.STUN_HAMMER.get());
        withExistingParent(ModItems.HAMMER_WIELDER_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.BLOOD_PACK_BASIC.get());
        basicItem(ModItems.BLOOD_PACK_MID.get());
        basicItem(ModItems.BLOOD_PACK_HIGH.get());
        basicItem(ModItems.BLOOD_PACK_TEMPLATE.get());
        withExistingParent(ModItems.BLOOD_SUCKER_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.DUAL_FORM_MOB_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.HUMANOID_CREATURE_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.SUZUNE_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.SUZUNE_ALARM_ITEM.getId().getPath(),
                modLoc("block/suzune_alarm"));
        // 头颅物品需用 template_skull（builtin/entity），交由原版 BlockEntityWithoutLevelRenderer 做 3D 渲染
        ModItems.HEAD_ITEMS.values().forEach(head ->
                withExistingParent(head.getId().getPath(), mcLoc("item/template_skull")));
        withExistingParent(ModItems.FLASHBANG_MONSTER_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.FROST_GUARDIAN_V2_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.DIAMOND_GUARDIAN_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.CAT_0721_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.WATER_SPIRIT_SPAWN_EGG.getId().getPath(),
                mcLoc("item/template_spawn_egg"));
        basicItem(ModItems.OMENS_BOTTLE_0721.get());
    }
    @Override
    public ItemModelBuilder handheldItem(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return withExistingParent(id.getPath(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + id.getPath()));
    }
}
