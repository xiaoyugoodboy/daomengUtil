package co.xiaoyuboy.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * 弹窗工具类
 */
public class AlertUtil {

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "错误", message);
    }

    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "警告", message);
    }

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "成功", message);
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "提示", message);
    }

    public static boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
