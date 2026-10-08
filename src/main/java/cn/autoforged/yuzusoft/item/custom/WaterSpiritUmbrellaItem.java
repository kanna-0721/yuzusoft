package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.config.WaterSpiritConfig;
import cn.autoforged.yuzusoft.entity.custom.WaterOrbEntity;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 水灵雨伞（死亡 20% 掉落、随机耐久）：
 * - 主手右键：发射一枚水弹（消耗 1 点耐久，带冷却，可在空中或对着方块点击）；
 * - 副手右键（主手不可用时自动进入，同盾牌）：持伞格挡，正面锥形范围内的
 *   来袭弹射物将被镜像反弹（速度大小不变、方向相反），见 ProjectileReflectHandler。
 */
public class WaterSpiritUmbrellaItem extends Item {

    public WaterSpiritUmbrellaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                fireOrb(serverPlayer, stack);
            }
            // 主手即时发射，不进入"使用"状态；返回 PASS 让副手雨伞仍可格挡
            return InteractionResultHolder.pass(stack);
        }
        // 副手：进入持续格挡状态（isUsingItem），反弹窗口由 ProjectileReflectHandler 每 tick 扫描
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /** 对着方块/实体右键时主手也能发射，避免被方块吸收点击 */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() == InteractionHand.MAIN_HAND
                && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            fireOrb(serverPlayer, context.getItemInHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void fireOrb(ServerPlayer player, ItemStack stack) {
        Level level = player.level();
        if (player.getCooldowns().isOnCooldown(this)) {
            return;
        }
        WaterOrbEntity orb = new WaterOrbEntity(level, player);
        Vec3 look = player.getLookAngle();
        orb.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        orb.shoot(look.x, look.y, look.z, 1.5F, 0.6F);
        level.addFreshEntity(orb);
        player.getCooldowns().addCooldown(this, WaterSpiritConfig.UMBRELLA_SHOT_COOLDOWN_TICKS.get());
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, false);
        level.playSound(null, player, ModSounds.WATER_SPIRIT_SHOOT.get(), SoundSource.PLAYERS, 1.0F, 1.1F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}
