package cn.autoforged.yuzusoft.transformation;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.network.payload.SyncTransformationPayload;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 变身系统的服务端核心：应用 / 撤销变身形态，并把形态同步给客户端。
 * <p>
 * 并入自独立模组 {@code mod_92152f0a}（变身法杖），仅包名与模组 id 改为 yuzusoft。
 */
public final class TransformationSystem {
    private static final String CURRENT = "transformation_current";
    private static final ResourceLocation HEALTH = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "health");
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "speed");
    private static final ResourceLocation ARMOR = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "armor");
    private static final ResourceLocation KNOCKBACK = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "knockback");
    private static final ResourceLocation ATTACK = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "attack");
    private static final ResourceLocation SCALE = ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "scale");
    /** 提供护甲值的四个槽位。 */
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private TransformationSystem() {}

    public static boolean hasTransformation(Player player) { return player.getPersistentData().contains(CURRENT); }
    public static String current(Player player) { return player.getPersistentData().getString(CURRENT); }

    /** 当前变身生物类型；未变身或 id 无效时返回 null。 */
    public static EntityType<?> currentType(Player player) {
        String id = current(player);
        return id.isEmpty() ? null : EntityType.byString(id).orElse(null);
    }

    public static boolean transform(ServerLevel level, Player player, String id) {
        if (id.equals("minecraft:player")) {
            revert(player);
            return true;
        }
        EntityType.byString(id).ifPresent(type -> apply(level, player, type, id));
        return id.equals(current(player));
    }

    public static void revert(Player player) {
        revertInternal(player);
        sync(player);
    }

    /** 向玩家本人及跟踪他的其他玩家同步当前变身形态（用于客户端渲染）。 */
    public static void sync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        String id = current(serverPlayer);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
                new SyncTransformationPayload(serverPlayer.getUUID(), id.isEmpty() ? "minecraft:player" : id));
    }

    private static void revertInternal(Player player) {
        if (player instanceof ServerPlayer serverPlayer) TransformationCombat.reclaimWeapon(serverPlayer);
        remove(player, Attributes.MAX_HEALTH, HEALTH);
        remove(player, Attributes.MOVEMENT_SPEED, SPEED);
        remove(player, Attributes.ARMOR, ARMOR);
        remove(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK);
        remove(player, Attributes.ATTACK_DAMAGE, ATTACK);
        remove(player, Attributes.SCALE, SCALE);
        player.getPersistentData().remove(CURRENT);
        player.refreshDimensions();
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    private static void apply(ServerLevel level, Player player, EntityType<?> type, String id) {
        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity target)) return;
        revertInternal(player);
        addDifference(player, target, Attributes.MAX_HEALTH, HEALTH);
        addDifference(player, target, Attributes.MOVEMENT_SPEED, SPEED);
        addArmorDifference(player, target);
        addDifference(player, target, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK);
        addAttackDifference(player, target);
        AttributeInstance sourceScale = player.getAttribute(Attributes.SCALE);
        if (sourceScale != null) {
            // 缩放以「视高比」为准，保证矮小生物（蜘蛛、蝙蝠等）视角真的降低；
            // revertInternal 已把玩家复位，故此时 getEyeHeight() 是未变身的 1.62。
            double baseEye = Math.max(0.1D, player.getEyeHeight());
            double targetEye = target.getDimensions(target.getPose()).eyeHeight();
            double scale = Math.max(0.35D, Math.min(3.0D, targetEye / baseEye));
            sourceScale.addOrUpdateTransientModifier(new AttributeModifier(SCALE, scale - 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
        player.getPersistentData().putString(CURRENT, id);
        if (player instanceof ServerPlayer serverPlayer) TransformationCombat.grantWeapon(serverPlayer, type);
        player.setHealth(player.getMaxHealth());
        player.refreshDimensions();
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 32, .5, 1, .5, .1);
        player.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1, 1.2f);
        sync(player);
        clearStaleTargets(player, type);
    }

    /** 变身后，清理周围怪物对该玩家已存在的锁定（新形态本不该被它们攻击时）。 */
    private static void clearStaleTargets(Player player, EntityType<?> disguised) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64.0D))) {
            if (mob.getTarget() == player && !TransformationTargeting.wouldTarget(mob, disguised, level)) {
                mob.setTarget(null);
            }
        }
    }

    private static void addDifference(Player player, LivingEntity target, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance source = player.getAttribute(attribute);
        AttributeInstance wanted = target.getAttribute(attribute);
        if (source == null || wanted == null) return;
        double difference = wanted.getValue() - source.getValue();
        if (Math.abs(difference) > .0001) source.addOrUpdateTransientModifier(new AttributeModifier(id, difference, AttributeModifier.Operation.ADD_VALUE));
    }

    /**
     * 攻击力：变身后取形态攻击力，但不低于原版空手 1 点。
     * <p>
     * 否则变身成攻击力为 0 的生物（如乃爱）后，空手攻击伤害会变成 0，导致完全打不动。
     */
    private static void addAttackDifference(Player player, LivingEntity target) {
        AttributeInstance source = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance wanted = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (source == null || wanted == null) return;
        double value = Math.max(wanted.getValue(), 1.0D);
        double difference = value - source.getValue();
        if (Math.abs(difference) > .0001) {
            source.addOrUpdateTransientModifier(new AttributeModifier(ATTACK, difference, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void remove(Player player, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    /**
     * 护甲：最终护甲 = 变身形态护甲 + 玩家穿戴装备提供的护甲。
     * <p>
     * 不能用 {@link #addDifference}——玩家的 ARMOR 总值里**已经包含**装备护甲，
     * 直接取差值会把装备贡献整个减掉（原 bug：变身后穿甲不算护甲值）。
     * 这里先把装备贡献从总值里剔除得到玩家自身护甲，再让差值只补上"形态护甲 - 玩家自身护甲"，
     * 于是穿在身上的护甲会与形态护甲叠加；装备之后增减也是实时的（装备修正始终生效）。
     */
    private static void addArmorDifference(Player player, LivingEntity target) {
        AttributeInstance source = player.getAttribute(Attributes.ARMOR);
        AttributeInstance wanted = target.getAttribute(Attributes.ARMOR);
        if (source == null || wanted == null) return;
        double equipment = equipmentArmor(player);
        double difference = wanted.getValue() - (source.getValue() - equipment);
        if (Math.abs(difference) > .0001) {
            source.addOrUpdateTransientModifier(new AttributeModifier(ARMOR, difference, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** 汇总玩家四个护甲槽位上装备（含其附魔属性效果）提供的 ARMOR 值。 */
    private static double equipmentArmor(Player player) {
        double[] sum = {0.0D};
        Holder<Attribute> armor = Attributes.ARMOR;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            stack.forEachModifier(slot, (attribute, modifier) -> {
                if (attribute.value() == armor.value()
                        && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    sum[0] += modifier.amount();
                }
            });
        }
        return sum[0];
    }
}