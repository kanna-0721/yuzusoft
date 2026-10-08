package cn.autoforged.yuzusoft.effect.custom;

import cn.autoforged.yuzusoft.api.RaidAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;

/**
 * "0721不祥之兆" 效果。
 *
 * <p>饮用 0721 不祥之瓶获得。当持有者进入村庄且当前没有可升级的袭击时，
 * 立即（不走原版 RAID_OMEN 延时）创建一场标记为 0721 的袭击，然后移除本效果。
 * 触发节奏与 {@code BadOmenMobEffect} 一致：
 * {@code shouldApplyEffectTickThisTick} 恒 true；触发成功返回 false（效果被移除），
 * 未触发返回 true（效果按剩余时长继续计时）。
 */
public class Effect0721BadOmen extends MobEffect {

    public Effect0721BadOmen(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 与原版兆头互斥：持有 0721 不祥之兆期间，原版不祥之兆/试炼之兆会被持续清除
        // （覆盖饮用 0721 瓶后击杀巡逻队长等再次获得原版兆头的途径）
        entity.removeEffect(MobEffects.BAD_OMEN);
        entity.removeEffect(MobEffects.TRIAL_OMEN);
        if (entity instanceof ServerPlayer player && !player.isSpectator()) {
            ServerLevel level = player.serverLevel();
            if (level.getDifficulty() != Difficulty.PEACEFUL && level.isVillage(player.blockPosition())) {
                BlockPos pos = player.blockPosition();
                Raid raidAt = level.getRaidAt(pos);
                // 与原版 BadOmen 相同：村庄已有满级袭击时不重复触发
                if (raidAt == null || raidAt.getRaidOmenLevel() < raidAt.getMaxRaidOmenLevel()) {
                    Raids raids = level.getRaids();
                    Raid raid = raids.createOrExtendRaid(player, pos);
                    if (raid != null) {
                        // 标记为 0721 袭击（RaidMixin 据此替换怪物/英雄效果/血条名）
                        ((RaidAccessor) raid).yz0721$setRaid(true);
                        // 袭击等级 = 效果等级 + 1（0721不祥之瓶固定 0 级 → 1 级袭击），上限 5
                        raid.setRaidOmenLevel(Mth.clamp(amplifier + 1, 0, raid.getMaxRaidOmenLevel()));
                        if (!raid.hasFirstWaveSpawned()) {
                            player.awardStat(Stats.RAID_TRIGGER);
                            CriteriaTriggers.RAID_OMEN.trigger(player);
                        }
                        return false; // 触发成功，移除效果（与原版一致）
                    }
                }
            }
        }
        return true;
    }
}
