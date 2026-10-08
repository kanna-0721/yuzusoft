package cn.autoforged.yuzusoft.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 「无头身体」渲染工具：按保存的生物 NBT 用生物自身的渲染器画出躯体，并隐藏头部部件。
 * <p>
 * 同时服务于两条管线：物品栏/手持的假实体渲染（{@code DecapitatedBodyItemRenderer}），
 * 以及世界里真实实体的头部隐藏（{@code ModClientEvents#onRenderLiving}）。
 * <p>
 * 假实体按「实体类型」缓存（每帧只创建一次）。同一类型存在多个装备不同的躯体时，
 * 会用首个躯体的装备外观——这是可接受的近似。
 */
public final class DecapitatedBodyRenderHelper {
    private DecapitatedBodyRenderHelper() {}

    private static final Map<ResourceLocation, Entity> CACHE = new HashMap<>();
    private static Level cachedLevel;

    /** 取得（并缓存）用于渲染的假实体；数据无效或创建失败返回 null。 */
    @Nullable
    public static Entity resolve(@Nullable Level level, @Nullable CompoundTag mobData) {
        if (level == null || mobData == null) {
            return null;
        }
        EntityType<?> type = EntityType.by(mobData).orElse(null);
        if (type == null) {
            return null;
        }
        if (level != cachedLevel) {
            CACHE.clear();
            cachedLevel = level;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        Entity cached = CACHE.get(id);
        if (cached == null) {
            cached = type.create(level);
            if (cached == null) {
                return null;
            }
            try {
                cached.load(mobData.copy());
            } catch (Throwable ignored) {
                // 个别实体的 NBT 加载依赖世界状态：忽略，仍用默认外观渲染
            }
            cached.setPos(0.0D, 0.0D, 0.0D);
            if (cached instanceof LivingEntity living) {
                // 死亡动画/受击闪红不该出现在躯体上
                living.deathTime = 0;
                living.hurtTime = 0;
                living.setHealth(living.getMaxHealth());
            }
            CACHE.put(id, cached);
        }
        return cached;
    }

    /**
     * 找出某实体渲染器所用模型里所有「头部」部件（含帽子/外层覆盖层），调用方负责临时隐藏。
     * <p>
     * 覆盖三条路径，原版四种有头生物与 yuzusoft 自定义生物都能命中：
     * <ol>
     *   <li>{@link HeadedModel}（僵尸/骷髅/猪灵等人形、牛/猫等四足）；</li>
     *   <li>{@link HumanoidModel}——补上 {@code hat} 覆盖层，避免留下漂浮的帽子层；</li>
     *   <li>{@link HierarchicalModel}（yuzusoft 自定义模型，头部部件名统一为 {@code head}）；</li>
     *   <li>{@link CreeperModel}——苦力怕模型既不是 hierarchical 也没实现 HeadedModel，
     *       但它的 {@code root()} 是公开的，可从中取 {@code head}。</li>
     * </ol>
     */
    public static List<ModelPart> headParts(EntityRenderer<?> renderer) {
        if (!(renderer instanceof LivingEntityRenderer<?, ?> living)) {
            return List.of();
        }
        EntityModel<?> model = living.getModel();
        if (model instanceof HumanoidModel<?> humanoid) {
            List<ModelPart> parts = new ArrayList<>(2);
            parts.add(humanoid.head);
            parts.add(humanoid.hat);
            return parts;
        }
        if (model instanceof HeadedModel headed) {
            return List.of(headed.getHead());
        }
        if (model instanceof HierarchicalModel<?> hierarchical) {
            ModelPart root = hierarchical.root();
            ModelPart head = root.hasChild("head") ? root.getChild("head")
                    : hierarchical.getAnyDescendantWithName("head").orElse(null);
            return head == null ? List.of() : List.of(head);
        }
        if (model instanceof CreeperModel<?> creeper) {
            ModelPart root = creeper.root();
            return root.hasChild("head") ? List.of(root.getChild("head")) : List.of();
        }
        return List.of();
    }

    /** 用实体自身渲染器绘制，隐藏模型中的头部部件；调用方负责摆位与缩放。 */
    public static void draw(EntityRenderDispatcher dispatcher, Entity entity,
                            PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        float yRot = readYaw(entity);
        entity.setYRot(yRot);
        entity.yRotO = yRot;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yRot;
            living.yBodyRotO = yRot;
            living.yHeadRot = yRot;
            living.yHeadRotO = yRot;
        }
        EntityRenderer<? super Entity> renderer = dispatcher.getRenderer(entity);
        List<ModelPart> heads = headParts(renderer);
        setVisible(heads, false);
        try {
            renderer.render(entity, yRot, 0.0F, poseStack, buffers, packedLight);
        } catch (Throwable ignored) {
            // 个别渲染器在假实体上可能读不到状态：跳过渲染而不是崩溃
        } finally {
            setVisible(heads, true);
        }
    }

    public static void setVisible(List<ModelPart> parts, boolean visible) {
        for (ModelPart part : parts) {
            part.visible = visible;
        }
    }

    /** 从实体自身字段读朝向（{@link #resolve} 已把 NBT 中的 Rotation 载入实体）。 */
    private static float readYaw(Entity entity) {
        return entity.getYRot();
    }
}