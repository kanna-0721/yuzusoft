package cn.autoforged.yuzusoft.charm;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * 魅惑系统的全部注册对象集中在这里。
 */
public final class CharmRegistry {
    private CharmRegistry() {}

    public static final String NAMESPACE = CycloneSwordMod.MODID;

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    // ---------- 注册表 ----------
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NAMESPACE);
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NAMESPACE);
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, NAMESPACE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NAMESPACE);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NAMESPACE);

    /** 紫红主题色：状态主色、箭镞染色共用。 */
    public static final int CHARM_COLOR = 0xC03CC0;

    public static final int CHARM_DURATION = 45 * 20;
    public static final int LONG_CHARM_DURATION = 90 * 20;

    // ---------- 物品 ----------
    public static final DeferredItem<CharmBottleItem> CHARM_BOTTLE =
            ITEMS.registerItem("charm_bottle",
                    props -> new CharmBottleItem(props.stacksTo(16)
                            .attributes(CharmBottleItem.REACH_MODIFIERS)));

    public static final DeferredItem<CharmArrowItem> CHARM_ARROW =
            ITEMS.registerItem("charm_arrow",
                    props -> new CharmArrowItem(props.stacksTo(64)
                            // 默认烘焙 45s 魅惑成分：裸物品/创造栏/give 直接可用，
                            // 合成配方会显式覆盖此成分。用自定义效果列表而非药水引用，
                            // 保证原版 Arrow 命中时按完整时长施加（药水引用会被原版缩放为 1/8）。
                            // 用方法调用避免前向引用（CHARM_EFFECT 声明在后）。
                            .component(DataComponents.POTION_CONTENTS,
                                    charmContents(CHARM_DURATION))));

    /** 魅惑之心：右键吸收被魅惑生物 / 对方块右键释放。默认堆叠 64；装生物时由 stack 级组件压到 1。 */
    public static final DeferredItem<CharmHeartItem> CHARM_HEART =
            ITEMS.registerItem("charm_heart",
                    props -> new CharmHeartItem(props.stacksTo(64)));

    // ---------- 状态效果 ----------
    public static final DeferredHolder<MobEffect, CharmMobEffect> CHARM_EFFECT =
            EFFECTS.register("charm", CharmMobEffect::new);

    // ---------- 药水 ----------
    public static final DeferredHolder<Potion, Potion> CHARM_POTION =
            POTIONS.register("charm",
                    () -> new Potion(new MobEffectInstance(
                            CHARM_EFFECT, CHARM_DURATION, 0, false, true)));

    public static final DeferredHolder<Potion, Potion> LONG_CHARM_POTION =
            POTIONS.register("long_charm",
                    () -> new Potion(new MobEffectInstance(
                            CHARM_EFFECT, LONG_CHARM_DURATION, 0, false, true)));

    // ---------- 创造模式标签页（并入 Yuzusoft 主栏，不单独开栏） ----------

    /** 把魅惑烘焙成"自定义效果列表"形式的 PotionContents：药水/滞留通用，箭命中时原时长施加。 */
    private static PotionContents charmContents(int durationTicks) {
        return new PotionContents(
                Optional.<Holder<Potion>>empty(),
                Optional.of(CHARM_COLOR),
                List.of(new MobEffectInstance(
                        CharmRegistry.CHARM_EFFECT, durationTicks, 0, false, true)));
    }

    /** 延长魅惑箭（90s）：供主栏 displayItems 烘焙健康栈。 */
    public static ItemStack charmArrow(int durationTicks) {
        ItemStack stack = new ItemStack(CHARM_ARROW.get());
        stack.set(DataComponents.POTION_CONTENTS, charmContents(durationTicks));
        return stack;
    }

    // ---------- 配方序列化器 ----------
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<CharmArrowRecipe>>
            CHARM_ARROW_SERIALIZER = RECIPE_SERIALIZERS.register("crafting_special_charmarrow",
                    () -> new SimpleCraftingRecipeSerializer<>(CharmArrowRecipe::new));

    // ---------- 实体挂载数据（契约，写实体 NBT，跨存档保留） ----------
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CharmState>> CHARM_STATE =
            ATTACHMENTS.register("charm_state",
                    () -> AttachmentType.builder(() -> (CharmState) null)
                            .serialize(CharmState.CODEC)
                            .build());

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        EFFECTS.register(modBus);
        POTIONS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
