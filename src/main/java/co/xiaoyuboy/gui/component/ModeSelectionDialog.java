package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.config.RuntimeConfig.ModeType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 苹果风格的模式选择对话框
 * 用于在登录后询问用户学校是否有验证码
 */
public class ModeSelectionDialog {

    private ModeType selectedMode = null;
    private boolean confirmed = false;

    public boolean showAndWait(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);  // 无边框
        dialog.setResizable(false);

        VBox root = new VBox(25);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 40, 0, 0, 10);"
        );

        // 图标
        Label iconLabel = new Label("🎓");
        iconLabel.setStyle("-fx-font-size: 48px;");

        // 标题
        Label titleLabel = new Label("欢迎使用到梦空间抢单助手");
        titleLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1d1d1f;"
        );

        // 提示文字
        Label questionLabel = new Label("请问您的学校在活动报名时是否需要");
        questionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label questionLabel2 = new Label("填写验证码（计算题）？");
        questionLabel2.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // 说明
        VBox hintBox = new VBox(8);
        hintBox.setAlignment(Pos.CENTER);
        hintBox.setStyle(
            "-fx-background-color: rgba(0, 122, 255, 0.1);" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 15;"
        );

        Label hint1 = new Label("💡 如果需要计算验证码（如 3+5=?）");
        hint1.setStyle("-fx-font-size: 12px; -fx-text-fill: #007aff;");

        Label hint2 = new Label("   请选择「验证码识别模式」");
        hint2.setStyle("-fx-font-size: 12px; -fx-text-fill: #007aff;");

        Label hint3 = new Label("🚀 如果无需验证码");
        hint3.setStyle("-fx-font-size: 12px; -fx-text-fill: #007aff;");

        Label hint4 = new Label("   请选择「无验证码模式」");
        hint4.setStyle("-fx-font-size: 12px; -fx-text-fill: #007aff;");

        hintBox.getChildren().addAll(hint1, hint2, hint3, hint4);

        // 验证码示例图片
        VBox imageBox = new VBox(10);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.05);" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 15;"
        );

        Label imageLabel = new Label("📸 验证码示例（如果有这种图，选择验证码模式）");
        imageLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #666;" +
            "-fx-font-weight: 600;"
        );

        try {
            Image captchaImage = new Image(getClass().getResourceAsStream("/captcha/1.jpg"));
            ImageView imageView = new ImageView(captchaImage);
            imageView.setFitWidth(200);
            imageView.setFitHeight(60);
            imageView.setPreserveRatio(true);
            imageView.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);" +
                "-fx-background-radius: 8;"
            );

            Label exampleLabel = new Label("这种计算题验证码 → 选择验证码模式");
            exampleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff3b30; -fx-font-weight: bold;");

            imageBox.getChildren().addAll(imageLabel, imageView, exampleLabel);
        } catch (Exception e) {
            Label errorLabel = new Label("⚠ 验证码示例图片加载失败");
            errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
            imageBox.getChildren().addAll(imageLabel, errorLabel);
        }

        // 按钮容器
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPrefWidth(320);

        // 验证码模式按钮
        Button captchaButton = createAppleButton(
            "需要验证码（AI智能识别）",
            "#007aff",
            "white",
            true
        );
        captchaButton.setOnAction(e -> {
            selectedMode = ModeType.CAPTCHA;
            confirmed = true;
            dialog.close();
        });

        // 无验证码模式按钮
        Button noCaptchaButton = createAppleButton(
            "无需验证码（快速抢单）",
            "#34c759",
            "white",
            true
        );
        noCaptchaButton.setOnAction(e -> {
            selectedMode = ModeType.NO_CAPTCHA_DEFAULT;
            confirmed = true;
            dialog.close();
        });

        // 自定义模式按钮（次要）
        Button customButton = createAppleButton(
            "高级自定义",
            "rgba(0, 0, 0, 0.05)",
            "#333",
            false
        );
        customButton.setOnAction(e -> {
            dialog.close();
            SubmitModeDialog modeDialog = new SubmitModeDialog();
            boolean customConfigured = modeDialog.showAndWait(owner);
            if (customConfigured) {
                selectedMode = modeDialog.getSelectedMode();
                confirmed = true;
            }
        });

        buttonBox.getChildren().addAll(captchaButton, noCaptchaButton, customButton);

        root.getChildren().addAll(
            iconLabel,
            titleLabel,
            questionLabel,
            questionLabel2,
            hintBox,
            imageBox,
            buttonBox
        );

        Scene scene = new Scene(root, 450, 720);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return confirmed;
    }

    private Button createAppleButton(String text, String bgColor, String textColor, boolean isPrimary) {
        Button button = new Button(text);
        button.setPrefWidth(320);
        button.setPrefHeight(50);

        String style = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: %s;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);",
            bgColor,
            textColor,
            isPrimary ? "bold" : "normal"
        );

        button.setStyle(style);

        // 悬停效果
        button.setOnMouseEntered(e -> {
            String hoverStyle = style.replace(bgColor, adjustBrightness(bgColor, 0.9));
            button.setStyle(hoverStyle);
        });

        button.setOnMouseExited(e -> button.setStyle(style));

        return button;
    }

    private String adjustBrightness(String color, double factor) {
        if (color.startsWith("#")) {
            // 简单的颜色变暗处理（实际应该用HSL，这里简化）
            return color;
        }
        return color;
    }

    public ModeType getSelectedMode() {
        return selectedMode;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
