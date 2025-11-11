package co.xiaoyuboy.gui.view;

import co.xiaoyuboy.gui.service.AuthService;
import co.xiaoyuboy.gui.service.NetworkLicenseService;
import co.xiaoyuboy.gui.util.AlertUtil;
import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录界面 - macOS风格
 */
@Slf4j
public class LoginView extends VBox {

    private final Stage primaryStage;
    private TextField accountField;
    private PasswordField passwordField;
    private TextField activationCodeField;
    private Button loginButton;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;

    private final AuthService authService;
    private final NetworkLicenseService licenseService;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.authService = new AuthService();
        this.licenseService = new NetworkLicenseService();

        initUI();
        LogManager.addLog("系统", "应用启动");
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setSpacing(20);
        getStyleClass().add("login-container");

        // Logo区域
        Label logoLabel = new Label("到梦空间");
        logoLabel.getStyleClass().add("logo-text");

        Label subtitleLabel = new Label("自动抢单助手");
        subtitleLabel.getStyleClass().add("subtitle-text");

        VBox logoBox = new VBox(10, logoLabel, subtitleLabel);
        logoBox.setAlignment(Pos.CENTER);

        // 表单区域
        VBox formBox = createFormBox();

        // 状态显示
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);

        // 加载指示器
        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);

        // 版本信息
        Label versionLabel = new Label("v2.0 GUI Edition");
        versionLabel.getStyleClass().add("version-label");

        // 组装
        getChildren().addAll(
            logoBox,
            formBox,
            statusLabel,
            progressIndicator,
            versionLabel
        );
    }

    private VBox createFormBox() {
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(20));
        formBox.getStyleClass().add("form-box");

        // 账号输入
        Label accountLabel = new Label("手机号");
        accountLabel.getStyleClass().add("field-label");
        accountField = new TextField();
        accountField.setPromptText("请输入手机号");
        accountField.getStyleClass().add("modern-text-field");
        accountField.setPrefWidth(300);

        // 密码输入
        Label passwordLabel = new Label("密码");
        passwordLabel.getStyleClass().add("field-label");
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.getStyleClass().add("modern-text-field");
        passwordField.setPrefWidth(300);

        // 激活码输入
        Label activationLabel = new Label("激活码");
        activationLabel.getStyleClass().add("field-label");
        activationCodeField = new TextField();
        activationCodeField.setPromptText("请输入激活码");
        activationCodeField.getStyleClass().add("modern-text-field");
        activationCodeField.setPrefWidth(300);

        // 登录按钮
        loginButton = new Button("登录");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(45);
        loginButton.setOnAction(e -> handleLogin());

        // 回车登录
        passwordField.setOnAction(e -> handleLogin());
        activationCodeField.setOnAction(e -> handleLogin());

        formBox.getChildren().addAll(
            accountLabel, accountField,
            passwordLabel, passwordField,
            activationLabel, activationCodeField,
            loginButton
        );

        return formBox;
    }

    private void handleLogin() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();
        String activationCode = activationCodeField.getText().trim();

        // 验证输入
        if (account.isEmpty() || password.isEmpty() || activationCode.isEmpty()) {
            AlertUtil.showError("请填写完整信息");
            return;
        }

        // 禁用按钮，显示加载
        loginButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("正在验证...");
        statusLabel.setVisible(true);

        LogManager.addLog("登录", "开始验证激活码和登录");

        // 异步验证和登录
        new Thread(() -> {
            try {
                // 1. 验证激活码
                statusLabel.setText("验证激活码中...");
                boolean licenseValid = licenseService.verify(activationCode, account);

                if (!licenseValid) {
                    Platform.runLater(() -> {
                        AlertUtil.showError("激活码验证失败，请联系管理员");
                        resetLoginUI();
                        LogManager.addLog("登录", "激活码验证失败");
                    });
                    return;
                }

                LogManager.addLog("登录", "激活码验证成功");

                // 2. 用户登录
                Platform.runLater(() -> statusLabel.setText("正在登录..."));
                var loginResult = authService.login(account, password);

                if (loginResult.isSuccess()) {
                    LogManager.addLog("登录", "登录成功: " + account);

                    Platform.runLater(() -> {
                        statusLabel.setText("登录成功！");
                        // 跳转到主界面
                        openMainView(loginResult.getData());
                    });
                } else {
                    Platform.runLater(() -> {
                        AlertUtil.showError("登录失败: " + loginResult.getMessage());
                        resetLoginUI();
                        LogManager.addLog("登录", "登录失败: " + loginResult.getMessage());
                    });
                }

            } catch (Exception e) {
                log.error("登录异常", e);
                Platform.runLater(() -> {
                    AlertUtil.showError("登录异常: " + e.getMessage());
                    resetLoginUI();
                    LogManager.addLog("登录", "登录异常: " + e.getMessage());
                });
            }
        }).start();
    }

    private void resetLoginUI() {
        loginButton.setDisable(false);
        progressIndicator.setVisible(false);
        statusLabel.setVisible(false);
    }

    private void openMainView(co.xiaoyuboy.entity.User user) {
        try {
            MainView mainView = new MainView(user);
            Scene scene = new Scene(mainView, 1200, 800);
            scene.getStylesheets().add(
                getClass().getResource("/css/macos-style.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle("到梦空间 - " + (user.getName() != null ? user.getName() : user.getNickname()));
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            LogManager.addLog("系统", "进入主界面");

        } catch (Exception e) {
            log.error("打开主界面失败", e);
            AlertUtil.showError("打开主界面失败");
        }
    }
}
