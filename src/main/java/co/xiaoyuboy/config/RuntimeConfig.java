package co.xiaoyuboy.config;

/**
 * 管理运行过程中的模式、队列及验证码设置.
 */
public final class RuntimeConfig {

    private RuntimeConfig() {
    }

    public enum ModeType {
        NO_CAPTCHA_DEFAULT,
        NO_CAPTCHA_CUSTOM,
        CAPTCHA
    }

    private static final QueueSettings QUEUE_SETTINGS = new QueueSettings();
    private static final CaptchaSettings CAPTCHA_SETTINGS = new CaptchaSettings();
    private static ModeType modeType = ModeType.NO_CAPTCHA_DEFAULT;
    private static boolean retryOnFailure = false;
    private static boolean captchaEnabled = false;

    public static ModeType getModeType() {
        return modeType;
    }

    public static void setModeType(ModeType modeType) {
        RuntimeConfig.modeType = modeType;
        captchaEnabled = modeType == ModeType.CAPTCHA;
        retryOnFailure = captchaEnabled;
    }

    public static QueueSettings getQueueSettings() {
        return QUEUE_SETTINGS;
    }

    public static void updateQueueSettings(long submitCount, long intervalMs, long leadTimeMs) {
        QUEUE_SETTINGS.setSubmitCount(submitCount);
        QUEUE_SETTINGS.setIntervalMs(intervalMs);
        QUEUE_SETTINGS.setLeadTimeMs(leadTimeMs);
    }

    public static boolean isQueueConfigured() {
        return QUEUE_SETTINGS.getSubmitCount() > 0
                && QUEUE_SETTINGS.getIntervalMs() >= 0
                && QUEUE_SETTINGS.getLeadTimeMs() >= 0;
    }

    public static boolean isCaptchaEnabled() {
        return captchaEnabled;
    }

    public static boolean shouldRetryOnFailure() {
        return retryOnFailure;
    }

    public static CaptchaSettings getCaptchaSettings() {
        return CAPTCHA_SETTINGS;
    }

    public static class QueueSettings {
        private long submitCount = 0;
        private long intervalMs = 0;
        private long leadTimeMs = 0;

        public long getSubmitCount() {
            return submitCount;
        }

        public void setSubmitCount(long submitCount) {
            this.submitCount = submitCount;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        public long getLeadTimeMs() {
            return leadTimeMs;
        }

        public void setLeadTimeMs(long leadTimeMs) {
            this.leadTimeMs = leadTimeMs;
        }
    }

    public static class CaptchaSettings {
        private String predictUrl = "http://127.0.0.1:5000/predict";
        private int maxAttempt = 15;

        public String getPredictUrl() {
            return predictUrl;
        }

        public void setPredictUrl(String predictUrl) {
            this.predictUrl = predictUrl;
        }

        public int getMaxAttempt() {
            return maxAttempt;
        }

        public void setMaxAttempt(int maxAttempt) {
            this.maxAttempt = maxAttempt;
        }
    }
}
