package cn.autoforged.yuzusoft.item.client;

import cn.autoforged.yuzusoft.block.client.DecapitatedBodyRenderHelper;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 「无头身体」物品的自定义渲染器：读取 {@code STORED_MOB} 组件，用来源生物自身的渲染器
 * 画躯体（隐藏头部），并按体型归一化缩放，使各种大小的躯体在物品格里都约占 3/4 格。
 * 物品栏、掉落物与手持共用同一条 builtin/entity 管线，视角摆位由模型 JSON 的 display 区块提供。
 */
public class DecapitatedBodyItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final DecapitatedBodyItemRenderer INSTANCE = new DecapitatedBodyItemRenderer();

    /** 躯体最高的一维占物品格（1 模型单位 = 1 格 = 16 px）的比例。 */
    private static final float TARGET_EXTENT = 0.8F;

    private DecapitatedBodyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        CompoundTag data = stack.get(ModDataComponents.STORED_MOB.get());
        if (data == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            // 主菜单等无世界场合不渲染实体（物品仍可在界面上显示）
            return;
        }
        Entity entity = DecapitatedBodyRenderHelper.resolve(level, data);
        if (entity == null) {
            return;
        }

        poseStack.pushPose();
        // 物品模型空间是 [0,1]^3 的方块，(0.5,0.5,0.5) 才是槽位中心；
        // 实体原点在脚底、默认落在方块角点上，所以先平移到中心再缩放。
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float extent = Math.max(entity.getBbHeight(), Math.max(entity.getBbWidth(), 0.2F));
        float scale = TARGET_EXTENT / extent;
        poseStack.scale(scale, scale, scale);
        // 实体原点在脚底，下移半身高让躯体围绕槽位中心
        poseStack.translate(0.0D, -entity.getBbHeight() * 0.5D, 0.0D);
        DecapitatedBodyRenderHelper.draw(minecraft.getEntityRenderDispatcher(),
                entity, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}