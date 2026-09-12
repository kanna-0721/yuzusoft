package cn.autoforged.yuzusoft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端入口：为模组注册配置界面。
 * 仅客户端加载；NeoForge 内置 ConfigurationScreen 会自动为 ModConfigSpec 生成编辑界面。
 */
@Mod(value = CycloneSwordMod.MODID, dist = Dist.CLIENT)
public class CycloneSwordModClient {

    public CycloneSwordModClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
