package cn.autoforged.yuzusoft.sound;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CycloneSwordMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIAN_AMBIENT = SOUND_EVENTS.register(
            "guardian_ambient",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guardian_ambient"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIAN_HURT = SOUND_EVENTS.register(
            "guardian_hurt",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guardian_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIAN_DEATH = SOUND_EVENTS.register(
            "guardian_death",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guardian_death"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIAN_ATTACK = SOUND_EVENTS.register(
            "guardian_attack",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guardian_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_ASSASSIN_AMBIENT =
            SOUND_EVENTS.register("shadow_assassin_ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "shadow_assassin_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_ASSASSIN_HURT =
            SOUND_EVENTS.register("shadow_assassin_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "shadow_assassin_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_ASSASSIN_DEATH =
            SOUND_EVENTS.register("shadow_assassin_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "shadow_assassin_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_DART_THROW =
            SOUND_EVENTS.register("shadow_dart_throw",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "shadow_dart_throw")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_IDLE =
            register("guardian_trader_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_HURT =
            register("guardian_trader_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_DEATH =
            register("guardian_trader_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_ANGRY =
            register("guardian_trader_angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_TRADE =
            register("guardian_trader_trade");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIANS_AMBIENT =
            register("guardian_trader_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DETONATOR_MONSTER_AMBIENT =
            SOUND_EVENTS.register("entity.detonator_monster.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.detonator_monster.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DETONATOR_MONSTER_HURT =
            SOUND_EVENTS.register("entity.detonator_monster.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.detonator_monster.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DETONATOR_MONSTER_DEATH =
            SOUND_EVENTS.register("entity.detonator_monster.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.detonator_monster.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_AMBIENT =
            SOUND_EVENTS.register("entity.frost_guardian.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.frost_guardian.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_HURT =
            SOUND_EVENTS.register("entity.frost_guardian.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.frost_guardian.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_DEATH =
            SOUND_EVENTS.register("entity.frost_guardian.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.frost_guardian.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLOATING_SENTINEL_AMBIENT =
            SOUND_EVENTS.register("entity.floating_sentinel.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.floating_sentinel.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLOATING_SENTINEL_HURT =
            SOUND_EVENTS.register("entity.floating_sentinel.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.floating_sentinel.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLOATING_SENTINEL_DEATH =
            SOUND_EVENTS.register("entity.floating_sentinel.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.floating_sentinel.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGE_GUARDIAN_AMBIENT = SOUND_EVENTS.register(
            "village_guardian_ambient",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "village_guardian_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGE_GUARDIAN_HURT = SOUND_EVENTS.register(
            "village_guardian_hurt",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "village_guardian_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGE_GUARDIAN_DEATH = SOUND_EVENTS.register(
            "village_guardian_death",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "village_guardian_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLER_CREEP_AMBIENT =
            SOUND_EVENTS.register("entity.sprinkler_creep.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.sprinkler_creep.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLER_CREEP_HURT =
            SOUND_EVENTS.register("entity.sprinkler_creep.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.sprinkler_creep.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLER_CREEP_DEATH =
            SOUND_EVENTS.register("entity.sprinkler_creep.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.sprinkler_creep.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLER_SHOOT =
            SOUND_EVENTS.register("item.sprinkler_shoot",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "item.sprinkler_shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_HIT =
            SOUND_EVENTS.register("item.water_hit",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "item.water_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_MONSTER_ATTACK = SOUND_EVENTS.register(
            "guitar_monster.attack", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_monster.attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_WEAPON_RANGED = SOUND_EVENTS.register(
            "guitar_weapon.ranged", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_weapon.ranged")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_MONSTER_CHARGE = SOUND_EVENTS.register(
            "guitar_monster.charge", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_monster.charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_MONSTER_AMBIENT = SOUND_EVENTS.register(
            "guitar_monster.ambient", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_monster.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_MONSTER_HURT = SOUND_EVENTS.register(
            "guitar_monster.hurt", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_monster.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GUITAR_MONSTER_DEATH = SOUND_EVENTS.register(
            "guitar_monster.death", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "guitar_monster.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLEEPY_SPIRIT_AMBIENT =
            SOUND_EVENTS.register("sleepy_spirit.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "sleepy_spirit.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLEEPY_SPIRIT_HURT =
            SOUND_EVENTS.register("sleepy_spirit.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "sleepy_spirit.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLEEPY_SPIRIT_DEATH =
            SOUND_EVENTS.register("sleepy_spirit.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "sleepy_spirit.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_SLEEP =
            register("sleepy_spirit_sleep");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_GIFT =
            register("sleepy_spirit_gift");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLASHBANG_MONSTER_AMBIENT =
            SOUND_EVENTS.register("entity.flashbang_monster.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.flashbang_monster.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLASHBANG_MONSTER_HURT =
            SOUND_EVENTS.register("entity.flashbang_monster.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.flashbang_monster.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLASHBANG_MONSTER_DEATH =
            SOUND_EVENTS.register("entity.flashbang_monster.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.flashbang_monster.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WIELDER_IDLE = registerSound("hammer_wielder_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WIELDER_HURT = registerSound("hammer_wielder_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WIELDER_DEATH = registerSound("hammer_wielder_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WIELDER_ATTACK = registerSound("hammer_wielder_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WIELDER_STEP = registerSound("hammer_wielder_step");

    public static final DeferredHolder<SoundEvent, SoundEvent> STUN_APPLY = registerSound("stun_apply");
    public static final DeferredHolder<SoundEvent, SoundEvent> STUN_EXPIRE = registerSound("stun_expire");

    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_SLAM = registerSound("hammer_slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOOD_SUCKER_ZOMBIE_AMBIENT =
            SOUND_EVENTS.register("entity.blood_sucker_zombie.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.blood_sucker_zombie.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOOD_SUCKER_ZOMBIE_HURT =
            SOUND_EVENTS.register("entity.blood_sucker_zombie.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.blood_sucker_zombie.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOOD_SUCKER_ZOMBIE_DEATH =
            SOUND_EVENTS.register("entity.blood_sucker_zombie.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.blood_sucker_zombie.death")));

    // DualFormMob (from noa project)
    public static final DeferredHolder<SoundEvent, SoundEvent> DUALFORMMOB_AMBIENT =
            SOUND_EVENTS.register("entity.dual_form_mob.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.dual_form_mob.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUALFORMMOB_HURT =
            SOUND_EVENTS.register("entity.dual_form_mob.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.dual_form_mob.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUALFORMMOB_DEATH =
            SOUND_EVENTS.register("entity.dual_form_mob.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.dual_form_mob.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUALFORMMOB_ANGRY =
            SOUND_EVENTS.register("entity.dual_form_mob.angry",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, "entity.dual_form_mob.angry")));

    // HumanoidCreature (from tameable_creature_mod)
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_AMBIENT =
            register("entity.humanoid_creature.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_HURT =
            register("entity.humanoid_creature.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_DEATH =
            register("entity.humanoid_creature.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_EAT =
            register("entity.humanoid_creature.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_TAME_SUCCESS =
            register("entity.humanoid_creature.tame_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUMANOID_CREATURE_TAME_FAIL =
            register("entity.humanoid_creature.tame_fail");

    // ---- 冰霜守卫 V2（由 mayu 工程并入）----
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_AMBIENT = registerSound("frost_guardian_v2_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_HURT = registerSound("frost_guardian_v2_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_DEATH = registerSound("frost_guardian_v2_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_ATTACK = registerSound("frost_guardian_v2_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_SKILL = registerSound("frost_guardian_v2_skill");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_GUARDIAN_V2_MODE_SWITCH = registerSound("frost_guardian_v2_mode_switch");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_BARRIER_CARD_USE = registerSound("frost_barrier_card_use");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_BARRIER_HUM = registerSound("frost_barrier_hum");

    // Suzune（凉音）自爆人形生物
    public static final DeferredHolder<SoundEvent, SoundEvent> SUZUNE_AMBIENT = registerSound("entity.suzune.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUZUNE_ANGRY = registerSound("entity.suzune.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUZUNE_HURT = registerSound("entity.suzune.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUZUNE_DEATH = registerSound("entity.suzune.death");

    // ---- 来海（kurumi 工程并入）----
    public static final DeferredHolder<SoundEvent, SoundEvent> DIAMOND_GUARDIAN_AMBIENT = registerSound("entity.diamond_guardian.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIAMOND_GUARDIAN_HURT = registerSound("entity.diamond_guardian.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIAMOND_GUARDIAN_DEATH = registerSound("entity.diamond_guardian.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIAMOND_GUARDIAN_TAME = registerSound("entity.diamond_guardian.tame");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIAMOND_GUARDIAN_ALERT = registerSound("entity.diamond_guardian.alert");

    // ---- 水灵（J 工程并入）----
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_AMBIENT = registerSound("water_spirit.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_HURT = registerSound("water_spirit.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_DEATH = registerSound("water_spirit.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_TAKE = registerSound("water_spirit.take");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_ABSORB = registerSound("water_spirit.absorb");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_ANGRY = registerSound("water_spirit.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_SHOOT = registerSound("water_spirit.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPIRIT_DENY = registerSound("water_spirit.deny");

    // ---- 岛越月望（PianoNeoForge 工程并入）----
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIMAGOE_IDLE = registerSound("entity.shimagoe_tsukumi.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIMAGOE_ANGRY = registerSound("entity.shimagoe_tsukumi.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIMAGOE_HURT = registerSound("entity.shimagoe_tsukumi.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIMAGOE_DEATH = registerSound("entity.shimagoe_tsukumi.death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(CycloneSwordMod.MODID, name)));
    }
}
