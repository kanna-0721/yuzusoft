package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.item.custom.WaterSpiritUmbrellaItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * 副手举伞格挡的第一人称"举面前"渲染。
 * 1.21.1 存在 MC-275917：非盾牌的 BLOCK 物品第一人称没有"举到面前"动画
 * （renderArmWithItem 的 BLOCK 分支只做 applyItemArmTransform，物品停在低位）。
 * 这里在 RenderHandEvent 中拦截副手雨伞格挡状态，取消默认渲染，
 * 手动复刻 applyItemArmTransform + 新版（24w44a 起）BLOCK 举面前 transform 后重新画物品。
 */
public final class UmbrellaFirstPersonHandler {

    private UmbrellaFirstPersonHandler() {
    }

    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.OFF_HAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof WaterSpiritUmbrellaItem)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isUsingItem()
                || mc.player.getUsedItemHand() != InteractionHand.OFF_HAND) {
            return;
        }
        event.setCanceled(true);
        renderBlockingUmbrella(event, mc.player, stack);
    }

    private static void renderBlockingUmbrella(RenderHandEvent event, net.minecraft.client.player.LocalPlayer player,
                                               ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        float equipProgress = event.getEquipProgress();

        // 副手手臂（举伞时雨伞在副手，右手玩家 => 左臂，左手玩家 => 右臂）
        HumanoidArm offArm = player.getMainArm().getOpposite();
        int i = offArm == HumanoidArm.RIGHT ? 1 : -1;      // applyItemArmTransform 的横向符号
        float f = offArm == HumanoidArm.RIGHT ? 1.0F : -1.0F; // BLOCK 举面前 transform 的横向符号

        pose.pushPose();
        // 1. 复刻 1.21.1 原版 applyItemArmTransform（BLOCK 分支的原版低位摆放）
        pose.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
        // 2. 复刻 24w44a 起的原版 BLOCK"举面前" transform（1.21.1 因 MC-275917 缺失）
        pose.translate(f * -0.14142136F, 0.08F, 0.14142136F);
        pose.mulPose(Axis.XP.rotationDegrees(-102.25F));
        pose.mulPose(Axis.YP.rotationDegrees(f * 13.365F));
        pose.mulPose(Axis.ZP.rotationDegrees(f * 78.05F));
        // 3. 直接画物品（副手 => FIRST_PERSON_LEFT_HAND；手持物品本就不画皮肤手臂，与盾牌一致）。
        // 必须用带实体的 renderStatic(LivingEntity,...) 重载：8 参 ItemStack 重载会把实体传成 null，
        // blocking 谓词评估时 entity 恒为空，模型 override 永远选闭伞模型。
        mc.getItemRenderer().renderStatic(player, stack, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                false, pose, buffer, player.level(), event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, player.getId());
        pose.popPose();
    }
}
