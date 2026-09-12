package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
public class StunHammerItem extends Item {
    public static final int COOLDOWN_TICKS = 100;
    public static final double AOE_RADIUS = 4.0;
    public static final int AOE_STUN_DURATION = 100;
    public static final int STUN_DURATION = 100;
    public static final Tier HAMMER_TIER = new Tier() {
        @Override
        public int getUses() {
            return 500;
        }

        @Override
        public float getSpeed() {
            return 6.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.IRON_INGOT);
        }
    };

    public StunHammerItem(Tier tier, Properties properties) {
        super(properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_DURATION, 0));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        player.swing(hand);
        if (!level.isClientSide()) {
            AABB area = player.getBoundingBox().inflate(AOE_RADIUS, AOE_RADIUS, AOE_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player && entity.isAlive() && !(entity instanceof ArmorStand));
            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(ModEffects.STUN, AOE_STUN_DURATION, 0));
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.HAMMER_SLAM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

