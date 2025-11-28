package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.config.RuntimeConfig.ModeType;
import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.gui.util.ActivitySubmitConfig;
import co.xiaoyuboy.gui.util.GuiConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * 活动卡片组件
 */
public class ActivityCard extends VBox {

    private ModeType customMode = null;  // 自定义模式（null表示使用全局）
    private long customSubmitCount = 0;
    private long customIntervalMs = 0;
    private long customLeadTimeMs = 0;
    private Label modeIndicator;

    public ActivityCard(Activity activity, User user, Consumer<ActivitySubmitConfig> onSubmit) {
        getStyleClass().add("activity-card");
        setPadding(new Insets(15));
        setSpacing(10);

        // 活动标题
        Label titleLabel = new Label(activity.getName());
        titleLabel.getStyleClass().add("activity-title");
        titleLabel.setWrapText(true);

        // 活动信息
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("⏰ " + activity.getActivitytime());
        timeLabel.getStyleClass().add("activity-info");

        Label statusLabel = new Label("📍 " + getStatusText(activity.getStatus()));
        statusLabel.getStyleClass().add("activity-status");

        infoBox.getChildren().addAll(timeLabel, statusLabel);

        // 模式指示器
        modeIndicator = new Label("🌐 使用全局模式");
        modeIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // 底部操作栏
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID: " + activity.getActivityId());
        idLabel.getStyleClass().add("activity-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 配置按钮
        Button configButton = new Button("⚙️");
        configButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 5 10; -fx-cursor: hand;");
        configButton.setTooltip(new javafx.scene.control.Tooltip("单独配置此活动的提交模式"));
        configButton.setOnAction(e -> {
            SubmitModeDialog modeDialog = new SubmitModeDialog();
            Stage stage = (Stage) getScene().getWindow();
            boolean configured = modeDialog.showAndWait(stage);
            if (configured) {
                customMode = modeDialog.getSelectedMode();
                customSubmitCount = modeDialog.getSubmitCount();
                customIntervalMs = modeDialog.getIntervalMs();
                customLeadTimeMs = modeDialog.getLeadTimeMs();
                updateModeIndicator();
            }
        });

        // 重置按钮
        Button resetButton = new Button("↺");
        resetButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 5 10; -fx-cursor: hand;");
        resetButton.setTooltip(new javafx.scene.control.Tooltip("恢复使用全局模式"));
        resetButton.setOnAction(e -> {
            customMode = null;
            updateModeIndicator();
        });

        // 提交按钮
        Button submitButton = new Button("立即报名");
        submitButton.getStyleClass().add("submit-button");
        submitButton.setOnAction(e -> {
            ActivitySubmitConfig config;
            if (customMode != null) {
                config = new ActivitySubmitConfig(activity, customMode, customSubmitCount, customIntervalMs, customLeadTimeMs);
            } else {
                config = ActivitySubmitConfig.fromGlobal(activity);
            }
            onSubmit.accept(config);
        });

        bottomBox.getChildren().addAll(idLabel, spacer, configButton, resetButton, submitButton);

        getChildren().addAll(titleLabel, infoBox, modeIndicator, bottomBox);
    }

    private void updateModeIndicator() {
        if (customMode == null) {
            modeIndicator.setText("🌐 使用全局模式");
            modeIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        } else {
            String desc = getModeDescription(customMode, customSubmitCount, customIntervalMs, customLeadTimeMs);
            modeIndicator.setText("⚙️ 自定义: " + desc);
            modeIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        }
    }

    private String getModeDescription(ModeType mode, long submitCount, long intervalMs, long leadTimeMs) {
        switch (mode) {
            case NO_CAPTCHA_DEFAULT:
                return "无验证码默认 (提交" + submitCount + "次, 间隔" + intervalMs + "ms)";
            case NO_CAPTCHA_CUSTOM:
                return "无验证码自定义 (提交" + submitCount + "次, 间隔" + intervalMs + "ms, 提前" + leadTimeMs + "ms)";
            case CAPTCHA:
                return "验证码识别 (提交" + submitCount + "次, 间隔" + intervalMs + "ms, 提前" + leadTimeMs + "ms)";
            default:
                return "未知模式";
        }
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "1" -> "未开始";
            case "2" -> "规划中";
            case "3" -> "报名中";
            case "4" -> "等待中";
            case "5" -> "进行中";
            case "6" -> "已结束";
            case "7" -> "已取消";
            case "8" -> "已删除";
            default -> "未知(" + status + ")";
        };
    }
}
