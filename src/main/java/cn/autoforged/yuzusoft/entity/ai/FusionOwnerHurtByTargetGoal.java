package cn.autoforged.yuzusoft.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import javax.annotation.Nullable;

/**
 * 融合生物的护主：照抄 {@code OwnerHurtByTargetGoal}——
 * 主人被别的生物打了，就去打那个攻击者。只对已驯服（有主人）的融合体生效。
 */
public class FusionOwnerHurtByTargetGoal extends TargetGoal {
    private final Mob mob;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private LivingEntity attacker;

    public FusionOwnerHurtByTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        LivingEntity found = FusionTameHelper.ownerOf(this.mob);
        if (found == null) {
            return false;
        }
        LivingEntity hurtBy = found.getLastHurtByMob();
        if (hurtBy == null || hurtBy == this.mob || !hurtBy.isAlive()) {
            return false;
        }
        this.owner = found;
        this.attacker = hurtBy;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && this.owner.isAlive()
                && this.attacker != null && this.attacker.isAlive()
                && this.mob.getTarget() == this.attacker;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
    }

    @Override
    public void stop() {
        this.attacker = null;
        this.owner = null;
        this.mob.setTarget(null);
    }
}