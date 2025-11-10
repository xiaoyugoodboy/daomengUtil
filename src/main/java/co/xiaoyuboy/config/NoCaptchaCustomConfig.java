package co.xiaoyuboy.config;

/**
 * 默认的无验证码自定义参数配置，方便后续手动调整。
 */
public final class NoCaptchaCustomConfig {

    private NoCaptchaCustomConfig() {
    }

    public static final long SUBMIT_COUNT = 5L;
    public static final long INTERVAL_MS = 2L;
    public static final long LEAD_TIME_MS = 0L;
}
