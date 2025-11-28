package co.xiaoyuboy.gui.view;

import co.xiaoyuboy.gui.component.ModeSelectionDialog;
import co.xiaoyuboy.gui.service.AuthService;
import co.xiaoyuboy.gui.service.NetworkLicenseService;
import co.xiaoyuboy.gui.util.AlertUtil;
import co.xiaoyuboy.gui.util.GuiConfigManager;
import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

        Label logoLabel = new Label("到梦空间");
        logoLabel.getStyleClass().add("logo-text");

        Label subtitleLabel = new Label("自动抢单助手");
        subtitleLabel.getStyleClass().add("subtitle-text");

        VBox logoBox = new VBox(10, logoLabel, subtitleLabel);
        logoBox.setAlignment(Pos.CENTER);

        VBox formBox = createFormBox();

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);

        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);

        Label versionLabel = new Label("v2.0 GUI Edition");
        versionLabel.getStyleClass().add("version-label");

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

        Label accountLabel = new Label("手机号");
        accountLabel.getStyleClass().add("field-label");
        accountField = new TextField();
        accountField.setPromptText("请输入手机号");
        accountField.getStyleClass().add("modern-text-field");
        accountField.setPrefWidth(300);

        Label passwordLabel = new Label("密码");
        passwordLabel.getStyleClass().add("field-label");
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.getStyleClass().add("modern-text-field");
        passwordField.setPrefWidth(300);

        loginButton = new Button("登录");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(45);
        loginButton.setOnAction(e -> handleLogin());

        passwordField.setOnAction(e -> handleLogin());

        formBox.getChildren().addAll(
            accountLabel, accountField,
            passwordLabel, passwordField,
            loginButton
        );

        return formBox;
    }

    private void handleLogin() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();

        if (account.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("请填写完整信息");
            return;
        }

        loginButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("正在登录...");
        statusLabel.setVisible(true);

        LogManager.addLog("登录", "开始登录");

        new Thread(() -> {
            try {
                var loginResult = authService.login(account, password);

                if (loginResult.isSuccess()) {
                    LogManager.addLog("登录", "登录成功: " + account);
                    Platform.runLater(() -> {
                        statusLabel.setText("登录成功，正在验证激活码...");
                        promptLicenseAndContinue(loginResult.getData(), account);
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

    private void promptLicenseAndContinue(co.xiaoyuboy.entity.User user, String account) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("激活验证");
        dialog.setHeaderText("请输入激活码");
        dialog.setContentText("激活码：");

        var resultOpt = dialog.showAndWait();
        if (resultOpt.isEmpty() || resultOpt.get().trim().isEmpty()) {
            AlertUtil.showWarning("激活码不能为空");
            resetLoginUI();
            return;
        }

        String code = resultOpt.get().trim();
        progressIndicator.setVisible(true);
        statusLabel.setText("正在验证激活码...");
        LogManager.addLog("授权", "开始验证激活码");

        new Thread(() -> {
            boolean ok = licenseService.verify(code, account);
            Platform.runLater(() -> {
                if (ok) {
                    LogManager.addLog("授权", "激活码验证成功");
                    statusLabel.setText("激活成功，请选择提交模式...");
                    openModeDialogThenMain(user);
                } else {
                    AlertUtil.showError("激活失败，请检查激活码");
                    LogManager.addLog("授权", "激活码验证失败");
                    resetLoginUI();
                }
            });
        }).start();
    }

    private void openModeDialogThenMain(co.xiaoyuboy.entity.User user) {
        ModeSelectionDialog modeDialog = new ModeSelectionDialog();
        boolean configured = modeDialog.showAndWait(primaryStage);

        if (configured && modeDialog.getSelectedMode() != null) {
            switch (modeDialog.getSelectedMode()) {
                case CAPTCHA:
                    GuiConfigManager.setGlobalConfig(modeDialog.getSelectedMode(), 1, 2, 4);
                    break;
                case NO_CAPTCHA_DEFAULT:
                    GuiConfigManager.setGlobalConfig(modeDialog.getSelectedMode(), 5, 2, 0);
                    break;
                case NO_CAPTCHA_CUSTOM:
                    // 自定义模式已在对话框中配置
                    break;
            }

            LogManager.addLog("配置", "全局模式: " + GuiConfigManager.getModeDescription());
            openMainView(user);
        } else {
            resetLoginUI();
        }
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
