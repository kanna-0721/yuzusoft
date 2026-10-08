package cn.autoforged.yuzusoft.head;

import cn.autoforged.yuzusoft.entity.ModEntities;
import cn.autoforged.yuzusoft.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 「头部音效表」：融合生物照抄头部对应原生物的音效。
 * <p>
 * 融合体的实体类型来自<b>身体</b>，其 {@code getAmbientSound/getHurtSound/getDeathSound}
 * 返回的都是身体的音效；组装时身体已被静音（见 {@link FusionAssembler}），改由
 * {@link cn.autoforged.yuzusoft.event.FusionSoundEvents} 按 HEAD 槽里的头颅解析头部类型、
 * 播这里的音效。
 * <p>
 * 音效来源与各原生物实体自身的 {@code getXxxSound} 保持一致（原版头取原版音效）。
 */
public final class FusionSounds {
    private FusionSounds() {}

    /** 一组头部音效（可空 = 该生物没有此类音效，如苦力怕没有环境音）。 */
    private record Trio(Supplier<SoundEvent> ambient, Supplier<SoundEvent> hurt, Supplier<SoundEvent> death) {}

    private static final Map<EntityType<?>, Trio> BY_HEAD = new HashMap<>();
    private static boolean bound = false;

    private static void ensureBound() {
        if (bound) {
            return;
        }
        bound = true;

        // ---- 21 只柚子社头 ----
        bind(ModEntities.GUARDIAN.get(),
                ModSounds.GUARDIAN_AMBIENT, ModSounds.GUARDIAN_HURT, ModSounds.GUARDIAN_DEATH);
        bind(ModEntities.GUARDIAN_TRADER.get(),
                ModSounds.GUARDIANS_AMBIENT, ModSounds.GUARDIANS_HURT, ModSounds.GUARDIANS_DEATH);
        bind(ModEntities.SHADOW_ASSASSIN.get(),
                ModSounds.SHADOW_ASSASSIN_AMBIENT, ModSounds.SHADOW_ASSASSIN_HURT, ModSounds.SHADOW_ASSASSIN_DEATH);
        bind(ModEntities.DETONATOR_THROWING_MONSTER.get(),
                ModSounds.DETONATOR_MONSTER_AMBIENT, ModSounds.DETONATOR_MONSTER_HURT, ModSounds.DETONATOR_MONSTER_DEATH);
        bind(ModEntities.FROST_GUARDIAN.get(),
                ModSounds.FROST_GUARDIAN_AMBIENT, ModSounds.FROST_GUARDIAN_HURT, ModSounds.FROST_GUARDIAN_DEATH);
        bind(ModEntities.FROST_GUARDIAN_V2.get(),
                ModSounds.FROST_GUARDIAN_V2_AMBIENT, ModSounds.FROST_GUARDIAN_V2_HURT, ModSounds.FROST_GUARDIAN_V2_DEATH);
        bind(ModEntities.FLOATING_SENTINEL.get(),
                ModSounds.FLOATING_SENTINEL_AMBIENT, ModSounds.FLOATING_SENTINEL_HURT, ModSounds.FLOATING_SENTINEL_DEATH);
        bind(ModEntities.VILLAGE_GUARDIAN.get(),
                ModSounds.VILLAGE_GUARDIAN_AMBIENT, ModSounds.VILLAGE_GUARDIAN_HURT, ModSounds.VILLAGE_GUARDIAN_DEATH);
        bind(ModEntities.SPRINKLER_CREEP.get(),
                ModSounds.SPRINKLER_CREEP_AMBIENT, ModSounds.SPRINKLER_CREEP_HURT, ModSounds.SPRINKLER_CREEP_DEATH);
        bind(ModEntities.GUITAR_MONSTER.get(),
                ModSounds.GUITAR_MONSTER_AMBIENT, ModSounds.GUITAR_MONSTER_HURT, ModSounds.GUITAR_MONSTER_DEATH);
        bind(ModEntities.SLEEPY_SPIRIT.get(),
                ModSounds.SLEEPY_SPIRIT_AMBIENT, ModSounds.SLEEPY_SPIRIT_HURT, ModSounds.SLEEPY_SPIRIT_DEATH);
        bind(ModEntities.FLASHBANG_MONSTER.get(),
                ModSounds.FLASHBANG_MONSTER_AMBIENT, ModSounds.FLASHBANG_MONSTER_HURT, ModSounds.FLASHBANG_MONSTER_DEATH);
        bind(ModEntities.HAMMER_WIELDER.get(),
                ModSounds.HAMMER_WIELDER_IDLE, ModSounds.HAMMER_WIELDER_HURT, ModSounds.HAMMER_WIELDER_DEATH);
        bind(ModEntities.BLOOD_SUCKER_ZOMBIE.get(),
                ModSounds.BLOOD_SUCKER_ZOMBIE_AMBIENT, ModSounds.BLOOD_SUCKER_ZOMBIE_HURT, ModSounds.BLOOD_SUCKER_ZOMBIE_DEATH);
        bind(ModEntities.CAT_0721.get(),
                () -> SoundEvents.CAT_STRAY_AMBIENT, () -> SoundEvents.CAT_HURT, () -> SoundEvents.CAT_DEATH);
        bind(ModEntities.DUAL_FORM_MOB.get(),
                ModSounds.DUALFORMMOB_AMBIENT, ModSounds.DUALFORMMOB_HURT, ModSounds.DUALFORMMOB_DEATH);
        bind(ModEntities.HUMANOID_CREATURE.get(),
                ModSounds.HUMANOID_CREATURE_AMBIENT, ModSounds.HUMANOID_CREATURE_HURT, ModSounds.HUMANOID_CREATURE_DEATH);
        bind(ModEntities.SUZUNE.get(),
                ModSounds.SUZUNE_AMBIENT, ModSounds.SUZUNE_HURT, ModSounds.SUZUNE_DEATH);
        bind(ModEntities.DIAMOND_GUARDIAN.get(),
                ModSounds.DIAMOND_GUARDIAN_AMBIENT, ModSounds.DIAMOND_GUARDIAN_HURT, ModSounds.DIAMOND_GUARDIAN_DEATH);
        // 恶七海复用七海的贴图与音效
        bind(ModEntities.EVIL_NANAMI.get(),
                ModSounds.VILLAGE_GUARDIAN_AMBIENT, ModSounds.VILLAGE_GUARDIAN_HURT, ModSounds.VILLAGE_GUARDIAN_DEATH);
        bind(ModEntities.WATER_SPIRIT.get(),
                ModSounds.WATER_SPIRIT_AMBIENT, ModSounds.WATER_SPIRIT_HURT, ModSounds.WATER_SPIRIT_DEATH);

        // ---- 原版头颅（也可能被手术刀安到别的身体上）----
        bind(EntityType.ZOMBIE,
                () -> SoundEvents.ZOMBIE_AMBIENT, () -> SoundEvents.ZOMBIE_HURT, () -> SoundEvents.ZOMBIE_DEATH);
        bind(EntityType.SKELETON,
                () -> SoundEvents.SKELETON_AMBIENT, () -> SoundEvents.SKELETON_HURT, () -> SoundEvents.SKELETON_DEATH);
        // 苦力怕没有环境音
        bind(EntityType.CREEPER,
                () -> null, () -> SoundEvents.CREEPER_HURT, () -> SoundEvents.CREEPER_DEATH);
        bind(EntityType.PIGLIN,
                () -> SoundEvents.PIGLIN_AMBIENT, () -> SoundEvents.PIGLIN_HURT, () -> SoundEvents.PIGLIN_DEATH);
    }

