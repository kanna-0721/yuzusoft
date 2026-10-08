package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.component.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 融合生物的「主人」读写：可驯服的头部（惠 / 天音 / 来海）驯服后把玩家 UUID
 * 记在 {@link ModAttachments#FUSION_OWNER} 上，跟随 / 护主行为据此判断归属。
 * <p>
 * 与 {@code TamableAnimal} 不同：融合生物是既有实体类型，加不了新接口，
 * 只能用附件做会话内的归属标记。
 */
public final class FusionTameHelper {
    private FusionTameHelper() {}

    /** 融合生物当前的主人；未驯服或主人不在线返回 {@code null}。 */
    @Nullable
    public static LivingEntity ownerOf(Mob mob) {
        UUID id = mob.getExistingDataOrNull(ModAttachments.FUSION_OWNER);
        if (id == null) {
            return null;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return null;
        }
        Player player = level.getServer().getPlayerList().getPlayer(id);
        return player;
    }

    public static void setOwner(Mob mob, Player player) {
        mob.setData(ModAttachments.FUSION_OWNER.get(), player.getUUID());
    }

    /** 是否已驯服（有归属）。 */
    public static boolean isTamed(Mob mob) {
        return mob.getExistingDataOrNull(ModAttachments.FUSION_OWNER) != null;
    }
}