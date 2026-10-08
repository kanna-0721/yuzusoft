package cn.autoforged.yuzusoft.transformation;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Map;

/**
 * 变身形态的战斗相关数据表。
 * <ul>
 *     <li><b>专武</b>：变身成原版持械生物时发放其招牌武器，结束变身（换形态 / 变回玩家）时从背包回收；</li>
 *     <li><b>攻击附加</b>：变身成会施加状态效果的生物后，近战 / 远程命中时施加对应效果
 *     （洞穴蜘蛛近战中毒、尸壳近战饥饿、凋灵骷髅近战凋零、流浪者远程迟缓、沼骸远程中毒）。</li>
 * </ul>
 * 专武用 {@code CUSTOM_DATA} 打标，回收时只清理带标记的副本，不会误删玩家自己的同类物品。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationCombat {
    /** 专武标记键。 */
    private static final String WEAPON_KEY = "transformation_weapon";

    /** 专武：生物 → 武器（仅近战；弓 / 弩等远程专武已取消，改由权杖左键发射）。 */
    private record Weapon(Item item) {}

    private static final Map<EntityType<?>, Weapon> WEAPONS = Map.ofEntries(
            Map.entry(EntityType.WITHER_SKELETON, new Weapon(Items.STONE_SWORD)),
            Map.entry(EntityType.VEX, new Weapon(Items.IRON_SWORD)),
            Map.entry(EntityType.PIGLIN, new Weapon(Items.GOLDEN_SWORD)),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, new Weapon(Items.GOLDEN_SWORD)),
            Map.entry(EntityType.PIGLIN_BRUTE, new Weapon(Items.GOLDEN_AXE)),
            Map.entry(EntityType.VINDICATOR, new Weapon(Items.IRON_AXE)));

    /** 攻击附加效果：生物 → 效果（时长刻 / 等级）。 */
    private record AttackEffect(Holder<MobEffect> effect, int duration, int amplifier) {}

    /** 仅近战命中时施加。 */
    private static final Map<EntityType<?>, AttackEffect> MELEE_EFFECTS = Map.of(
            EntityType.CAVE_SPIDER, new AttackEffect(MobEffects.POISON, 140, 0),
            EntityType.HUSK, new AttackEffect(MobEffects.HUNGER, 140, 0),
            EntityType.WITHER_SKELETON, new AttackEffect(MobEffects.WITHER, 200, 0));

    /** 仅远程命中时施加（原版穿在箭矢上）。 */
    private static final Map<EntityType<?>, AttackEffect> RANGED_EFFECTS = Map.of(
            EntityType.STRAY, new AttackEffect(MobEffects.MOVEMENT_SLOWDOWN, 600, 0),
            EntityType.BOGGED, new AttackEffect(MobEffects.POISON, 100, 0));

    private TransformationCombat() {}

    /** 变身为该生物时发放其专武（主手空则放主手，否则塞进背包）。 */
    public static void grantWeapon(ServerPlayer player, EntityType<?> type) {
        Weapon weapon = WEAPONS.get(type);
        if (weapon == null) return;
        ItemStack stack = new ItemStack(weapon.item());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(WEAPON_KEY, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** 结束变身时回收带标记的专武（背包 + 副手 + 护甲槽，兼容被挪动过的情况）。 */
    public static void reclaimWeapon(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        boolean changed = removeMarked(inventory.items);
        changed |= removeMarked(inventory.offhand);
        changed |= removeMarked(inventory.armor);
        if (changed) player.inventoryMenu.broadcastChanges();
    }

    private static boolean removeMarked(List<ItemStack> slots) {
        boolean changed = false;
        for (int i = 0; i < slots.size(); i++) {
            if (isMarked(slots.get(i))) {
                slots.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean isMarked(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(WEAPON_KEY);
    }

    /** 变身形态的攻击附加效果；{@code ranged} 为远程（弹射物）命中。 */
    public static void applyAttackEffect(EntityType<?> type, LivingEntity attacker, LivingEntity target, boolean ranged) {
        AttackEffect effect = (ranged ? RANGED_EFFECTS : MELEE_EFFECTS).get(type);
        if (effect == null) return;
        target.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier(), false, true), attacker);
    }
}