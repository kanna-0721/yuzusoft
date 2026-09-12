package cn.autoforged.yuzusoft.event;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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

    /** 骑乘白雪乃爱飞行时：N 键下降（空格键上升）。默认键可在模组配置界面修改。 */
    public static final Lazy<KeyMapping> DUALFORM_DESCEND_KEY = Lazy.of(() -> new KeyMapping(
            "key." + CycloneSwordMod.MODID + ".dualform_descend",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            KEY_CATEGORY));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CHESTPLATE_ABILITY_KEY.get());
        event.register(MOUNT_KEY.get());
        event.register(DISMOUNT_KEY.get());

        // 从配置读取下降键并覆盖默认绑定（未知键名会解析为 KEY_UNKNOWN，不会崩溃）
        KeyMapping descend = DUALFORM_DESCEND_KEY.get();
        descend.setKey(InputConstants.getKey(ModConfig.DUALFORM_DESCEND_KEY.get()));
        event.register(descend);
    }

    /**
     * Options 在注册键位之后会从 options.txt 恢复旧绑定（旧版本留下的 X 键等），
     * 这里在客户端 setup 末尾强制应用配置值，确保配置为权威来源。
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            DUALFORM_DESCEND_KEY.get().setKey(InputConstants.getKey(ModConfig.DUALFORM_DESCEND_KEY.get())));
    }
}

