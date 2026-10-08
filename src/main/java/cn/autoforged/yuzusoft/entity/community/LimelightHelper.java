package cn.autoforged.yuzusoft.entity.community;

import cn.autoforged.yuzusoft.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * "limelight生物" 族群判定：阳见惠凪（harumi_ena）、隐杏珠（nabari_anju）、
 * 岛越月望（shimagoe_tsukumi）、二见原莉莉子（futamihara_ririko）。
 *
 * <p>成员彼此为盟友：不会因近战、远程弹射物或范围攻击互相误伤
 * （见 {@code LimelightFriendlyFireHandler} 与 {@code ShimagoeTsukumi.canBeHitByPiano}），
 * 且其中一员被非本族生物攻击时，其余成员会前往支援
 * （见 {@code GroupSupportTargetGoal} / {@code GroupHurtByTargetGoal}）。</p>
 */
public class LimelightHelper {

    private LimelightHelper() {
    }

    /** 判断某个活体是否属于 limelight 族群。 */
    public static boolean isLimelight(LivingEntity entity) {
        return entity != null && isLimelightType(entity.getType());
    }

    /** 该实体类型是否为 limelight 族员。 */
    public static boolean isLimelightType(EntityType<?> type) {
        return type == ModEntities.GUITAR_MONSTER.get()
                || type == ModEntities.FLASHBANG_MONSTER.get()
                || type == ModEntities.SHIMAGOE_TSUKUMI.get()
                || type == ModEntities.SLEEPY_SPIRIT.get();
    }

    /** 两个活体是否都属于本族群且不是同一个体。 */
    public static boolean areAllied(LivingEntity a, LivingEntity b) {
        return a != b && isLimelight(a) && isLimelight(b);
    }
}