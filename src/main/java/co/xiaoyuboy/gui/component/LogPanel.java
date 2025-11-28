package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志面板组件 - 彩色日志显示
 */
public class LogPanel extends VBox {

    private final TextFlow logFlow;
    private final ScrollPane scrollPane;
    private boolean autoScroll = true;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public LogPanel() {
        getStyleClass().add("log-panel");
        setPadding(new Insets(25, 20, 20, 20));
        setSpacing(12);

        // 标题区域
        HBox headerBox = createHeader();

        // 日志内容区域（使用 TextFlow 支持多色文本）
        logFlow = new TextFlow();
        logFlow.setPadding(new Insets(15));
        logFlow.setStyle(
            "-fx-background-color: rgba(29, 29, 31, 0.95);" +
            "-fx-background-radius: 12px;"
        );

        scrollPane = new ScrollPane(logFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle(
            "-fx-background: rgba(29, 29, 31, 0.95);" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: rgba(0, 0, 0, 0.2);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: innershadow(gaussian, rgba(0, 0, 0, 0.5), 8, 0, 0, 2);"
        );
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(headerBox, scrollPane);

        // 监听日志更新
        LogManager.setLogListener(this::updateLog);
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // 左侧标题
        VBox titleBox = new VBox(5);

        HBox titleHeaderBox = new HBox(8);
        titleHeaderBox.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label("📊");
        headerIcon.setStyle("-fx-font-size: 18px;");

        Label headerLabel = new Label("实时日志");
        headerLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: #1d1d1f;"
        );

        titleHeaderBox.getChildren().addAll(headerIcon, headerLabel);

        Label subtitleLabel = new Label("查看程序运行状态");
        subtitleLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #86868b;"
        );

        titleBox.getChildren().addAll(titleHeaderBox, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 右侧按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // 自动滚动开关
        ToggleButton autoScrollBtn = new ToggleButton("自动滚动");
        autoScrollBtn.setSelected(true);
        autoScrollBtn.setStyle(
            "-fx-background-color: rgba(52, 199, 89, 0.12);" +
            "-fx-text-fill: #34c759;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 6px 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(52, 199, 89, 0.3);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;"
        );
        autoScrollBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autoScroll = newVal;
            if (newVal) {
                autoScrollBtn.setStyle(
                    "-fx-background-color: rgba(52, 199, 89, 0.12);" +
                    "-fx-text-fill: #34c759;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: rgba(52, 199, 89, 0.3);" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;"
                );
            } else {
                autoScrollBtn.setStyle(
                    "-fx-background-color: rgba(229, 229, 234, 0.6);" +
                    "-fx-text-fill: #86868b;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: rgba(0, 0, 0, 0.1);" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;"
                );
            }
        });

        // 清空按钮
        Button clearBtn = new Button("🗑️ 清空");
        clearBtn.setStyle(
            "-fx-background-color: rgba(255, 59, 48, 0.12);" +
            "-fx-text-fill: #ff3b30;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 6px 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(255, 59, 48, 0.3);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;"
        );
        clearBtn.setOnMouseEntered(e -> {
            clearBtn.setStyle(
                "-fx-background-color: rgba(255, 59, 48, 0.2);" +
                "-fx-text-fill: #ff3b30;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: rgba(255, 59, 48, 0.4);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;"
            );
        });
        clearBtn.setOnMouseExited(e -> {
            clearBtn.setStyle(
                "-fx-background-color: rgba(255, 59, 48, 0.12);" +
                "-fx-text-fill: #ff3b30;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: rgba(255, 59, 48, 0.3);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;"
            );
        });
        clearBtn.setOnAction(e -> clearLogs());

        buttonBox.getChildren().addAll(autoScrollBtn, clearBtn);

        headerBox.getChildren().addAll(titleBox, spacer, buttonBox);
        return headerBox;
    }

    private void updateLog(String log) {
        Platform.runLater(() -> {
            // 解析日志类型和内容
            String[] parts = log.split("\\|", 2);
            String type = parts.length > 1 ? parts[0].trim() : "";
            String content = parts.length > 1 ? parts[1].trim() : log;

            // 添加时间戳
            String time = LocalTime.now().format(timeFormatter);
            Text timeText = new Text("[" + time + "] ");
            timeText.setStyle(
                "-fx-fill: #666666;" +
                "-fx-font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;" +
                "-fx-font-size: 11px;"
            );

            // 根据类型设置颜色
            Text contentText = new Text(content + "\n");
            String color = getColorForType(type);
            contentText.setStyle(
                "-fx-fill: " + color + ";" +
                "-fx-font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;" +
                "-fx-font-size: 12px;"
            );

            logFlow.getChildren().addAll(timeText, contentText);

            // 自动滚动到底部
            if (autoScroll) {
                scrollPane.setVvalue(1.0);
            }
        });
    }

    private String getColorForType(String type) {
        return switch (type.toLowerCase()) {
            case "错误", "error", "失败" -> "#ff3b30";  // 红色
            case "警告", "warn", "warning" -> "#ff9500"; // 橙色
            case "成功", "success", "✓", "✅" -> "#34c759";  // 绿色
            case "配置", "config", "设置" -> "#007aff";  // 蓝色
            case "活动", "检查", "切换" -> "#00d4ff";  // 青色
            case "提交", "submit" -> "#af52de";  // 紫色
            case "信息", "info" -> "#ffffff";  // 白色
            default -> "#00ff00";  // 默认绿色（终端风格）
        };
    }

    private void clearLogs() {
        Platform.runLater(() -> {
            logFlow.getChildren().clear();
            Text welcomeText = new Text("日志已清空\n");
            welcomeText.setStyle(
                "-fx-fill: #86868b;" +
                "-fx-font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;" +
                "-fx-font-size: 12px;"
            );
            logFlow.getChildren().add(welcomeText);
        });
    }
}
