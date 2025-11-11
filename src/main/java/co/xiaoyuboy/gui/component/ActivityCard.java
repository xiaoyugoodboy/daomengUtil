package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * 活动卡片组件
 */
public class ActivityCard extends VBox {

    public ActivityCard(Activity activity, User user, Consumer<Activity> onSubmit) {
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

        // 底部操作栏
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID: " + activity.getActivityId());
        idLabel.getStyleClass().add("activity-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button submitButton = new Button("立即报名");
        submitButton.getStyleClass().add("submit-button");
        submitButton.setOnAction(e -> onSubmit.accept(activity));

        bottomBox.getChildren().addAll(idLabel, spacer, submitButton);

        getChildren().addAll(titleLabel, infoBox, bottomBox);
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "2" -> "规划中";
            case "3" -> "报名中";
            case "4" -> "等待中";
            default -> "未知";
        };
    }
}
