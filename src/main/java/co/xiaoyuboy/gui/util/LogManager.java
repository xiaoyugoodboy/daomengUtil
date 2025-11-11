package co.xiaoyuboy.gui.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * 日志管理器
 */
@Slf4j
public class LogManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Consumer<String> logListener;

    public static void setLogListener(Consumer<String> listener) {
        logListener = listener;
    }

    public static void addLog(String type, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logLine = String.format("[%s] [%s] %s", timestamp, type, message);

        // 记录到SLF4J
        log.info("{}: {}", type, message);

        // 通知监听器
        if (logListener != null) {
            logListener.accept(logLine);
        }
    }
}
