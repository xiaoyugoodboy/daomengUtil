package co.xiaoyuboy.gui.view;

import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.gui.component.ActivityCard;
import co.xiaoyuboy.gui.component.LogPanel;
import co.xiaoyuboy.gui.service.ActivityService;
import co.xiaoyuboy.gui.util.AlertUtil;
import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 主界面 - 活动列表 + 日志系统
 */
@Slf4j
public class MainView extends BorderPane {

    private final User user;
    private final ActivityService activityService;

    private VBox activityListBox;
    private LogPanel logPanel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    private Label statusLabel;

    public MainView(User user) {
        this.user = user;
        this.activityService = new ActivityService();

        initUI();
        loadActivities();
    }

    private void initUI() {
        getStyleClass().add("main-view");

        // 顶部工具栏
        HBox topBar = createTopBar();
        setTop(topBar);

        // 中间分割面板
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.6);

        // 左侧：活动列表
        VBox leftPanel = createActivityListPanel();

        // 右侧：日志面板
        logPanel = new LogPanel();

        splitPane.getItems().addAll(leftPanel, logPanel);
        setCenter(splitPane);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        // 标题
        Label titleLabel = new Label("📅 活动列表");
        titleLabel.getStyleClass().add("top-bar-title");

        // 用户信息
        Label userLabel = new Label("用户: " + (user.getName() != null ? user.getName() : user.getNickname()));
        userLabel.getStyleClass().add("user-label");

        // 刷新按钮
        refreshButton = new Button("🔄 刷新");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(e -> loadActivities());

        // 加载指示器
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);

        // 状态标签
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(
            titleLabel,
            spacer,
            statusLabel,
            loadingIndicator,
            userLabel,
            refreshButton
        );

        return topBar;
    }

    private VBox createActivityListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("activity-list-panel");

        Label headerLabel = new Label("可报名活动");
        headerLabel.getStyleClass().add("panel-header");

        activityListBox = new VBox(15);
        activityListBox.getStyleClass().add("activity-list");

        ScrollPane scrollPane = new ScrollPane(activityListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("modern-scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(headerLabel, scrollPane);

        return panel;
    }

    private void loadActivities() {
        refreshButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("加载中...");
        activityListBox.getChildren().clear();

        LogManager.addLog("活动", "开始获取活动列表");

        new Thread(() -> {
            try {
                List<Activity> activities = activityService.getAvailableActivities(user);

                Platform.runLater(() -> {
                    if (activities.isEmpty()) {
                        Label emptyLabel = new Label("暂无可报名活动");
                        emptyLabel.getStyleClass().add("empty-label");
                        activityListBox.getChildren().add(emptyLabel);
                        statusLabel.setText("无活动");
                    } else {
                        for (Activity activity : activities) {
                            ActivityCard card = new ActivityCard(activity, user, this::handleSubmit);
                            activityListBox.getChildren().add(card);
                        }
                        statusLabel.setText("共 " + activities.size() + " 个活动");
                    }

                    LogManager.addLog("活动", "获取成功，共 " + activities.size() + " 个活动");
                    resetLoadingUI();
                });

            } catch (Exception e) {
                log.error("加载活动失败", e);
                Platform.runLater(() -> {
                    AlertUtil.showError("加载活动失败: " + e.getMessage());
                    statusLabel.setText("加载失败");
                    LogManager.addLog("活动", "加载失败: " + e.getMessage());
                    resetLoadingUI();
                });
            }
        }).start();
    }

    private void resetLoadingUI() {
        refreshButton.setDisable(false);
        loadingIndicator.setVisible(false);
    }

    private void handleSubmit(Activity activity) {
        LogManager.addLog("提交", "开始提交活动: " + activity.getName());

        new Thread(() -> {
            try {
                boolean success = activityService.submitActivity(activity, user);

                Platform.runLater(() -> {
                    if (success) {
                        AlertUtil.showSuccess("提交成功！");
                        LogManager.addLog("提交", "提交成功: " + activity.getName());
                    } else {
                        AlertUtil.showWarning("提交失败，请重试");
                        LogManager.addLog("提交", "提交失败: " + activity.getName());
                    }
                });

            } catch (Exception e) {
                log.error("提交活动失败", e);
                Platform.runLater(() -> {
                    AlertUtil.showError("提交异常: " + e.getMessage());
                    LogManager.addLog("提交", "提交异常: " + e.getMessage());
                });
            }
        }).start();
    }
}
