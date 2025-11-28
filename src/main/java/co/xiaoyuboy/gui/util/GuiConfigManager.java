package co.xiaoyuboy.gui.util;

import co.xiaoyuboy.config.RuntimeConfig.ModeType;

/**
 * GUI全局配置管理
 */
public class GuiConfigManager {

    private static ModeType globalMode = ModeType.NO_CAPTCHA_DEFAULT;
    private static long globalSubmitCount = 5;
    private static long globalIntervalMs = 2;
    private static long globalLeadTimeMs = 0;

    public static ModeType getGlobalMode() {
        return globalMode;
    }

    public static void setGlobalMode(ModeType mode) {
        globalMode = mode;
    }

    public static long getGlobalSubmitCount() {
        return globalSubmitCount;
    }

    public static void setGlobalSubmitCount(long count) {
        globalSubmitCount = count;
    }

    public static long getGlobalIntervalMs() {
        return globalIntervalMs;
    }

    public static void setGlobalIntervalMs(long interval) {
        globalIntervalMs = interval;
    }

    public static long getGlobalLeadTimeMs() {
        return globalLeadTimeMs;
    }

    public static void setGlobalLeadTimeMs(long leadTime) {
        globalLeadTimeMs = leadTime;
    }

    /**
     * 一次性设置全局配置
     */
    public static void setGlobalConfig(ModeType mode, long submitCount, long intervalMs, long leadTimeMs) {
        globalMode = mode;
        globalSubmitCount = submitCount;
        globalIntervalMs = intervalMs;
        globalLeadTimeMs = leadTimeMs;
    }

    /**
     * 获取模式描述
     */
    public static String getModeDescription() {
        switch (globalMode) {
            case NO_CAPTCHA_DEFAULT:
                return "无验证码默认 (提交" + globalSubmitCount + "次, 间隔" + globalIntervalMs + "ms)";
            case NO_CAPTCHA_CUSTOM:
                return "无验证码自定义 (提交" + globalSubmitCount + "次, 间隔" + globalIntervalMs + "ms, 提前" + globalLeadTimeMs + "ms)";
            case CAPTCHA:
                return "验证码识别 (提交" + globalSubmitCount + "次, 间隔" + globalIntervalMs + "ms, 提前" + globalLeadTimeMs + "ms)";
            default:
                return "未知模式";
        }
    }
}
