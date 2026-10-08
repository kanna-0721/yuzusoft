package cn.autoforged.yuzusoft.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * 阴影刺客（ShadowAssassin）专属刷怪判定。
 *
 * NeoForge 1.21 起原版静态 {@code SpawnPlacements.register(...)} 已被移除，
 * 所有刷怪放置统一在 MOD 总线的 {@link net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent}
 * 里登记（本项目见 event/EntityAttributeHandler.onRegisterSpawnPlacements）。
 * 因此本类只提供"单独刷出必须亮度=0"的判定，由 EntityAttributeHandler 用
 * Operation.REPLACE 注册到 ShadowAssassin 上。
 *
 * 该判据不影响巡逻小队——巡逻队由 ShadowAssassinPatrolSpawner 手动生成，不经过 SpawnPlacements 判定。
 */
public class ModSpawnPlacements {

    private static boolean registered = false;

    /** 0721 前哨站结构 key。结构 spawn_overrides 的持续刷新以 MobSpawnType.NATURAL 触发判据，
     *  故需在判据内按"位置是否在哨站内"来放行亮度限制。 */
    private static final ResourceKey<Structure> OUTPOST_0721 =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("yuzusoft", "outpost_0721"));

    private ModSpawnPlacements() {
    }

    /**
     * 防重入的注册占位。实际注册在 RegisterSpawnPlacementsEvent 中完成，
     * 此处仅保留 static boolean 防重入结构，避免重复登记。
     */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
    }

    /**
     * ShadowAssassin 专属刷怪判定。
     * 结构 spawn_overrides 的持续刷新以 MobSpawnType.NATURAL 触发本判据（NaturalSpawner 写死 NATURAL），
     * 因此"哨站内无视亮度持续刷新"必须靠位置检测：位于 yuzusoft:outpost_0721 结构内时直接放行（可站立不卡墙）；
     * 试炼刷怪笼（TRIAL_SPAWNER）直接放行。
     * 其余自然来源保留"暗处突袭"定位：亮度为 0，且只在地表露天、可站立不卡墙处刷新。
     */
    public static boolean checkShadowAssassinSpawnRules(
            EntityType<? extends Monster> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (spawnType == MobSpawnType.TRIAL_SPAWNER) {
            return true;
        }
        // 结构决定落点的来源（含持续刷新的 NATURAL，位置在哨站内）：无视亮度，但落点需可站立不卡墙
        if (spawnType == MobSpawnType.STRUCTURE || isInOutpost0721(level, pos)) {
            return isStandableSpot(level, pos);
        }
        // 自然来源：暗处突袭（亮度==0）保留，同时只在地表露天、可站立不卡墙处刷新
        return isExposedGroundSpot(level, pos)
                && level.getRawBrightness(pos, 0) == 0
                && Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }

    /** 位置是否位于 yuzusoft:outpost_0721 结构（任一 piece）内。 */
    public static boolean isInOutpost0721(ServerLevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure outpost = structureRegistry.getHolderOrThrow(OUTPOST_0721).value();
        return serverLevel.structureManager().getStructureAt(pos, outpost).isValid();
    }

    /** 落点可站立：脚下实心、身体两格可通行（防止卡进墙缝）。 */
    private static boolean isStandableSpot(ServerLevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid()
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }

    /** 露天可站立地表：能直视天空，且满足 isStandableSpot。 */
    private static boolean isExposedGroundSpot(ServerLevelAccessor level, BlockPos pos) {
        return level.canSeeSky(pos) && isStandableSpot(level, pos);
    }
}