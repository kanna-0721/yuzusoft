package cn.autoforged.yuzusoft.item;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 浮游使者胸甲材质：
 * 胸甲防御 6、耐久 240、韧性 1.0、击退抗性 0.05、附魔能力 10。
 */
public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, CycloneSwordMod.MODID);

    public static final Holder<ArmorMaterial> FLOATING_SENTINEL = ARMOR_MATERIALS.register("floating_sentinel",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 2);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 2);
                        map.put(ArmorItem.Type.BODY, 8);
                    }),
                    10, // enchantability
                    SoundEvents.ARMOR_EQUIP_DIAMOND, // Holder<SoundEvent>
                    () -> Ingredient.of(Items.SHULKER_SHELL),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "floating_sentinel"))),
                    1.0F,  // toughness
                    0.05F  // knockback resistance
            ));
}

