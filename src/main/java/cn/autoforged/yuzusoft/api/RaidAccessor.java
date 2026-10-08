package cn.autoforged.yuzusoft.api;

/**
 * 访问 {@code RaidMixin} 注入到 {@code Raid} 上的 "0721 袭击" 标志。
 *
 * <p>{@code RaidMixin} 声明 {@code implements RaidAccessor}，sponge-mixin
 * 会把本接口加进目标类 {@code Raid} 的接口列表并合并方法实现，因此普通代码
 * 可以直接强转调用：
 * <pre>{@code ((RaidAccessor) raid).yz0721$setRaid(true);}</pre>
 *
 * <p>注意：必须放在 mixin 包（{@code cn.autoforged.yuzusoft.mixin}）之外，
 * 否则 sponge-mixin 会以 IllegalClassLoadError 拒绝普通代码引用；本接口本身
 * 不注册进 mixins.json。
 */
public interface RaidAccessor {

    boolean yz0721$isRaid();

    void yz0721$setRaid(boolean raid);
}
