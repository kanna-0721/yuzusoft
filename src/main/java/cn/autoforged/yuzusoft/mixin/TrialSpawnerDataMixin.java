package cn.autoforged.yuzusoft.mixin;

import cn.autoforged.yuzusoft.api.TrialSpawnerDataAccessor;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0721 试炼刷怪笼的实体替换（仅 0721 试炼生效）。
 *
 * <p>通过 {@link TrialSpawnerDataAccessor#yz0721$isTrial()} 判断当前试炼是否为 0721 试炼
 * （标志由 {@link TrialSpawnerMixin} 在检测到携带 0721 不祥之兆的玩家时置位）：
 * <ul>
 *   <li>{@link TrialSpawnerData#getOrCreateNextSpawnData} 是服务端实际刷怪
 *       与客户端旋转展示共用的入口，RETURN 处重写 "id" 标签可修正真实刷出的怪；
 *       非 0721 试炼（原版不祥 / 普通）完全走原版。</li>
 *   <li>{@link TrialSpawnerData#getUpdateTag} 是发给客户端的同步数据，0721 试炼时
 *       把其中的实体 id 一并替换，使刷怪笼内的旋转展示与实际刷出的 0721 怪一致。</li>
 * </ul>
 */
@Mixin(TrialSpawnerData.class)
public abstract class TrialSpawnerDataMixin implements TrialSpawnerDataAccessor {

    private static final String SPRINKLER_CREEP_ID = "yuzusoft:akizuki_kanna";

    /** vanilla 实体 id -> 本模组自定义实体 id。以后要替换其它刷怪笼生物，只需在此加一行。 */
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "minecraft:zombie", "yuzusoft:yarai_miu",
            "minecraft:skeleton", "yuzusoft:hitachi_mako",
            "minecraft:bogged", "yuzusoft:nabari_anju",
            "minecraft:slime", "yuzusoft:mitsukasa_ayase",
            "minecraft:husk", "yuzusoft:yarai_miu",
            "minecraft:stray", "yuzusoft:shikibe_mayu",
            "minecraft:cave_spider", "yuzusoft:harumi_ena",
            "minecraft:silverfish", SPRINKLER_CREEP_ID
    );

    /** 是否为 0721 试炼（仅服务端置位；重启/区块卸载后由 TrialSpawnerMixin 重新探测）。 */
    @Unique
    private boolean yz0721$trial;

    @Override
    public boolean yz0721$isTrial() {
        return this.yz0721$trial;
    }

    @Override
    public void yz0721$setTrial(boolean trial) {
        this.yz0721$trial = trial;
    }

    /**
     * 替换实体 id；SprinklerCreep 强制矿工形态（0721 试炼只出手持下界合金镐的变种，
     * 由 SprinklerCreepEntity.readAdditionalSaveData 保证主手物品一致）。
     */
    @Unique
    private static void yz0721$applyTrialReplacement(CompoundTag entityTag) {
        String vanillaId = entityTag.getString("id");
        String replacement = REPLACEMENTS.get(vanillaId);
        if (replacement != null) {
            entityTag.putString("id", replacement);
            if (SPRINKLER_CREEP_ID.equals(replacement)) {
                entityTag.putBoolean("MinerForm", true);
            }
        }
    }

    @Inject(method = "getOrCreateNextSpawnData", at = @At("RETURN"))
    private void yuzusoft$replaceTrialSpawnerEntity(
            TrialSpawner spawner, RandomSource random, CallbackInfoReturnable<SpawnData> cir) {
        if (!this.yz0721$trial) {
            return;
        }
        yz0721$applyTrialReplacement(cir.getReturnValue().entityToSpawn());
    }

    @Inject(method = "getUpdateTag", at = @At("TAIL"))
    private void yuzusoft$syncDisplayEntity(TrialSpawnerState state, CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.yz0721$trial) {
            return;
        }
        CompoundTag tag = cir.getReturnValue();
        if (tag.contains("spawn_data")) {
            yz0721$applyTrialReplacement(tag.getCompound("spawn_data").getCompound("entity"));
        }
    }
}
