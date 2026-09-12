package cn.autoforged.yuzusoft.item.custom;
import cn.autoforged.yuzusoft.entity.custom.ShadowDartEntity;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class ShadowDartItem extends Item implements ProjectileItem {
    public ShadowDartItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SHADOW_DART_THROW.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ShadowDartEntity dart = new ShadowDartEntity(level, player);
            dart.setItem(itemstack);
            dart.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(dart);
        }
        player.getCooldowns().addCooldown(this, 10);
        itemstack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ShadowDartEntity dart = new ShadowDartEntity(ModEntities.SHADOW_DART.get(), level);
        dart.setPos(pos.x(), pos.y(), pos.z());
        dart.setItem(stack);
        return dart;
    }
}
