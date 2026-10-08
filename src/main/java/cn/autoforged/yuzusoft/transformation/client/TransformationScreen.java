package cn.autoforged.yuzusoft.transformation.client;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.network.payload.SelectTransformationPayload;
import cn.autoforged.yuzusoft.ui.ModTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变身列表界面：列出已解锁的生物，点击即请求变身。
 * <p>
 * 分两页展示——「原版生物」与「柚子社生物」，页内顺序与创造模式物品栏里刷怪蛋的排列顺序一致。
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖）。
 */
public final class TransformationScreen extends Screen {
    /** 原版刷怪蛋 + 本模组创造栏刷怪蛋的展示顺序（作用于每个 {@link EntityType}）。 */
    private static final Map<EntityType<?>, Integer> SPAWN_EGG_ORDER = buildSpawnEggOrder();

    private final List<String> vanilla = new ArrayList<>();
    private final List<String> yuzusoft = new ArrayList<>();
    private int page;

    private TransformationScreen(List<String> unlocked) {
        super(Component.translatable("screen.yuzusoft.transformation_list"));
        List<String> sorted = new ArrayList<>(unlocked);
        sorted.removeIf("minecraft:player"::equals);
        sorted.sort(Comparator.comparingInt(TransformationScreen::spawnEggOrder).thenComparing(String::compareTo));
        for (String id : sorted) {
            (id.startsWith(CycloneSwordMod.MODID + ":") ? yuzusoft : vanilla).add(id);
        }
    }

    public static void open(List<String> unlocked) {
        Minecraft.getInstance().setScreen(new TransformationScreen(unlocked));
    }

    @Override
    protected void init() {
        List<String> entries = page == 0 ? vanilla : yuzusoft;
        int columns = 4;
        int width = 94;
        int startX = (this.width - columns * width) / 2;
        int startY = 45;
        for (int i = 0; i < entries.size(); i++) {
            String id = entries.get(i);
            int col = i % columns;
            int row = i / columns;
            EntityType.byString(id).ifPresent(type -> addRenderableWidget(Button.builder(type.getDescription(), button -> {
                PacketDistributor.sendToServer(new SelectTransformationPayload(id));
                onClose();
            }).bounds(startX + col * width, startY + row * 25, 88, 20).build()));
        }
        int tabY = this.height - 60;
        Button vanillaTab = Button.builder(Component.translatable("screen.yuzusoft.page_vanilla"), button -> switchPage(0))
                .bounds(this.width / 2 - 92, tabY, 88, 20).build();
        vanillaTab.active = page != 0;
        addRenderableWidget(vanillaTab);
        Button yuzusoftTab = Button.builder(Component.translatable("screen.yuzusoft.page_yuzusoft"), button -> switchPage(1))
                .bounds(this.width / 2 + 4, tabY, 88, 20).build();
        yuzusoftTab.active = page != 1;
        addRenderableWidget(yuzusoftTab);
        addRenderableWidget(Button.builder(Component.translatable("screen.yuzusoft.revert"), button -> {
            PacketDistributor.sendToServer(new SelectTransformationPayload("minecraft:player"));
            onClose();
        }).bounds(this.width / 2 - 44, this.height - 35, 88, 20).build());
    }

    private void switchPage(int target) {
        if (page == target) return;
        page = target;
        rebuildWidgets();
    }

    /** 形态在创造栏刷怪蛋里的顺序；没有对应刷怪蛋的排到最后。 */
    private static int spawnEggOrder(String id) {
        return EntityType.byString(id)
                .map(type -> SPAWN_EGG_ORDER.getOrDefault(type, Integer.MAX_VALUE))
                .orElse(Integer.MAX_VALUE);
    }

    /** 收集原版刷怪蛋标签页与本模组创造栏里的刷怪蛋顺序，首个出现的位置即顺序号。 */
    private static Map<EntityType<?>, Integer> buildSpawnEggOrder() {
        Map<EntityType<?>, Integer> order = new HashMap<>();
        int[] index = {0};
        CreativeModeTab vanilla = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS);
        if (vanilla != null) collectSpawnEggs(order, vanilla.getDisplayItems(), index);
        collectSpawnEggs(order, ModTabs.CYCLONE_TAB.get().getDisplayItems(), index);
        return order;
    }

    private static void collectSpawnEggs(Map<EntityType<?>, Integer> order, Collection<ItemStack> stacks, int[] index) {
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(stack);
                if (type != null) order.putIfAbsent(type, index[0]++);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.yuzusoft.hint"), width / 2, 32, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return true; }
}