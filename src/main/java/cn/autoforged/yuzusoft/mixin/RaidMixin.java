package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.api.RaidAccessor;
import cn.autoforged.yuzusoft.effect.ModEffects;
import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0721 袭击核心 Mixin（挂 {@link Raid}）。
 *
 * <p>仅在 {@code yz0721$raid} 标志为 true 的袭击上生效（标志由
 * {@link Effect0721BadOmen} 触发时写入并经 NBT 持久化）：
 * <ul>
 *   <li>{@code spawnGroup} 的 4 处 {@code EntityType.create}：掠夺者→ShadowAssassin、
 *       卫道士→BloodSuckerZombie、唤魔者→SprinklerCreep（女巫/劫掠兽及骑手分支数量、
 *       比例与原版完全一致）；非 0721 袭击完全走原版。</li>
 *   <li>{@code tick} 胜利后把 村庄英雄 换成 0721英雄（时长/等级不变）。</li>
 *   <li>{@code tick} 里全部袭击血条名读取换成 "0721袭击"。</li>
 * </ul>
 */
@Mixin(Raid.class)
public abstract class RaidMixin implements RaidAccessor {

    /** 是否为 0721 袭击。 */
    @Unique
    private boolean yz0721$raid;

    /** 原版袭击名（私有静态，@Shadow 直读）。 */
    @Shadow
    private static Component RAID_NAME_COMPONENT;

    @Override
    public boolean yz0721$isRaid() {
        return this.yz0721$raid;
    }

    @Override
    public void yz0721$setRaid(boolean raid) {
        this.yz0721$raid = raid;
    }

    // ------------------------------------------------------------------
    // 生成替换：spawnGroup 内 4 处 EntityType.create(Level)
    // ordinal 0 = 主循环（各 RaiderType），1 = 劫掠兽骑手 PILLAGER，
    // 2 = 骑手 EVOKER，3 = 骑手 VINDICATOR（与字节码顺序一致）。
    // ------------------------------------------------------------------
    @Redirect(method = "spawnGroup",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity yz0721$create0(EntityType<?> type, Level level) {
        return yz0721$createRaidMob(type, level);
    }

    @Redirect(method = "spawnGroup",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity yz0721$create1(EntityType<?> type, Level level) {
        return yz0721$createRaidMob(type, level);
    }

    @Redirect(method = "spawnGroup",
            at = @At(value = "INVOKE", ordinal = 2,
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity yz0721$create2(EntityType<?> type, Level level) {
        return yz0721$createRaidMob(type, level);
    }

    @Redirect(method = "spawnGroup",
            at = @At(value = "INVOKE", ordinal = 3,
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity yz0721$create3(EntityType<?> type, Level level) {
        return yz0721$createRaidMob(type, level);
    }

    /** 仅 0721 袭击时把掠夺者/卫道士/唤魔者换成 0721 三兄弟，其余类型与普通袭击走原版。 */
    @Unique
    private Entity yz0721$createRaidMob(EntityType<?> type, Level level) {
        if (!this.yz0721$raid) {
            return type.create(level);
        }
        EntityType<?> replacement = null;
        if (type == EntityType.PILLAGER) {
            replacement = ModEntities.SHADOW_ASSASSIN.get();
        } else if (type == EntityType.VINDICATOR) {
            replacement = ModEntities.BLOOD_SUCKER_ZOMBIE.get();
        } else if (type == EntityType.EVOKER) {
            replacement = ModEntities.SPRINKLER_CREEP.get();
        } else if (type == EntityType.WITCH) {
            replacement = ModEntities.EVIL_NANAMI.get();
        }
        return (replacement != null ? replacement : type).create(level);
    }

    // ------------------------------------------------------------------
    // 胜利英雄效果：tick() 里唯一一处读取 MobEffects.HERO_OF_THE_VILLAGE
    // （静态字段 GET，处理器零参数；与 RAID_NAME_COMPONENT 同一约定。
    // 注意：不可用 @At("NEW") 重定向 MobEffectInstance 构造器——NeoForge 的
    // MixinExtras 会自动把 NEW 重定向包成 FactoryRedirectWrapper 并导致
    // "failed injection check (0/1)" 崩溃，故改为重定向字段读取。）
    // ------------------------------------------------------------------
    @Redirect(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/effect/MobEffects;HERO_OF_THE_VILLAGE:Lnet/minecraft/core/Holder;"))
    private Holder<MobEffect> yz0721$heroHolder() {
        return this.yz0721$raid ? ModEffects.EFFECT_0721_HERO : MobEffects.HERO_OF_THE_VILLAGE;
    }

    // ------------------------------------------------------------------
    // 袭击血条名：tick() 里所有 RAID_NAME_COMPONENT 读取（静态字段 GET，
    // 处理器零参数；无 ordinal = 命中该方法内全部 4 处读取）
    // ------------------------------------------------------------------
    @Redirect(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/entity/raid/Raid;RAID_NAME_COMPONENT:Lnet/minecraft/network/chat/Component;"))
    private Component yz0721$raidName() {
        return this.yz0721$raid ? Component.translatable("event.yuzusoft.raid0721") : RAID_NAME_COMPONENT;
    }

    // ------------------------------------------------------------------
    // 持久化：标志随袭击存档写入/恢复
    // ------------------------------------------------------------------
    @Inject(method = "save", at = @At("TAIL"))
    private void yz0721$saveFlag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        tag.putBoolean("YZ0721Raid", this.yz0721$raid);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void yz0721$loadFlag(ServerLevel level, CompoundTag tag, CallbackInfo ci) {
        this.yz0721$raid = tag.getBoolean("YZ0721Raid");
    }
}
