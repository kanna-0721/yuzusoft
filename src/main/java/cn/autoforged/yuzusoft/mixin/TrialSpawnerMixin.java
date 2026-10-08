package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.api.TrialSpawnerDataAccessor;
import cn.autoforged.yuzusoft.effect.ModEffects;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 试炼刷怪笼三路分流：只有饮用 0721 不祥之瓶（携带
 * {@link ModEffects#EFFECT_0721_BAD_OMEN}）的玩家才会进入 0721 试炼路线。
 *
 * <p>在 {@link TrialSpawner#tickServer} 开头（与原版玩家探测同为每 20 tick 一次）：
 * <ul>
 *   <li>检测到携带 0721 不祥之兆的玩家 → 镜像原版 {@code transformBadOmenIntoTrialOmen}
 *       转换为其 0721 试炼之兆（时长 = 18000 * 等级），随后与携带 0721 试炼之兆的玩家
 *       一样：刷怪笼尚未不祥 → {@code applyOminous}
 *       （变成不祥外观、按不祥难度与奖励，行为与原版不祥转换一致）；</li>
 *   <li>置位 {@link TrialSpawnerDataAccessor#yz0721$setTrial} 标志，
 *       {@link TrialSpawnerDataMixin} / {@link TrialSpawnerStateMixin} 据此替换怪
 *       并额外吐一次战利品；</li>
 *   <li>无 0721 玩家且未不祥 → 清理标志（防止上一场残留）。</li>
 * </ul>
 * 原版不祥之兆（BAD_OMEN / TRIAL_OMEN）由原版 {@code tryDetectPlayers} 自行处理，
 * 本 mixin 完全不介入，因此原版不祥试炼与原版普通试炼都保持原版路线。
 */
@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerMixin {

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void yuzusoft$detect0721Trial(ServerLevel level, BlockPos pos, boolean isOminous, CallbackInfo ci) {
        // 与原版 tryDetectPlayers 相同的 20 tick 节奏，保证不祥转换与试炼启动同拍
        if ((pos.asLong() + level.getGameTime()) % 20L != 0L) {
            return;
        }
        TrialSpawner self = (TrialSpawner) (Object) this;
        boolean has0721 = false;
        List<UUID> detected = self.getPlayerDetector()
                .detect(level, self.getEntitySelector(), pos, self.getRequiredPlayerRange(), true);
        for (UUID uuid : detected) {
            Player player = level.getPlayerByUUID(uuid);
            if (player == null) {
                continue;
            }
            if (player.hasEffect(ModEffects.EFFECT_0721_BAD_OMEN)) {
                // 镜像原版 transformBadOmenIntoTrialOmen：0721不祥之兆 → 0721试炼之兆
                // （时长 = 18000 * 等级，等级 = 放大器+1，新效果放大器归 0）
                MobEffectInstance badOmen = player.getEffect(ModEffects.EFFECT_0721_BAD_OMEN);
                int duration = 18000 * (badOmen.getAmplifier() + 1);
                player.removeEffect(ModEffects.EFFECT_0721_BAD_OMEN);
                player.addEffect(new MobEffectInstance(ModEffects.EFFECT_0721_TRIAL_OMEN, duration, 0));
                has0721 = true;
                break;
            }
            if (player.hasEffect(ModEffects.EFFECT_0721_TRIAL_OMEN)) {
                has0721 = true;
                break;
            }
        }
        if (has0721) {
            if (!self.isOminous()) {
                self.applyOminous(level, pos);
            }
            ((TrialSpawnerDataAccessor) self.getData()).yz0721$setTrial(true);
        } else if (!self.isOminous()) {
            // 未进入不祥状态且无 0721 玩家 → 清理旧标志（上一场 0721 试炼已结束）
            ((TrialSpawnerDataAccessor) self.getData()).yz0721$setTrial(false);
        }
    }
}
