package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.community.Group0721Helper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * 族群反击（高优先级，通常为 1）：只有"自己被打"的 0721 生物才反击攻击者。
 *
 * <p>不拉全族、不报警。其余族员的支援改由低优先级的
 * {@link GroupSupportTargetGoal} 承担——只有未锁定玩家 / 铁傀儡 / 村民目标时才前往。
 */
public class GroupHurtByTargetGoal extends TargetGoal {

    /** 族群成员判定；默认按 0721 族群。 */
    private final Predicate<LivingEntity> isMember;

    public GroupHurtByTargetGoal(Mob mob) {
        this(mob, Group0721Helper::isGroup0721);
    }

    public GroupHurtByTargetGoal(Mob mob, Predicate<LivingEntity> isMember) {
        super(mob, true);
        this.isMember = isMember;
        // 与原版 HurtByTargetGoal 一致：占用目标槽位，避免同优先级的
        // NearestAttackableTargetGoal 并行运行、反过来把反击目标清掉。
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        // 自己被非 0721 生物攻击 → 反击（创造模式玩家被完全无视）
        LivingEntity attacker = this.mob.getLastHurtByMob();
        return attacker != null && attacker != this.mob && attacker.isAlive()
                && !this.isMember.test(attacker)
                && !Group0721Helper.isIgnoredByGroup(attacker);
    }

    @Override
    public boolean canContinueToUse() {
        // 注意：不能用 getLastHurtByMob() 作为续战条件——原版会在最后一次受伤 100 tick 后
        // 自动清空它（LivingEntity.tick），会把手握远程武器的生物在射出第一发后直接掐断仇恨。
        // 这里与原版 HurtByTargetGoal 一样，只校验目标本身是否还有效。
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || this.isMember.test(target)) {
            return false;
        }
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        return target.distanceToSqr(this.mob) <= followRange * followRange;
    }

    @Override
    public void start() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker != null) {
            this.mob.setTarget(attacker);
        }
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
    }
}
