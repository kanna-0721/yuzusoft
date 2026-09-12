package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.item.ModItems;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IItemExtension;

public class GuitarWeaponItem extends Item implements IItemExtension {
    private static final int RANGED_COOLDOWN = 40;

    public GuitarWeaponItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 from = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 to;
            float sonicDamage;
            if (stack.getItem() == ModItems.GUITAR_WEAPON_FIXED.get()) {
                to = from.add(look.scale(30.0));
                sonicDamage = 8.0F;
            } else {
                to = from.add(look.scale(25.0));
                sonicDamage = 6.0F;
            }
            EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                    level, player, from, to,
                    player.getBoundingBox().expandTowards(look.scale(30.0)).inflate(1.0),
                    e -> e instanceof LivingEntity && !e.is(player));
            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity target) {
                Vec3 origin = player.position().add(0.0, player.getEyeHeight() * 0.8, 0.0);
                Vec3 targetVec = target.getEyePosition().subtract(origin);
                Vec3 dir = targetVec.normalize();
                int steps = Mth.floor(targetVec.length()) + 7;
                for (int j = 1; j < steps; j++) {
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                            origin.x + dir.x * j, origin.y + dir.y * j, origin.z + dir.z * j,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
                target.hurt(level.damageSources().sonicBoom(player), sonicDamage);
            }
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUITAR_WEAPON_RANGED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, RANGED_COOLDOWN);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount()==1;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Holder<Enchantment> enchantment) {
        // 通用附魔：耐久、经验修补、消失诅咒
        if (enchantment.is(Enchantments.UNBREAKING) ||
                enchantment.is(Enchantments.MENDING) ||
                enchantment.is(Enchantments.VANISHING_CURSE)) {
            return true;
        }
        // 所有剑专属附魔
        return enchantment.is(Enchantments.SHARPNESS) ||
                enchantment.is(Enchantments.SMITE) ||
                enchantment.is(Enchantments.BANE_OF_ARTHROPODS) ||
                enchantment.is(Enchantments.KNOCKBACK) ||
                enchantment.is(Enchantments.FIRE_ASPECT) ||
                enchantment.is(Enchantments.LOOTING) ||
                enchantment.is(Enchantments.SWEEPING_EDGE);
    }
}