package co.xiaoyuboy.gui.util;

import co.xiaoyuboy.config.RuntimeConfig.ModeType;
import co.xiaoyuboy.entity.Activity;

/**
 * 活动提交配置（包含活动和提交模式）
 */
public class ActivitySubmitConfig {
    private final Activity activity;
    private final ModeType mode;
    private final long submitCount;
    private final long intervalMs;
    private final long leadTimeMs;

    public ActivitySubmitConfig(Activity activity, ModeType mode, long submitCount, long intervalMs, long leadTimeMs) {
        this.activity = activity;
        this.mode = mode;
        this.submitCount = submitCount;
        this.intervalMs = intervalMs;
        this.leadTimeMs = leadTimeMs;
    }

    // 使用全局配置
    public static ActivitySubmitConfig fromGlobal(Activity activity) {
        return new ActivitySubmitConfig(
            activity,
            GuiConfigManager.getGlobalMode(),
            GuiConfigManager.getGlobalSubmitCount(),
            GuiConfigManager.getGlobalIntervalMs(),
            GuiConfigManager.getGlobalLeadTimeMs()
        );
    }

    public Activity getActivity() {
        return activity;
    }

    public ModeType getMode() {
        return mode;
    }

    public long getSubmitCount() {
        return submitCount;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public long getLeadTimeMs() {
        return leadTimeMs;
    }
}
