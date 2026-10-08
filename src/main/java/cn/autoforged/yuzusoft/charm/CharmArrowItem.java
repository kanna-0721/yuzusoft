package cn.autoforged.yuzusoft.charm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 魅惑之箭：箭矢形态的魅惑药水。
 * 物品上带 POTION_CONTENTS（整段继承自滞留魅惑药水的自定义效果列表），
 * 命中时由原版 Arrow 逻辑原时长施加魅惑（自定义效果不做 1/8 缩放），
 * 并经由效果来源（射手）绑定契约主人。
 */
public class CharmArrowItem extends ArrowItem {
    public CharmArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        // 原版箭实体即可承载药水成分；命中后按完整时长施加魅惑
        return new Arrow(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        Arrow arrow = new Arrow(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.yuzusoft.charm_arrow");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        contents.addPotionTooltip(tooltip::add, 1.0F, context.tickRate());
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.world.entity.LivingEntity livingEntity) {
        return false;
    }
}
