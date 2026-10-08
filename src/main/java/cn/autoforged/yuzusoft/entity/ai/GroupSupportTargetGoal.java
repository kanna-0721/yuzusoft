package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

/**
 * 族群支援（低优先级，必须注册在玩家 / 铁傀儡 / 村民索敌目标之后）：
 * 附近有 0721 族员被非 0721 生物攻击时前往支援。
 *
 * <p>只有自己未锁定更高优先级目标（玩家 / 铁傀儡 / 村民）时才支援；
 * 目标一旦被更高优先级目标接管，立即放弃（canContinueToUse 校验当前目标仍是本次支援目标）。
 */
public class GroupSupportTargetGoal extends TargetGoal {

    /** 本次支援的目标（被打族员的攻击者）。 */
    private LivingEntity supportTarget;

    /** 族群成员判定；默认按 0721 族群。 */
    private final Predicate<LivingEntity> isMember;

    public GroupSupportTargetGoal(Mob mob) {
        this(mob, Group0721Helper::isGroup0721);
    }

    public GroupSupportTargetGoal(Mob mob, Predicate<LivingEntity> isMember) {
        super(mob, false);
        this.isMember = isMember;
    }

    @Override
    public boolean canUse() {
        // 已锁定更高优先级目标（玩家/铁傀儡/村民）时不支援
        if (this.mob.getTarget() != null) {
            return false;
        }
        this.supportTarget = this.findHurtAlly();
        return this.supportTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || target != this.supportTarget || !target.isAlive()
                || this.isMember.test(target)) {
            return false;
        }
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        return target.distanceToSqr(this.mob) <= followRange * followRange;
    }

    @Override
    public void start() {
        if (this.supportTarget != null) {
            this.mob.setTarget(this.supportTarget);
        }
    }

    @Override
    public void stop() {
        // 只清自己锁定的支援目标，不覆盖更高优先级目标刚接管的指向
        if (this.mob.getTarget() == this.supportTarget) {
            this.mob.setTarget(null);
        }
        this.supportTarget = null;
    }

    /** 扫描附近正在被非 0721 生物攻击的族群成员，返回攻击者；没有则返回 null。 */
    private LivingEntity findHurtAlly() {
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB area = AABB.unitCubeFromLowerCorner(this.mob.position())
                .inflate(followRange, 10.0D, followRange);
        for (Mob candidate : this.mob.level().getEntitiesOfClass(Mob.class, area,
                m -> m != this.mob && this.isMember.test(m))) {
            LivingEntity attacker = candidate.getLastHurtByMob();
            if (attacker != null && attacker != candidate && attacker.isAlive()
                    && !this.isMember.test(attacker)
                    && !Group0721Helper.isIgnoredByGroup(attacker)) {
                return attacker;
            }
        }
        return null;
    }
}
