package cn.autoforged.yuzusoft.charm;

import cn.autoforged.yuzusoft.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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

/**
 * 魅惑之心：
 * - 手持空心，右键"被魅惑的生物"→ 吸收：该生物被移除，状态完整封入物品数据组件；
 * - 手持已封入的心，对方块表面（任意面）右键 → 释放：在点击位置附近还原该生物，心变回空心。
 * 吸收/释放走实体 NBT 往返（Entity#save / EntityType#loadEntityRecursive），
 * 因此血量、效果、装备、AI 记忆、魅惑契约（charm_state 附件）与移速全部原样保持。
 */
public class CharmHeartItem extends Item {

    public CharmHeartItem(Properties properties) {
        super(properties);
    }

    // ------------------------------------------------------------------
    //  吸收：右键被魅惑的生物
    // ------------------------------------------------------------------
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Mob mob)) {
            return InteractionResult.PASS;
        }
        // 已经装着生物的心不能再装
        if (stack.has(ModDataComponents.STORED_MOB.get())) {
            return InteractionResult.PASS;
        }
        // 只接受"魅惑生效中"的生物
        if (!CharmHelper.isCharmActive(mob)) {
            return InteractionResult.PASS;
        }
        // 拒绝载具/乘客：discard() 会弹出乘客，而 saveWithoutId 已把 Passengers 写进 NBT，
        // 释放时会出现乘客翻倍。直接不受理，语义上不影响普通生物。
        if (mob.isVehicle() || mob.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (!mob.isAlive()) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = new CompoundTag();
        if (!mob.save(tag)) { // save 会拒绝 passenger；成功后含 "id"
            return InteractionResult.FAIL;
        }

        // 装生物的心不可堆叠：新建 count=1 的栈并覆盖 stack 级 MAX_STACK_SIZE
        ItemStack filled = new ItemStack(this);
        filled.set(ModDataComponents.STORED_MOB.get(), tag);
        filled.set(DataComponents.MAX_STACK_SIZE, 1);
        // 视觉标记：ItemStack#hasFoil 直接读此组件，为 true 即渲染附魔光效，便于区分空心/已封入
        filled.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        if (stack.getCount() <= 1) {
            // 手上只有一个（创造模式收到的还是副本，就地改动无效）→ 直接替换手持格。
            // 关键：绝不能先把 stack 减到 0。Player#interactOn 在交互返回后会检查它自己持有的
            // 那个 stack 对象，一旦为空就把手持格整个清空（连刚放进去的心一起丢掉）。
            player.setItemInHand(hand, filled);
        } else {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    mob.getRandomX(0.7), mob.getY(mob.getBbHeight() * 0.7), mob.getRandomZ(0.7),
                    8, 0.2, 0.2, 0.2, 0.0);
            serverLevel.playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 0.7F, 1.4F);
            mob.discard();
        }
        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------------
    //  释放：对方块右键
    // ------------------------------------------------------------------
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.get(ModDataComponents.STORED_MOB.get());
        if (tag == null) {
            return InteractionResult.PASS; // 空心：交给方块原版行为
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // 落点：点击方块若可穿（无碰撞）就落在它里面，否则落在被点击面的相邻格
        BlockPos clicked = context.getClickedPos();
        BlockPos spawnPos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()
                ? clicked
                : clicked.relative(context.getClickedFace());
        Vec3 vec = Vec3.atBottomCenterOf(spawnPos);

        // fn 对每一层实体都会调用 → 必须各自 addFreshEntity，否则实体不进世界（不 tick、不可见）
        final boolean[] added = { true };
        Entity restored = EntityType.loadEntityRecursive(tag, serverLevel, entity -> {
            entity.moveTo(vec.x, vec.y, vec.z, entity.getYRot(), entity.getXRot());
            if (!serverLevel.addFreshEntity(entity)) {
                added[0] = false;
            }
            return entity;
        });
        if (restored == null || !added[0]) {
            return InteractionResult.FAIL; // 失败时不扣物品
        }

        // 变回空心，并恢复可堆叠、去掉光效标记
        stack.remove(ModDataComponents.STORED_MOB.get());
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        stack.set(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize());

        serverLevel.sendParticles(ParticleTypes.HEART,
                vec.x, vec.y + 0.7, vec.z, 8, 0.25, 0.25, 0.25, 0.0);
        serverLevel.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7F, 1.0F);
        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------------
    //  提示文本
    // ------------------------------------------------------------------
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.get(ModDataComponents.STORED_MOB.get());
        if (tag == null) {
            tooltip.add(Component.translatable("item.yuzusoft.charm_heart.tooltip.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            Component name = EntityType.by(tag)
                    .<Component>map(type -> Component.translatable(type.getDescriptionId()))
                    .orElseGet(() -> Component.literal(tag.getString("id")));
            tooltip.add(Component.translatable("item.yuzusoft.charm_heart.tooltip.stored", name)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            float health = tag.getFloat("Health");
            if (health > 0.0F) {
                tooltip.add(Component.translatable("item.yuzusoft.charm_heart.tooltip.health",
                        String.format("%.1f", health)).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
