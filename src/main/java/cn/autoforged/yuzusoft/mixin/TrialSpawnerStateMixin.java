package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.api.TrialSpawnerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0721 试炼结束时刷怪笼多吐一次战利品（不影响宝库）。
 *
 * <p>在状态机 {@link TrialSpawnerState#tickAndGetNext} 返回
 * ACTIVE → WAITING_FOR_REWARD_EJECTION 的瞬间（最后一只怪死亡、试炼战斗结束）：
 * 若为 0721 试炼，则从 {@code spawner.getConfig().lootTablesToEject()}
 * <b>独立随机再抽取一次</b>奖励表并 {@code ejectReward}——不是复用原版那次随机结果
 * 简单翻倍，因此可能吐出一份完全不同的奖励。原版后续的 EJECTING_REWARD 逐玩家
 * 吐出、不祥宝库开箱等流程一概不动。
 */
@Mixin(TrialSpawnerState.class)
public abstract class TrialSpawnerStateMixin {

    @Inject(method = "tickAndGetNext", at = @At("RETURN"))
    private void yuzusoft$extraRewardFor0721Trial(
            BlockPos pos, TrialSpawner spawner, ServerLevel level, CallbackInfoReturnable<TrialSpawnerState> cir) {
        TrialSpawnerState self = (TrialSpawnerState) (Object) this;
        if (self != TrialSpawnerState.ACTIVE || cir.getReturnValue() != TrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
            return;
        }
        if (!((TrialSpawnerDataAccessor) spawner.getData()).yz0721$isTrial()) {
            return;
        }
        spawner.getConfig().lootTablesToEject().getRandomValue(level.getRandom())
                .ifPresent(key -> spawner.ejectReward(level, pos, key));
    }
}
