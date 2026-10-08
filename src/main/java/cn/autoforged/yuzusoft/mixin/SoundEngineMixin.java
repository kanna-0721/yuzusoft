package cn.autoforged.yuzusoft.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 放宽音高裁剪范围：原版 {@code SoundEngine.calculatePitch} 把 pitch 限制在 0.5~2.0，
 * 而 61 键钢琴需要 0.1768（C2）~ 5.6569（C7）。做法参考 PianoCraft 的 SoundSystemMixin。
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Inject(method = "calculatePitch", at = @At("HEAD"), cancellable = true)
    private void yuzusoft$widenPitchRange(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(Mth.clamp(sound.getPitch(), 0.05F, 10.0F));
    }
}