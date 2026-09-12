package cn.autoforged.yuzusoft.entity.ai;

import cn.autoforged.yuzusoft.entity.custom.HumanoidCreatureEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.LivingEntity;
import java.util.EnumSet;

public class AllyHurtTargetGoal extends TargetGoal {
    private final HumanoidCreatureEntity mob;
    private LivingEntity target;

    public AllyHurtTargetGoal(HumanoidCreatureEntity mob) {
        super(mob, true);
        this.mob = mob;
        // 标记该AI占用目标槽位
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    // 判断是否可以启动该AI
    @Override
    public boolean canUse() {
        LivingEntity lastHurtByMob = mob.getLastHurtByMob();
        // 过滤无效目标：无攻击者、目标死亡、和平模式
        if (lastHurtByMob == null || !lastHurtByMob.isAlive() || mob.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        // 不打自己的主人
        if (mob.getOwner() == lastHurtByMob) {
            return false;
        }
        // 不打主人旗下其它驯服宠物（友军误伤防护）
        if (lastHurtByMob instanceof net.minecraft.world.entity.TamableAnimal other && other.isTame()
                && mob.getOwnerUUID() != null && mob.getOwnerUUID().equals(other.getOwnerUUID())) {
            return false;
        }

        this.target = lastHurtByMob;
        return true;
    }

    // AI启动时设置实体仇恨目标
    @Override
    public void start() {
        mob.setTarget(this.target);
        super.start();
    }
}