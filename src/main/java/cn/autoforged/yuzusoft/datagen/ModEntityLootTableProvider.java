package cn.autoforged.yuzusoft.datagen;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import java.util.stream.Stream;

public class ModEntityLootTableProvider extends EntityLootSubProvider {

    public ModEntityLootTableProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    public void generate() {
        add(ModEntities.GUARDIAN.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(0))
                        .add(LootItem.lootTableItem(ModItems.CYCLONE_SWORD.get())
                                .setWeight(0))));
        add(ModEntities.SHADOW_ASSASSIN.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SHADOW_DART.get())
                                .setWeight(1)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F))))));
        add(ModEntities.GUARDIAN_TRADER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.TAMAGOYAKI.get())
                                .setWeight(1))));
        add(ModEntities.DETONATOR_THROWING_MONSTER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.DETONATOR.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))));
        add(ModEntities.FROST_GUARDIAN.get(), LootTable.lootTable());
        add(ModEntities.FLOATING_SENTINEL.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.FLOATING_SENTINEL_CHESTPLATE.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.02F)))));
        add(ModEntities.VILLAGE_GUARDIAN.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0f))
                                .add(LootItem.lootTableItem(ModItems.DUMPLINGS)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0f))))));
        // 洒水苦力怕的掉落（普通形态 30% 洒水器 / 矿工形态 0.1% 下界合金镐）在
        // SprinklerCreepEntity.dropCustomDeathLoot() 中按形态处理，gen 表保持为空。
        add(ModEntities.SPRINKLER_CREEP.get(), LootTable.lootTable());
        add(ModEntities.GUITAR_MONSTER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.GUITAR_WEAPON.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.1f)))));
        add(ModEntities.FLASHBANG_MONSTER.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.FLASHBANG))
                        )
        );
        this.add(ModEntities.HAMMER_WIELDER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.STUN_HAMMER.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.02F)))));
        add(ModEntities.SLEEPY_SPIRIT.get(), LootTable.lootTable());
        add(ModEntities.BLOOD_SUCKER_ZOMBIE.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(0.0f, 1.0f))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(LootItem.lootTableItem(ModItems.BLOOD_PACK_BASIC.get()))
                        .when(LootItemRandomChanceCondition.randomChance(0.05f))));
        // 邪恶七海：0721 袭击专属，击败固定掉落 2 个饺子
        add(ModEntities.EVIL_NANAMI.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(LootItem.lootTableItem(ModItems.DUMPLINGS)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f))))));
        add(ModEntities.DUAL_FORM_MOB.get(), LootTable.lootTable());
        add(ModEntities.HUMANOID_CREATURE.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.WHEAT)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2))))));
        // Suzune 固定掉 1 个警报器：由 SuzuneEntity.dropCustomDeathLoot 在钻出状态被杀时掉落，战利品表置空避免重复
        add(ModEntities.SUZUNE.get(), LootTable.lootTable());
        // 无掉落的 LivingEntity（Monster/Creature）：空表占位以满足 EntityLootSubProvider 校验
        // 注意：MISC 类弹射物（Projectile）禁止添加战利品表，否则触发
        // "not a LivingEntity so should not have loot" 校验错误
        add(ModEntities.FROST_GUARDIAN_V2.get(), LootTable.lootTable());
        // 无掉落的 LivingEntity（Monster）：空表占位以满足 EntityLootSubProvider 校验
        add(ModEntities.CAT_0721.get(), LootTable.lootTable());
        // 来海无掉落：空表占位
        add(ModEntities.DIAMOND_GUARDIAN.get(), LootTable.lootTable());
        // 水灵（J 工程并入）无固定战利品表：伞由 WaterSpiritEntity.die() 20% 概率直接掉落，置空避免重复
        add(ModEntities.WATER_SPIRIT.get(), LootTable.lootTable());
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntities.ENTITY_TYPES.getEntries().stream()
                .map(e -> (EntityType<?>) e.value());
    }
}