    private static void bind(EntityType<?> head, Supplier<SoundEvent> ambient,
                             Supplier<SoundEvent> hurt, Supplier<SoundEvent> death) {
        BY_HEAD.put(head, new Trio(ambient, hurt, death));
    }

    /** 按 HEAD 槽里的头颅物品解析头部实体类型；头顶不是手术刀头颅时返回 null。 */
    @Nullable
    public static EntityType<?> headTypeOf(LivingEntity entity) {
        return MobHeadRegistry.entityFor(entity.getItemBySlot(EquipmentSlot.HEAD).getItem());
    }

    /** 播头部对应原生物的环境音效。 */
    public static void playAmbient(LivingEntity entity) {
        play(entity, soundOf(entity, Kind.AMBIENT));
    }

    /** 播头部对应原生物的受击音效。 */
    public static void playHurt(LivingEntity entity) {
        play(entity, soundOf(entity, Kind.HURT));
    }

    /** 播头部对应原生物的死亡音效。 */
    public static void playDeath(LivingEntity entity) {
        play(entity, soundOf(entity, Kind.DEATH));
    }

    private enum Kind { AMBIENT, HURT, DEATH }

    @Nullable
    private static SoundEvent soundOf(LivingEntity entity, Kind kind) {
        ensureBound();
        EntityType<?> head = headTypeOf(entity);
        if (head == null) {
            return null;
        }
        Trio trio = BY_HEAD.get(head);
        if (trio == null) {
            return null;
        }
        return switch (kind) {
            case AMBIENT -> trio.ambient().get();
            case HURT -> trio.hurt().get();
            case DEATH -> trio.death().get();
        };
    }

    private static void play(LivingEntity entity, @Nullable SoundEvent sound) {
        if (sound == null) {
            return;
        }
        // 直接走 level.playSound 绕过实体自身的静音开关（身体已被 setSilent）
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                sound, entity.getSoundSource(), 1.0F, entity.getVoicePitch());
    }
}