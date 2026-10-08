package cn.autoforged.yuzusoft.charm.ai;

import cn.autoforged.yuzusoft.charm.CharmHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 契约进攻目标之一：攻击"正在攻击主人"的生物。
 * 参照原版 OwnerHurtByTargetGoal，主人从驯服动物改为契约玩家。
 */
public class CharmOwnerHurtByTargetGoal extends TargetGoal {
    private final Mob mob;
    @Nullable
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public CharmOwnerHurtByTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!CharmHelper.hasCharmEffect(this.mob)) {
            return false;
        }
        Player owner = CharmHelper.resolveOwner(this.mob);
        if (owner == null) {
            return false;
        }
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int i = owner.getLastHurtByMobTimestamp();
        return i != this.timestamp
                && CharmHelper.isAllowedCharmTarget(this.mob, this.ownerLastHurtBy)
                && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        Player owner = CharmHelper.resolveOwner(this.mob);
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
