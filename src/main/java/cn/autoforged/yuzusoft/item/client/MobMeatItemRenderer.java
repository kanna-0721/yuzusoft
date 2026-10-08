package cn.autoforged.yuzusoft.item.client;

import cn.autoforged.yuzusoft.component.MobMeatData;
import cn.autoforged.yuzusoft.component.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * 生物肉的自定义渲染器：读取组件中的来源实体，用其原渲染器画“迷你生物”，
 * 缩放按来源实体体型归一化，保证每种肉在物品格里大小一致、都占满约 3/4 格。
 * 物品栏、掉落物和手持共用同一条 builtin/entity 管线，视角摆位由模型 JSON 的
 * display 区块提供。
 */
public class MobMeatItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final MobMeatItemRenderer INSTANCE = new MobMeatItemRenderer();

    /** 迷你生物最高的一维占物品格（1 模型单位 = 1 格 = 16 px）的比例。 */
    private static final float TARGET_EXTENT = 0.75F;

    private final Map<ResourceLocation, Entity> fakeEntities = new HashMap<>();
    private Level lastLevel;

    private MobMeatItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        MobMeatData data = stack.get(ModDataComponents.MOB_MEAT.get());
        if (data == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            // 主菜单等无世界场合不渲染实体（物品仍可在界面上显示）
            return;
        }
        if (level != lastLevel) {
            fakeEntities.clear();
            lastLevel = level;
        }
        Entity entity = fakeEntities.computeIfAbsent(data.entity(), id -> {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type == null) {
                return null;
            }
            Entity created = type.create(level);
            if (created == null) {
                return null;
            }
            created.setPos(0.0D, 0.0D, 0.0D);
            created.setYRot(0.0F);
            created.setXRot(0.0F);
            created.yRotO = 0.0F;
            created.xRotO = 0.0F;
            if (created instanceof Mob mob) {
                mob.setTarget(null);
            }
            return created;
        });
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
        // 实体原点在脚底，下移半身高让身体围绕槽位中心
        poseStack.translate(0.0D, -entity.getBbHeight() * 0.5D, 0.0D);
        try {
            EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
            EntityRenderer<? super Entity> renderer = dispatcher.getRenderer(entity);
            // 假实体不做动画，partialTick 传 0 即可
            renderer.render(entity, 0.0F, 0.0F, poseStack, bufferSource, packedLight);
        } catch (Throwable ignored) {
            // 个别实体渲染器在假实体上可能读不到状态：跳过渲染而不是崩溃
        }
        poseStack.popPose();
    }
}