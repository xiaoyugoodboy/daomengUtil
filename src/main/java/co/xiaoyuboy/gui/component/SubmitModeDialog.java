package co.xiaoyuboy.gui.component;

import co.xiaoyuboy.config.RuntimeConfig.ModeType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 提交模式配置对话框 - Apple Big Sur风格
 */
public class SubmitModeDialog {

    private ModeType selectedMode = ModeType.NO_CAPTCHA_DEFAULT;
    private long submitCount = 5;
    private long intervalMs = 2;
    private long leadTimeMs = 0;
    private boolean confirmed = false;

    // UI组件引用
    private HBox defaultCard, customCard;
    private VBox captchaCard, noCaptchaMainCard;
    private TextField submitCountField, intervalField, leadTimeField;
    private VBox customParamsBox;

    public boolean showAndWait(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(true); // allow user to enlarge if needed

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 40, 0, 0, 10);"
        );

        // 头部区域
        VBox header = createHeader(dialog);

        // 模式卡片容器
        VBox modesContainer = new VBox(12);
        modesContainer.setAlignment(Pos.CENTER);

        // 1. 验证码识别模式
        captchaCard = createModeCard(
            "🤖",
            "验证码识别模式",
            "AI智能",
            "自动识别计算题验证码（提交1次，失败自动重试最多7次）",
            "#007aff"
        );
        captchaCard.setOnMouseClicked(e -> {
            selectMainCard(captchaCard, ModeType.CAPTCHA);
        });

        // 2. 无验证码模式（包含默认和自定义）
        noCaptchaMainCard = createNoCaptchaModeCard();

        modesContainer.getChildren().addAll(captchaCard, noCaptchaMainCard);

        // 按钮区域
        HBox buttonBox = createButtonBox(dialog);

        // 将主体内容放入可滚动容器，防止内容溢出遮挡底部按钮
        VBox content = new VBox(20);
        content.setPadding(new Insets(10, 0, 0, 0));
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(header, modesContainer, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().add(scrollPane);

        Scene scene = new Scene(root, 520, 560);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(scene);

        // 默认选中无验证码默认模式
        selectMainCard(noCaptchaMainCard, ModeType.NO_CAPTCHA_DEFAULT);
        selectNoCaptchaDefault();

        dialog.showAndWait();

        return confirmed;
    }

    private VBox createHeader(Stage dialog) {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);

        // 顶部关闭按钮，保证无边框窗口也能关闭
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        Button closeBtn = new Button("✕");
        closeBtn.setFocusTraversable(false);
        closeBtn.setStyle(
            "-fx-background-color: rgba(0,0,0,0.08);" +
            "-fx-text-fill: #555;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 4 8;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            confirmed = false;
            dialog.close();
        });
        topBar.getChildren().add(closeBtn);

        Label iconLabel = new Label("🎛");
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label titleLabel = new Label("配置提交模式");
        titleLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1d1d1f;"
        );

        Label subtitleLabel = new Label("选择最适合您学校的提交策略");
        subtitleLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #86868b;"
        );

        header.getChildren().addAll(topBar, iconLabel, titleLabel, subtitleLabel);
        return header;
    }

    private VBox createModeCard(String icon, String title, String badge, String description, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(400);
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(0, 0, 0, 0.08);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0, 0, 2);"
        );

        // 顶部：图标 + 标题 + 徽章
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 22px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1d1d1f;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeLabel = new Label(badge);
        badgeLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: " + accentColor + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 3px 8px;"
        );

        topRow.getChildren().addAll(iconLabel, titleLabel, spacer, badgeLabel);

        // 描述
        Label descLabel = new Label(description);
        descLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #666;" +
            "-fx-wrap-text: true;"
        );
        descLabel.setMaxWidth(380);

        card.getChildren().addAll(topRow, descLabel);

        // 悬停效果
        card.setOnMouseEntered(e -> {
            if (!card.getStyle().contains("border-width: 3")) {
                card.setStyle(card.getStyle().replace(
                    "-fx-border-color: rgba(0, 0, 0, 0.08);",
                    "-fx-border-color: rgba(0, 122, 255, 0.3);"
                ));
            }
        });

        card.setOnMouseExited(e -> {
            if (!card.getStyle().contains("border-width: 3")) {
                card.setStyle(card.getStyle().replace(
                    "-fx-border-color: rgba(0, 122, 255, 0.3);",
                    "-fx-border-color: rgba(0, 0, 0, 0.08);"
                ));
            }
        });

        return card;
    }

    private VBox createNoCaptchaModeCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(400);
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(0, 0, 0, 0.08);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0, 0, 2);"
        );

        // 顶部
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🚀");
        iconLabel.setStyle("-fx-font-size: 22px;");

        Label titleLabel = new Label("无验证码模式");
        titleLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1d1d1f;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeLabel = new Label("推荐");
        badgeLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #34c759;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 3px 8px;"
        );

        topRow.getChildren().addAll(iconLabel, titleLabel, spacer, badgeLabel);

        Label descLabel = new Label("快速抢单，无需验证码识别");
        descLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #666;"
        );

        // 子选项：默认和自定义
        VBox subOptions = new VBox(8);
        subOptions.setPadding(new Insets(10, 0, 0, 0));

        // 默认参数选项
        defaultCard = createSubOption("默认参数（推荐）", "提交5次 · 间隔2ms · 提前量0ms", true);
        defaultCard.setOnMouseClicked(e -> {
            e.consume();  // 阻止事件冒泡到主卡片
            selectMainCard(noCaptchaMainCard, ModeType.NO_CAPTCHA_DEFAULT);
            selectNoCaptchaDefault();
        });

        // 自定义参数选项
        customCard = createSubOption("自定义参数", "自行配置提交策略", false);
        customCard.setOnMouseClicked(e -> {
            e.consume();  // 阻止事件冒泡到主卡片
            selectMainCard(noCaptchaMainCard, ModeType.NO_CAPTCHA_CUSTOM);
            selectNoCaptchaCustom();
        });

        // 自定义参数面板（默认隐藏）
        customParamsBox = new VBox(8);
        customParamsBox.setPadding(new Insets(10, 0, 0, 10));
        customParamsBox.setVisible(false);

        HBox countRow = createCompactParamField("次数", "5");
        submitCountField = (TextField) countRow.getChildren().get(1);

        HBox intervalRow = createCompactParamField("间隔(ms)", "2");
        intervalField = (TextField) intervalRow.getChildren().get(1);

        HBox leadRow = createCompactParamField("提前(ms)", "0");
        leadTimeField = (TextField) leadRow.getChildren().get(1);

        customParamsBox.getChildren().addAll(countRow, intervalRow, leadRow);

        subOptions.getChildren().addAll(defaultCard, customCard, customParamsBox);

        card.getChildren().addAll(topRow, descLabel, subOptions);

        // 点击主卡片（空白区域）选中无验证码默认模式
        card.setOnMouseClicked(e -> {
            selectMainCard(card, ModeType.NO_CAPTCHA_DEFAULT);
            selectNoCaptchaDefault();
        });

        return card;
    }

    private HBox createSubOption(String text, String desc, boolean isDefault) {
        HBox option = new HBox(10);
        option.setPadding(new Insets(10, 12, 10, 12));
        option.setAlignment(Pos.CENTER_LEFT);
        option.setStyle(
            "-fx-background-color: rgba(245, 245, 247, 0.6);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(0, 0, 0, 0.05);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );

        VBox textBox = new VBox(3);
        Label titleLabel = new Label(text);
        titleLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1d1d1f;"
        );

        Label descLabel = new Label(desc);
        descLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #86868b;" +
            "-fx-font-family: 'SF Mono', 'Consolas', monospace;"
        );

        textBox.getChildren().addAll(titleLabel, descLabel);
        option.getChildren().add(textBox);

        // 悬停效果
        option.setOnMouseEntered(e -> {
            if (!option.getStyle().contains("border-width: 2")) {
                option.setStyle(option.getStyle().replace(
                    "-fx-background-color: rgba(245, 245, 247, 0.6);",
                    "-fx-background-color: rgba(235, 235, 240, 0.8);"
                ));
            }
        });

        option.setOnMouseExited(e -> {
            if (!option.getStyle().contains("border-width: 2")) {
                option.setStyle(option.getStyle().replace(
                    "-fx-background-color: rgba(235, 235, 240, 0.8);",
                    "-fx-background-color: rgba(245, 245, 247, 0.6);"
                ));
            }
        });

        return option;
    }

    private HBox createCompactParamField(String label, String defaultValue) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(label);
        nameLabel.setPrefWidth(60);
        nameLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1d1d1f;"
        );

        TextField field = new TextField(defaultValue);
        field.setPrefWidth(100);
        field.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 6px 10px;" +
            "-fx-font-size: 12px;" +
            "-fx-border-color: rgba(0, 0, 0, 0.1);" +
            "-fx-border-width: 1px;"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-padding: 6px 10px;" +
                    "-fx-font-size: 12px;" +
                    "-fx-border-color: #007aff;" +
                    "-fx-border-width: 2px;"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-padding: 6px 10px;" +
                    "-fx-font-size: 12px;" +
                    "-fx-border-color: rgba(0, 0, 0, 0.1);" +
                    "-fx-border-width: 1px;"
                );
            }
        });

        row.getChildren().addAll(nameLabel, field);
        return row;
    }

    private void selectNoCaptchaDefault() {
        selectedMode = ModeType.NO_CAPTCHA_DEFAULT;
        customParamsBox.setVisible(false);

        // 高亮默认选项
        defaultCard.setStyle(
            "-fx-background-color: rgba(0, 122, 255, 0.12);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #007aff;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );

        // 重置自定义选项
        customCard.setStyle(
            "-fx-background-color: rgba(245, 245, 247, 0.6);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(0, 0, 0, 0.05);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
    }

    private void selectNoCaptchaCustom() {
        selectedMode = ModeType.NO_CAPTCHA_CUSTOM;
        customParamsBox.setVisible(true);

        // 重置默认选项
        defaultCard.setStyle(
            "-fx-background-color: rgba(245, 245, 247, 0.6);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(0, 0, 0, 0.05);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );

        // 高亮自定义选项
        customCard.setStyle(
            "-fx-background-color: rgba(0, 122, 255, 0.12);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #007aff;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
    }

    private void selectMainCard(VBox selectedCard, ModeType mode) {
        // 重置所有主卡片样式
        captchaCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(0, 0, 0, 0.08);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 14;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 2);"
        );

        noCaptchaMainCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(0, 0, 0, 0.08);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 14;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 2);"
        );

        // 高亮选中的主卡片
        selectedCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 1);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #007aff;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 14;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.3), 16, 0, 0, 4);"
        );

        selectedMode = mode;
    }

    private HBox createButtonBox(Stage dialog) {
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        Button cancelBtn = new Button("取消");
        cancelBtn.setPrefWidth(180);
        cancelBtn.setPrefHeight(44);
        cancelBtn.setStyle(
            "-fx-background-color: rgba(229, 229, 234, 0.8);" +
            "-fx-text-fill: #1d1d1f;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );
        cancelBtn.setOnMouseEntered(e -> {
            cancelBtn.setStyle(
                "-fx-background-color: rgba(209, 209, 214, 0.9);" +
                "-fx-text-fill: #1d1d1f;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"
            );
        });
        cancelBtn.setOnMouseExited(e -> {
            cancelBtn.setStyle(
                "-fx-background-color: rgba(229, 229, 234, 0.8);" +
                "-fx-text-fill: #1d1d1f;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
            );
        });
        cancelBtn.setOnAction(e -> {
            confirmed = false;
            dialog.close();
        });

        Button confirmBtn = new Button("确认配置");
        confirmBtn.setPrefWidth(180);
        confirmBtn.setPrefHeight(44);
        confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0a84ff, #007aff);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.4), 10, 0, 0, 3);"
        );
        confirmBtn.setOnMouseEntered(e -> {
            confirmBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #007aff, #0051d5);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.5), 12, 0, 0, 4);"
            );
        });
        confirmBtn.setOnMouseExited(e -> {
            confirmBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0a84ff, #007aff);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.4), 10, 0, 0, 3);"
            );
        });
        confirmBtn.setOnAction(e -> handleConfirm(dialog));

        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
        return buttonBox;
    }

    private void handleConfirm(Stage dialog) {
        if (selectedMode == ModeType.NO_CAPTCHA_DEFAULT) {
            submitCount = 5;
            intervalMs = 2;
            leadTimeMs = 0;
        } else if (selectedMode == ModeType.CAPTCHA) {
            submitCount = 1;
            intervalMs = 2;
            leadTimeMs = 4;
        } else if (selectedMode == ModeType.NO_CAPTCHA_CUSTOM) {
            try {
                submitCount = Long.parseLong(submitCountField.getText());
                intervalMs = Long.parseLong(intervalField.getText());
                leadTimeMs = Long.parseLong(leadTimeField.getText());

                if (submitCount <= 0 || intervalMs < 0 || leadTimeMs < 0) {
                    showError("参数必须为非负数，提交次数必须大于0");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("请输入有效的数字");
                return;
            }
        }

        confirmed = true;
        dialog.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("输入错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public ModeType getSelectedMode() {
        return selectedMode;
    }

    public long getSubmitCount() {
        return submitCount;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public long getLeadTimeMs() {
        return leadTimeMs;
    }
}
