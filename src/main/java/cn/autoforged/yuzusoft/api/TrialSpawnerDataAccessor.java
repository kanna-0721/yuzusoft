package cn.autoforged.yuzusoft.api;

/**
 * 访问 {@code TrialSpawnerDataMixin} 注入到 {@code TrialSpawnerData} 上的 "0721 试炼" 标志。
 *
 * <p>{@code TrialSpawnerDataMixin} 声明 {@code implements TrialSpawnerDataAccessor}，
 * sponge-mixin 会把本接口加进目标类 {@code TrialSpawnerData} 的接口列表并合并方法实现，
 * 因此普通代码可以直接强转调用：
 * <pre>{@code ((TrialSpawnerDataAccessor) spawner.getData()).yz0721$isTrial();}</pre>
 *
 * <p>注意：必须放在 mixin 包（{@code cn.autoforged.yuzusoft.mixin}）之外，
 * 否则 sponge-mixin 会以 IllegalClassLoadError 拒绝普通代码引用；本接口本身
 * 不注册进 mixins.json。
 */
public interface TrialSpawnerDataAccessor {

    boolean yz0721$isTrial();

    void yz0721$setTrial(boolean trial);
}
