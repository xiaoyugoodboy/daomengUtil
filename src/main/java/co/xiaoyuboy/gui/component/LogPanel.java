package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 日志面板组件
 */
public class LogPanel extends VBox {

    private final TextArea logArea;

    public LogPanel() {
        setPadding(new Insets(20));
        setSpacing(10);
        getStyleClass().add("log-panel");

        Label headerLabel = new Label("📋 运行日志");
        headerLabel.getStyleClass().add("panel-header");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-area");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        getChildren().addAll(headerLabel, logArea);

        // 监听日志变化
        LogManager.setLogListener(this::appendLog);
    }

    private void appendLog(String log) {
        Platform.runLater(() -> {
            logArea.appendText(log + "\n");
            // 自动滚动到底部
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
}
