package cn.autoforged.yuzusoft.worldgen.spawner;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.entity.custom.ShadowAssassinEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Optional;

/**
 * 阴影刺客专属巡逻队生成器（与原版掠夺者 PatrolSpawner 并行的独立实现）。
 * 生成节奏完全复刻原版 PatrolSpawner：间隔 10~11 分钟、1/5 成功概率、
 * 仅白天且第 5 天之后、村庄附近不刷、禁刷群系不刷。
 */
public class ShadowAssassinPatrolSpawner implements CustomSpawner {

    private static final ResourceKey<BannerPattern> BANNER_0721 =
            ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath("yuzusoft", "0721"));

    /** 距离玩家多远的环带内尝试落点。 */
    private static final double MIN_RANGE = 24.0D;
    private static final double MAX_RANGE = 48.0D;
    /** 对每个玩家最多尝试的落点次数。 */
    private static final int MAX_SPAWN_TRIES = 8;

    /** 间隔计数器：每 tick -1，到 0 触发一次生成尝试（对齐原版初始 0）。 */
    private int nextTick = 0;

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!spawnEnemies) {
            return 0;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        // 复刻原版：每 tick -1，归零时以 10~11 分钟为间隔重置并尝试生成
        if (--this.nextTick > 0) {
            return 0;
        }
        this.nextTick = this.nextTick + 12000 + level.getRandom().nextInt(1200);

        // 仅白天且第 5 天之后才生成（与原版一致）
        if (level.getDayTime() / 24000L < 5L || !level.isDay()) {
            return 0;
        }
        // 每次尝试只有 1/5 概率真正生成（与原版一致）
        if (level.getRandom().nextInt(5) != 0) {
            return 0;
        }

        // 随机挑一个非旁观玩家（ServerLevel.players() 返回 List<ServerPlayer>）
        List<ServerPlayer> candidates = level.players().stream().filter(p -> !p.isSpectator()).toList();
        if (candidates.isEmpty()) {
            return 0;
        }
        ServerPlayer player = candidates.get(level.getRandom().nextInt(candidates.size()));

        // 村庄附近不刷（与原版一致）
        if (level.isCloseToVillage(player.blockPosition(), 2)) {
            return 0;
        }

        BlockPos spawnPos = getSpawningPos(level, player.getX(), player.getZ());
        if (spawnPos == null) {
            return 0;
        }

        // 禁刷群系不刷（与原版一致）
        if (level.getBiome(spawnPos).is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        return spawnPatrol(level, spawnPos, level.getRandom());
    }

    /** 在玩家周围 24~48 格内找一个地面有效落点；失败返回 null。 */
    private BlockPos getSpawningPos(ServerLevel level, double px, double pz) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < MAX_SPAWN_TRIES; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = MIN_RANGE + random.nextDouble() * (MAX_RANGE - MIN_RANGE);
            int x = (int) Math.floor(px + Math.cos(angle) * dist);
            int z = (int) Math.floor(pz + Math.sin(angle) * dist);

            BlockPos candidate = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            // 脚下方块必须是实心（可站立）
            if (!level.getBlockState(candidate.below()).isSolid()) {
                continue;
            }
            // 头身两格必须是非液体、非实心（不能卡在方块里）
            if (level.isWaterAt(candidate)
                    || level.getBlockState(candidate).isSolid()
                    || level.getBlockState(candidate.above()).isSolid()) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    /** 生成 1 名队长 + 2~4 名成员；返回本次成功抽中的数量。 */
    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        int spawned = 0;

        ShadowAssassinEntity leader = createAssassin(level, pos);
        if (leader != null) {
            leader.setPatrolLeader(true);
            leader.setItemSlot(EquipmentSlot.HEAD, make0721Banner(level));
            leader.setDropChance(EquipmentSlot.HEAD, 0.0F);
            spawned++;
        }

        int members = 2 + random.nextInt(3); // 2~4
        for (int i = 0; i < members; i++) {
            if (createAssassin(level, pos) != null) {
                spawned++;
            }
        }
        return spawned;
    }

    /**
     * 创建一个放置在给定位置的阴影刺客并加入世界；失败返回 null。
     * 注：NeoForge 21.1 尚无 EntitySpawnReason API（1.21.2+ 才有），
     * 这里用等价的 vanilla create(Level) + finalizeSpawn(MobSpawnType.PATROL) 写法。
     */
    private ShadowAssassinEntity createAssassin(ServerLevel level, BlockPos pos) {
        ShadowAssassinEntity assassin = ModEntities.SHADOW_ASSASSIN.get().create(level);
        if (assassin == null) {
            return null;
        }
        assassin.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        assassin.finalizeSpawn(level, level.getCurrentDifficultyAt(assassin.blockPosition()),
                MobSpawnType.PATROL, null);
        // 巡逻队成员持久化：不会因 removeWhenFarAway 自然消失
        assassin.setPersistenceRequired();
        level.addFreshEntity(assassin);
        return assassin;
    }

    /** 构造头戴的 0721 旗帜。 */
    private ItemStack make0721Banner(ServerLevel level) {
        ItemStack banner = new ItemStack(Items.WHITE_BANNER);
        Holder<BannerPattern> holder = level.registryAccess().holderOrThrow(BANNER_0721);
        banner.set(DataComponents.BANNER_PATTERNS,
                new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(holder, DyeColor.BLACK))));
        return banner;
    }
}