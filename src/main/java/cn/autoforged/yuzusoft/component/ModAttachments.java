package cn.autoforged.yuzusoft.component;

import cn.autoforged.yuzusoft.CycloneSwordMod;
import cn.autoforged.yuzusoft.head.FusionMerchant;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;

/**
 * 实体挂载数据。
 * <p>
 * 「无头躯体」不再是方块，而是被手术刀击杀的生物本体（冻结不动的真实实体）。
 * 该附件既当标记（存在即代表这是无头躯体），又当负载（原始死亡 NBT）：
 * <ul>
 *   <li>持久化 —— 区块重载后身体仍是无头状态，也能被拾回成物品；</li>
 *   <li>同步到客户端 —— 客户端渲染层据此隐藏模型头部。</li>
 * </ul>
 * 另有两项「融合生物」用的会话内数据（{@link #FUSION_BODY} / {@link #FUSION_MERCHANT}）。
 */
public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CycloneSwordMod.MODID);

    /** 值 = 该生物死亡瞬间的完整 NBT（{@link net.minecraft.world.entity.Entity#save} 产物）。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> DECAPITATED_BODY =
            ATTACHMENT_TYPES.register("decapitated_body",
                    () -> AttachmentType.builder(() -> (CompoundTag) null)
                            .serialize(CompoundTag.CODEC, tag -> tag != null)
                            .sync(ByteBufCodecs.COMPOUND_TAG)
                            .build());

    /**
     * 融合生物标记（true = 由头颅与身体拼出的融合体）。
     * <p>
     * 只做会话内标记：融合体的 AI 是运行时注入的 goal，本身也不随 NBT 保存，
     * 重载后融合体本就退回身体自身的 AI，故这里不额外持久化。
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FUSION_BODY =
            ATTACHMENT_TYPES.register("fusion_body",
                    () -> AttachmentType.builder(() -> Boolean.FALSE)
                            .sync(ByteBufCodecs.BOOL)
                            .build());

    /**
     * 融合生物「可骑乘」标记（true = 头部为乃爱等可骑乘生物，玩家可骑乘并操控）。
     * <p>
     * 需要同步：客户端据此判断该融合体能否被 R 键骑乘、以及能否用 X 键脱离。
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FUSION_RIDEABLE =
            ATTACHMENT_TYPES.register("fusion_rideable",
                    () -> AttachmentType.builder(() -> Boolean.FALSE)
                            .sync(ByteBufCodecs.BOOL)
                            .build());

    /**
     * 融合生物的主人（可驯服头部：惠 / 天音 / 来海）。
     * <p>
     * 融合体是既有实体类型，实现不了 {@code TamableAnimal}，只能用附件记录归属，
     * 供跟随 / 护主行为判断。与 {@link #FUSION_BODY} 一样只做会话内标记，不持久化、不同步。
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UUID>> FUSION_OWNER =
            ATTACHMENT_TYPES.register("fusion_owner",
                    () -> AttachmentType.builder(() -> (UUID) null).build());

    /** 融合生物的交易对象缓存：让报价的剩余次数在多次开关交易界面之间保持。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FusionMerchant>> FUSION_MERCHANT =
            ATTACHMENT_TYPES.register("fusion_merchant",
                    () -> AttachmentType.builder(() -> (FusionMerchant) null).build());

    /**
     * 隐身卡片激活标记（true = 隐身卡片正在生效）。
     * <p>
     * 只做会话内标记：卡面的附魔光效由物品组件（{@code ENCHANTMENT_GLINT_OVERRIDE}）同步，
     * 只要是同一玩家的库存改动，客户端自然会看到，无需再同步本值。
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> CARD_ACTIVE =
            ATTACHMENT_TYPES.register("invisibility_card_active",
                    () -> AttachmentType.builder(() -> Boolean.FALSE).build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}