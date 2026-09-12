package cn.autoforged.yuzusoft.mixin;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the vanilla trial-spawner zombie with the mod's BloodSuckerZombieEntity.
 *
 * <p>The spot is {@link TrialSpawnerData#getOrCreateNextSpawnData}, which is the shared entry
 * point used both by {@link TrialSpawner#spawnMob} (server-side actual spawning) and by
 * {@link TrialSpawnerData#getOrCreateDisplayEntity} (client-side spinning display model).
 * Rewriting the "id" tag here fixes BOTH the spawned mob and the model shown inside the
 * spawner. All other tags (most importantly "IsBaby") are preserved as-is, so the vanilla
 * zombie and its baby variant both map to the matching blood-sucker form.
 *
 * <p>To replace more trial-spawner mobs, just add an entry to {@link #REPLACEMENTS}.
 * Each entry maps a vanilla entity id to the mod's custom entity id; the same entry covers
 * both the actual spawn (server) and the spinning preview model (client).
 */
@Mixin(TrialSpawnerData.class)
public abstract class TrialSpawnerDataMixin {

    /** vanilla 实体 id -> 本模组自定义实体 id。以后要替换其它刷怪笼生物，只需在此加一行。 */
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "minecraft:zombie", "yuzusoft:blood_sucker_zombie",
            "minecraft:skeleton", "yuzusoft:shadow_assassin",
            "minecraft:bogged", "yuzusoft:flashbang_monster",
            "minecraft:spider", "yuzusoft:guitar_monster",
            "minecraft:slime", "yuzusoft:floating_sentinel",
            "minecraft:husk", "yuzusoft:blood_sucker_zombie",
            "minecraft:stray", "yuzusoft:frost_guardian_v2",
            "minecraft:cave_spider", "yuzusoft:guitar_monster"
    );

    @Inject(method = "getOrCreateNextSpawnData", at = @At("RETURN"))
    private void yuzusoft$replaceTrialSpawnerEntity(
            TrialSpawner spawner, RandomSource random, CallbackInfoReturnable<SpawnData> cir) {
        CompoundTag tag = cir.getReturnValue().entityToSpawn();
        String vanillaId = tag.getString("id");
        String replacement = REPLACEMENTS.get(vanillaId);
        if (replacement != null) {
            tag.putString("id", replacement);
        }
    }
}