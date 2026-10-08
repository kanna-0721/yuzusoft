package cn.autoforged.yuzusoft.item.custom;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * 「无头身体」物品：被手术刀击杀的有头生物掉落的躯体（携带来源生物完整 NBT）。
 * <p>
 * 右键放置到任意 2 格高空间，即在该处生成一只<b>冻结的真实生物实体</b>——
 * 用它自己的渲染器画身体（客户端隐藏头部），所以外观一定正确；
 * 空手再次右键可拾回，手持头颅右键则按 {@code ScalpelEvents} 的规则复活或融合。
 */
public class DecapitatedBodyItem extends Item {
    public DecapitatedBodyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        CompoundTag data = context.getItemInHand().get(ModDataComponents.STORED_MOB.get());
        EntityType<?> type = data == null ? null : EntityType.by(data).orElse(null);
        if (type == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!hasTwoHighSpace(level, pos)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Entity body = type.create(level);
        if (body == null) {
            return InteractionResult.FAIL;
        }
        body.load(data.copy());
        // 原实体已死亡，换新 UUID，避免与原记录冲突导致 addFreshEntity 被拒
        body.setUUID(UUID.randomUUID());
        Player player = context.getPlayer();
        float yRot = player == null ? 0.0F : player.getYRot() + 180.0F;
        body.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yRot, 0.0F);
        body.setDeltaMovement(Vec3.ZERO);
        body.setInvulnerable(true);
        body.setNoGravity(true);
        body.setSilent(true);
        if (body instanceof LivingEntity living) {
            living.deathTime = 0;
            living.hurtTime = 0;
            living.setHealth(living.getMaxHealth());
            living.yBodyRot = yRot;
            living.yBodyRotO = yRot;
            living.yHeadRot = yRot;
            living.yHeadRotO = yRot;
        }
        if (body instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            mob.setTarget(null);
        }
        // 标记 + 负载：客户端据此隐藏头部，拾回时据此还原物品
        body.setData(ModAttachments.DECAPITATED_BODY.get(), data.copy());
        if (!level.addFreshEntity(body)) {
            return InteractionResult.FAIL;
        }

        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.playSound(null, pos, SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    /** 需要 2 格高空间：本格 + 上方一格都要可替换。 */
    private static boolean hasTwoHighSpace(Level level, BlockPos pos) {
        if (pos.getY() >= level.getMaxBuildHeight() - 1) {
            return false;
        }
        return level.getBlockState(pos).canBeReplaced() && level.getBlockState(pos.above()).canBeReplaced();
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = sourceType(stack);
        if (type == null) {
            return super.getName(stack);
        }
        return Component.translatable("item." + CycloneSwordMod.MODID + ".decapitated_body", sourceName(type));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EntityType<?> type = sourceType(stack);
        tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".decapitated_body.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
        if (type != null) {
            tooltip.add(Component.translatable("item." + CycloneSwordMod.MODID + ".decapitated_body.tooltip.source",
                    sourceName(type)).withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private static EntityType<?> sourceType(ItemStack stack) {
        CompoundTag tag = stack.get(ModDataComponents.STORED_MOB.get());
        return tag == null ? null : EntityType.by(tag).orElse(null);
    }

    private static Component sourceName(EntityType<?> type) {
        return Component.translatable(type.getDescriptionId());
    }
}