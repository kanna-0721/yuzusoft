package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * HUD 正上方显示实时游戏日时（等同 /time query daytime，0–24000）。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT)
public class DayTimeHudEvents {

    private static final int DAY_CYCLE = 24000;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.options.hideGui) {
            return;
        }
        long daytime = Math.floorMod(level.getDayTime(), DAY_CYCLE);
        String text = "daytime: " + daytime;
        int x = (minecraft.getWindow().getGuiScaledWidth() - minecraft.font.width(text)) / 2;
        event.getGuiGraphics().drawString(minecraft.font, text, x, 4, 0xFFFFFF, true);
    }
}
