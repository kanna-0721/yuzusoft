package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

/**
 * J 键：发射胸甲技能（潜影贝漂浮弹）。
 */
@EventBusSubscriber(modid = CycloneSwordMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModKeyMappings {

    public static final String KEY_CATEGORY = "key.categories." + CycloneSwordMod.MODID;

    public static final Lazy<KeyMapping> CHESTPLATE_ABILITY_KEY = Lazy.of(() -> new KeyMapping(
            "key." + CycloneSwordMod.MODID + ".chestplate_ability",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> MOUNT_KEY = Lazy.of(() -> new KeyMapping(
            "key." + CycloneSwordMod.MODID + ".mount",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> DISMOUNT_KEY = Lazy.of(() -> new KeyMapping(
            "key." + CycloneSwordMod.MODID + ".dismount",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            KEY_CATEGORY));

    /** 骑乘白雪乃爱飞行时：X 键下降（空格键上升）。 */
    public static final Lazy<KeyMapping> DUALFORM_DESCEND_KEY = Lazy.of(() -> new KeyMapping(
            "key." + CycloneSwordMod.MODID + ".dualform_descend",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_CATEGORY));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CHESTPLATE_ABILITY_KEY.get());
        event.register(MOUNT_KEY.get());
        event.register(DISMOUNT_KEY.get());
        event.register(DUALFORM_DESCEND_KEY.get());
    }
}

