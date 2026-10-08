package cn.autoforged.yuzusoft.mixin;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 提高主世界动物（CREATURE 类别）的总生成数量，不改动刷怪权重：
 * 将 maxInstancesPerChunk（原版 10）放大 BOOST_MULTIPLIER 倍，
 * 使每个刷怪循环的动物生成尝试次数与区域容纳上限同步上升。
 * 仅影响 CREATURE（牛/羊/猪/鸡等陆地动物），怪物/水生/环境不受影响。
 */
@Mixin(MobCategory.class)
public class MobCategoryMixin {

    /** 动物生成量放大倍数（10 -> 30 次尝试/循环）。 */
    private static final int BOOST_MULTIPLIER = 3;

    @Inject(method = "getMaxInstancesPerChunk", at = @At("RETURN"), cancellable = true)
    private void yuzusoft$boostAnimalSpawns(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == MobCategory.CREATURE) {
            cir.setReturnValue(cir.getReturnValue() * BOOST_MULTIPLIER);
        }
    }
}
