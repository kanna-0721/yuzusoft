package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.api.RaidAccessor;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 0721 袭击专用：禁止外部袭击者中途加入。
 *
 * <p>原版 {@link Raider#aiStep()} 允许村庄附近任何 {@code canJoinRaid=true} 的袭击者
 * （原版掠夺者巡逻队、哨站掠夺者、女巫小屋女巫、自然刷怪等）自动加入进行中的袭击，
 * 且加入时保持**原样类型**，不经过 {@link Raid#spawnGroup} 的替换。
 * 这会导致 0721 袭击里混入原版掠夺者/卫道士/唤魔者（"没替换完全"），
 * 同时原版巡逻队与本模组自然刷出的 0721 生物也一并入袭，使袭击总数超过原版。
 *
 * <p>本 Mixin 对已标记为 0721 的袭击（{@link RaidAccessor#yz0721$isRaid()}）
 * 直接拒绝一切外部袭击者加入：袭击内怪物完全由袭击波次生成（已全部替换为
 * 0721 三兄弟），数量与原版完全一致；非 0721 袭击保持原版行为。
 */
@Mixin(Raider.class)
public abstract class RaiderMixin {

    @Redirect(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/raid/Raids;canJoinRaid(Lnet/minecraft/world/entity/raid/Raider;Lnet/minecraft/world/entity/raid/Raid;)Z"))
    private boolean yz0721$blockJoinFor0721Raid(Raider raider, Raid raid) {
        if (raid instanceof RaidAccessor accessor && accessor.yz0721$isRaid()) {
            return false;
        }
        return Raids.canJoinRaid(raider, raid);
    }
}
