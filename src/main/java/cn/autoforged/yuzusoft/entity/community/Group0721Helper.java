package cn.autoforged.yuzusoft.entity.community;

import cn.autoforged.yuzusoft.component.ModAttachments;
import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.head.MobHeadRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * "0721生物" 族群判定。四个族群成员彼此是盟友（不互相攻击），但仍是敌对怪物，
 * 照常攻击玩家与铁傀儡。通过各实体覆写 isAlliedTo(Entity) 接入，
 * NearestAttackableTargetGoal 与 HurtByTargetGoal 会自动把同族目标排除。
 *
 * <p>融合生物（身体 + 0721 系头颅）也算族员：其 {@code isAlliedTo} 定义在身体类上、
 * 运行时改不了，但可以让族群的判定把「顶着 0721 头颅的融合体」一并算作自己人，
 * 这样族员不会打它、也会支援它；融合体自身的索敌同样按此判定排除族员。
 */
public class Group0721Helper {

    private Group0721Helper() {
    }

    /** 判断某个活体是否属于 0721 族群（含顶着 0721 系头颅的融合生物）。 */
    public static boolean isGroup0721(LivingEntity entity) {
        return entity != null
                && (isGroup0721Type(entity.getType()) || hasGroup0721Head(entity));
    }

    /** 该实体类型本身是否为 0721 族员。 */
    public static boolean isGroup0721Type(EntityType<?> type) {
        return type == ModEntities.SHADOW_ASSASSIN.get()
                || type == ModEntities.BLOOD_SUCKER_ZOMBIE.get()
                || type == ModEntities.SPRINKLER_CREEP.get()
                || type == ModEntities.DETONATOR_THROWING_MONSTER.get()
                || type == ModEntities.CAT_0721.get()
                || type == ModEntities.EVIL_NANAMI.get();
    }

    /** 该头部生物类型是否属于 0721 族群（融合体据此决定阵营与目标表）。 */
    public static boolean isGroup0721Head(EntityType<?> headType) {
        return headType != null && isGroup0721Type(headType);
    }

    /** 融合生物且头部是 0721 系头颅：视作族员。 */
    public static boolean hasGroup0721Head(LivingEntity entity) {
        if (!Boolean.TRUE.equals(entity.getData(ModAttachments.FUSION_BODY.get()))) {
            return false;
        }
        EntityType<?> headType = MobHeadRegistry.entityFor(entity.getItemBySlot(EquipmentSlot.HEAD).getItem());
        return headType != null && isGroup0721Type(headType);
    }

    /** 两个活体是否都属于本族群且不是同一个体。 */
    public static boolean areAllied(LivingEntity a, LivingEntity b) {
        return a != b && isGroup0721(a) && isGroup0721(b);
    }

    /** 0721 生物完全无视的目标：创造模式玩家（无敌管理员，不索敌、不因它触发支援）。 */
    public static boolean isIgnoredByGroup(LivingEntity entity) {
        return entity instanceof Player player && player.isCreative();
    }
}