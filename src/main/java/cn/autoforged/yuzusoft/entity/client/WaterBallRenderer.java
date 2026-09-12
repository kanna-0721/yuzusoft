package cn.autoforged.yuzusoft.entity.client;

import cn.autoforged.yuzusoft.entity.custom.WaterBallEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class WaterBallRenderer extends ThrownItemRenderer<WaterBallEntity> {
    public WaterBallRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, false);
    }
}

