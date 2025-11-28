package co.xiaoyuboy.gui.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 日志管理器
 */
@Slf4j
public class LogManager {

    private static Consumer<String> logListener;

    public static void setLogListener(Consumer<String> listener) {
        logListener = listener;
    }

    public static void addLog(String type, String message) {
        // 记录到SLF4J
        log.info("{}: {}", type, message);

        // 通知监听器（使用新格式：类型|消息）
        if (logListener != null) {
            String logLine = type + "|" + message;
            logListener.accept(logLine);
        }
    }
}
