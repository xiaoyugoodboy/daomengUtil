package co.xiaoyuboy.util;

import java.util.Date;
import java.util.logging.*;

/**
 * @Author: Smile
 * @Date: 2024-04-02 10:35
 * @Description:
 */
public class LogConfigurator {
    public static void configure() {
        // 自定义Formatter类
        Formatter myFormatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$tF %1$tT.%1$tL %2$-7s %3$s %n",
                        new Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
                        formatMessage(record));
            }
        };

        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();

        if (handlers.length == 0) {
            // 没有默认的控制台处理器，则添加一个
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO); // 设置日志级别
            consoleHandler.setFormatter(myFormatter); // 设置自定义Formatter
            rootLogger.addHandler(consoleHandler);
        } else {
            // 如果已经存在默认的控制台处理器则替换其Formatter
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    handler.setFormatter(myFormatter);
                    handler.setLevel(Level.INFO); // 设置日志级别
                }
            }
        }

        rootLogger.setLevel(Level.INFO); // 设置日志级别
    }
    public static void main(String[] args) {
        configure();

        // 以下是测试代码
        Logger logger = Logger.getLogger(LogConfigurator.class.getName());
        logger.info("日志消息会包括毫秒值");
    }
}
