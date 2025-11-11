package co.xiaoyuboy.gui;

import co.xiaoyuboy.gui.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

/**
 * 到梦空间抢单工具 - GUI主应用
 *
 * @author xiaoyuboy
 * @version 2.0-GUI
 */
@Slf4j
public class DaoMengApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("启动到梦空间GUI应用...");

            // 创建登录视图
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView, 400, 550);

            // 加载CSS样式（macOS风格）
            scene.getStylesheets().add(
                getClass().getResource("/css/macos-style.css").toExternalForm()
            );

            // 设置舞台
            primaryStage.setScene(scene);
            primaryStage.setTitle("到梦空间 - 登录");
            primaryStage.setResizable(false);

            // 设置图标（如果有）
            try {
                primaryStage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/icon/app-icon.png"))
                );
            } catch (Exception e) {
                log.warn("未找到应用图标");
            }

            // 显示窗口
            primaryStage.show();

            log.info("应用启动成功");

        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        log.info("应用关闭");
    }
}
