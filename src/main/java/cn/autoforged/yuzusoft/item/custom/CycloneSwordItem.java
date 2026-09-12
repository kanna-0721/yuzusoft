package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.GuardianEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CycloneSwordItem extends SwordItem {
    private static final Tier CYCLONE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1561;
        }

        @Override
        public float getSpeed() {
            return 9.0f;
        }

        @Override
        public float getAttackDamageBonus() {
            return 4.0f;
        }

        @Override
        public int getEnchantmentValue() {
            return 14;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.BREEZE_ROD);
        }
    };

    public CycloneSwordItem() {
        super(CYCLONE_TIER, new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .attributes(SwordItem.createAttributes(CYCLONE_TIER, 3, -2.4f)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            stack.hurtAndBreak(100, (ServerLevel) level, player, item -> {});
            if (stack.isEmpty()) {
                return InteractionResultHolder.consume(stack);
            }

            GuardianEntity guardian = ModEntities.GUARDIAN.get().create(level);
            if (guardian != null) {
                guardian.setPos(player.getX(), player.getY(), player.getZ());
                level.addFreshEntity(guardian);
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
