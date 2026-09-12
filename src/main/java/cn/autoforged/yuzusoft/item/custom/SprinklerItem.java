package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.entity.custom.WaterBallEntity;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SprinklerItem extends Item {
    public SprinklerItem(Properties properties) {
        super(properties);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepairItem, ItemStack repairMaterial) {
        // 铁锭修复
        return repairMaterial.is(Items.IRON_INGOT);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SPRINKLER_SHOOT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        if (!level.isClientSide) {
            WaterBallEntity projectile = new WaterBallEntity(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(projectile);
            itemStack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(itemStack));
        }

        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}

