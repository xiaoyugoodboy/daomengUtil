package co.xiaoyuboy.gui.view;

import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.gui.component.ActivityCard;
import co.xiaoyuboy.gui.component.LogPanel;
import co.xiaoyuboy.gui.component.SubmitModeDialog;
import co.xiaoyuboy.gui.service.ActivityService;
import co.xiaoyuboy.gui.util.ActivitySubmitConfig;
import co.xiaoyuboy.gui.util.AlertUtil;
import co.xiaoyuboy.gui.util.GuiConfigManager;
import co.xiaoyuboy.gui.util.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面 - 活动列表 + 日志系统
 */
@Slf4j
public class MainView extends BorderPane {

    private final User user;
    private final ActivityService activityService;

    private LogPanel logPanel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    private Label statusLabel;
    private TabPane tabPane;
    private Tab availableTab;
    private Tab allTab;
    private TextField searchField;  // 搜索框
    private List<Activity> currentActivities;  // 当前显示的所有活动

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

        // 左侧：活动列表（带Tab切换）
        VBox leftPanel = createActivityTabPanel();

        // 右侧：日志面板
        logPanel = new LogPanel();

        splitPane.getItems().addAll(leftPanel, logPanel);
        setCenter(splitPane);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(18, 25, 18, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");
        topBar.setStyle(topBar.getStyle() + "-fx-min-height: 70px;");

        // 左侧：标题区域
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("到梦空间抢单助手");
        titleLabel.getStyleClass().add("top-bar-title");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #1d1d1f;");

        // 全局模式显示（更醒目）
        HBox modeBox = new HBox(8);
        modeBox.setAlignment(Pos.CENTER_LEFT);

        Label modeIconLabel = new Label("⚙️");
        modeIconLabel.setStyle("-fx-font-size: 14px;");

        Label modeLabel = new Label(GuiConfigManager.getModeDescription());
        modeLabel.getStyleClass().add("user-label");
        modeLabel.setStyle(
            "-fx-text-fill: #007aff;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 5px 12px;" +
            "-fx-background-color: rgba(0, 122, 255, 0.12);" +
            "-fx-background-radius: 8px;"
        );

        modeBox.getChildren().addAll(modeIconLabel, modeLabel);
        titleBox.getChildren().addAll(titleLabel, modeBox);

        // 修改模式按钮（小而精致）
        Button changeModeButton = new Button("修改");
        changeModeButton.setStyle(
            "-fx-background-color: rgba(0, 122, 255, 0.08);" +
            "-fx-text-fill: #007aff;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 4px 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(0, 122, 255, 0.3);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;"
        );
        changeModeButton.setOnMouseEntered(e -> {
            changeModeButton.setStyle(changeModeButton.getStyle() + "-fx-background-color: rgba(0, 122, 255, 0.15);");
        });
        changeModeButton.setOnMouseExited(e -> {
            changeModeButton.setStyle(
                "-fx-background-color: rgba(0, 122, 255, 0.08);" +
                "-fx-text-fill: #007aff;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 4px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: rgba(0, 122, 255, 0.3);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;"
            );
        });
        changeModeButton.setOnAction(e -> {
            SubmitModeDialog modeDialog = new SubmitModeDialog();
            boolean configured = modeDialog.showAndWait((javafx.stage.Stage) getScene().getWindow());
            if (configured) {
                GuiConfigManager.setGlobalConfig(
                    modeDialog.getSelectedMode(),
                    modeDialog.getSubmitCount(),
                    modeDialog.getIntervalMs(),
                    modeDialog.getLeadTimeMs()
                );
                modeLabel.setText(GuiConfigManager.getModeDescription());
                LogManager.addLog("配置", "全局模式已更新: " + GuiConfigManager.getModeDescription());
                AlertUtil.showSuccess("全局提交模式已更新");
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 右侧：用户信息和操作区域
        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        // 状态标签
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-text");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b; -fx-font-weight: 500;");

        // 加载指示器
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(18, 18);
        loadingIndicator.setVisible(false);

        // 用户信息（更醒目）
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER);
        userBox.setStyle(
            "-fx-background-color: rgba(52, 199, 89, 0.12);" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 8px 14px;"
        );

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 14px;");

        Label userLabel = new Label(user.getName() != null ? user.getName() : user.getNickname());
        userLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #34c759;" +
            "-fx-font-weight: 600;"
        );

        userBox.getChildren().addAll(userIcon, userLabel);

        // 刷新按钮（更大更醒目）
        refreshButton = new Button("🔄 刷新活动");
        refreshButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0a84ff, #007aff);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.3), 8, 0, 0, 2);"
        );
        refreshButton.setOnMouseEntered(e -> {
            refreshButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #007aff, #0051d5);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 10px;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.4), 12, 0, 0, 3);" +
                "-fx-scale-x: 1.02;" +
                "-fx-scale-y: 1.02;"
            );
        });
        refreshButton.setOnMouseExited(e -> {
            refreshButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0a84ff, #007aff);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 10px;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.3), 8, 0, 0, 2);"
            );
        });
        refreshButton.setOnAction(e -> loadActivities());

        rightBox.getChildren().addAll(statusLabel, loadingIndicator, userBox, refreshButton);

        topBar.getChildren().addAll(titleBox, changeModeButton, spacer, rightBox);

        return topBar;
    }

    private VBox createActivityTabPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25, 20, 20, 20));
        panel.getStyleClass().add("activity-list-panel");

        // 标题区域
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // 左侧标题
        VBox titleBox = new VBox(5);
        Label headerLabel = new Label("活动列表");
        headerLabel.getStyleClass().add("panel-header");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1d1d1f;");

        Label subtitleLabel = new Label("选择您要报名的活动");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");

        titleBox.getChildren().addAll(headerLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 右侧搜索框
        searchField = new TextField();
        searchField.setPromptText("🔍 搜索活动名称...");
        searchField.setPrefWidth(250);
        searchField.setStyle(
            "-fx-background-color: rgba(245, 245, 247, 0.8);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-padding: 8px 14px;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: rgba(0, 0, 0, 0.1);" +
            "-fx-border-width: 1px;"
        );

        // 搜索框聚焦效果
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                searchField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-padding: 8px 14px;" +
                    "-fx-font-size: 13px;" +
                    "-fx-border-color: #007aff;" +
                    "-fx-border-width: 2px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.2), 8, 0, 0, 0);"
                );
            } else {
                searchField.setStyle(
                    "-fx-background-color: rgba(245, 245, 247, 0.8);" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-padding: 8px 14px;" +
                    "-fx-font-size: 13px;" +
                    "-fx-border-color: rgba(0, 0, 0, 0.1);" +
                    "-fx-border-width: 1px;"
                );
            }
        });

        // 实时搜索
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterActivities(newVal);
        });

        headerBox.getChildren().addAll(titleBox, spacer, searchField);

        // 创建TabPane
        tabPane = new TabPane();
        tabPane.getStyleClass().add("modern-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // 可报名活动Tab
        availableTab = new Tab("📋 可报名活动");
        availableTab.setClosable(false);
        ScrollPane availableContent = createTabContent();
        availableTab.setContent(availableContent);

        // 全部活动Tab
        allTab = new Tab("📊 全部活动");
        allTab.setClosable(false);
        ScrollPane allContent = createTabContent();
        allTab.setContent(allContent);

        tabPane.getTabs().addAll(availableTab, allTab);

        // 监听Tab切换
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == availableTab) {
                LogManager.addLog("切换", "📋 切换到可报名活动列表");
                loadActivities();
            } else if (newTab == allTab) {
                LogManager.addLog("切换", "📊 切换到全部活动列表");
                loadAllActivities();
            }
        });

        panel.getChildren().addAll(headerBox, tabPane);

        return panel;
    }

    private ScrollPane createTabContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("activity-list");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);  // 允许垂直滚动
        scrollPane.getStyleClass().add("modern-scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;  // 返回ScrollPane而不是content
    }

    private void loadActivities() {
        // 根据当前选中的Tab加载相应的活动
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == allTab) {
            loadAllActivities();
        } else {
            loadAvailableActivities();
        }
    }

    private void loadAvailableActivities() {
        refreshButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("加载中...");

        ScrollPane scrollPane = (ScrollPane) availableTab.getContent();
        VBox currentListBox = (VBox) scrollPane.getContent();
        currentListBox.getChildren().clear();

        LogManager.addLog("活动", "开始获取可报名活动列表");

        new Thread(() -> {
            try {
                List<Activity> activities = activityService.getAvailableActivities(user);

                Platform.runLater(() -> {
                    if (activities.isEmpty()) {
                        Label emptyLabel = new Label("暂无可报名活动");
                        emptyLabel.getStyleClass().add("empty-label");
                        currentListBox.getChildren().add(emptyLabel);
                        statusLabel.setText("无活动");
                    } else {
                        currentActivities = new ArrayList<>(activities);  // 保存当前活动列表
                        for (Activity activity : activities) {
                            ActivityCard card = new ActivityCard(activity, user, this::handleSubmit);
                            currentListBox.getChildren().add(card);
                        }
                        statusLabel.setText("共 " + activities.size() + " 个可报名活动");
                    }

                    LogManager.addLog("活动", "获取成功，共 " + activities.size() + " 个可报名活动");
                    resetLoadingUI();
                });

            } catch (Exception e) {
                log.error("加载可报名活动失败", e);
                Platform.runLater(() -> {
                    AlertUtil.showError("加载可报名活动失败: " + e.getMessage());
                    statusLabel.setText("加载失败");
                    LogManager.addLog("活动", "加载失败: " + e.getMessage());
                    resetLoadingUI();
                });
            }
        }).start();
    }

    private void loadAllActivities() {
        refreshButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("加载中...");

        ScrollPane scrollPane = (ScrollPane) allTab.getContent();
        VBox currentListBox = (VBox) scrollPane.getContent();
        currentListBox.getChildren().clear();

        LogManager.addLog("活动", "开始获取全部活动列表");

        new Thread(() -> {
            try {
                List<Activity> activities = activityService.getAllActivities(user);

                Platform.runLater(() -> {
                    if (activities.isEmpty()) {
                        Label emptyLabel = new Label("暂无活动");
                        emptyLabel.getStyleClass().add("empty-label");
                        currentListBox.getChildren().add(emptyLabel);
                        statusLabel.setText("无活动");
                    } else {
                        currentActivities = new ArrayList<>(activities);  // 保存当前活动列表
                        for (Activity activity : activities) {
                            ActivityCard card = new ActivityCard(activity, user, this::handleSubmit);
                            currentListBox.getChildren().add(card);
                        }
                        statusLabel.setText("共 " + activities.size() + " 个活动");
                    }

                    LogManager.addLog("活动", "获取成功，共 " + activities.size() + " 个活动");
                    resetLoadingUI();
                });

            } catch (Exception e) {
                log.error("加载全部活动失败", e);
                Platform.runLater(() -> {
                    AlertUtil.showError("加载全部活动失败: " + e.getMessage());
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

    private void filterActivities(String keyword) {
        if (currentActivities == null || currentActivities.isEmpty()) {
            return;
        }

        // 获取当前Tab的ScrollPane
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
        VBox listBox = (VBox) scrollPane.getContent();
        listBox.getChildren().clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            // 没有搜索关键词，显示所有活动
            for (Activity activity : currentActivities) {
                ActivityCard card = new ActivityCard(activity, user, this::handleSubmit);
                listBox.getChildren().add(card);
            }
            statusLabel.setText("共 " + currentActivities.size() + " 个活动");
        } else {
            // 有搜索关键词，过滤活动
            String lowerKeyword = keyword.toLowerCase().trim();
            List<Activity> filtered = currentActivities.stream()
                .filter(activity -> activity.getName().toLowerCase().contains(lowerKeyword))
                .toList();

            if (filtered.isEmpty()) {
                Label emptyLabel = new Label("未找到包含 \"" + keyword + "\" 的活动");
                emptyLabel.getStyleClass().add("empty-label");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #86868b;");
                listBox.getChildren().add(emptyLabel);
                statusLabel.setText("未找到匹配活动");
            } else {
                for (Activity activity : filtered) {
                    ActivityCard card = new ActivityCard(activity, user, this::handleSubmit);
                    listBox.getChildren().add(card);
                }
                statusLabel.setText("找到 " + filtered.size() + " 个匹配活动");
            }
        }
    }

    private void handleSubmit(ActivitySubmitConfig config) {
        Activity activity = config.getActivity();
        LogManager.addLog("提交", "开始提交活动: " + activity.getName() +
            " | 模式: " + config.getMode() +
            " | 次数:" + config.getSubmitCount() +
            " | 间隔:" + config.getIntervalMs() + "ms" +
            " | 提前:" + config.getLeadTimeMs() + "ms");

        new Thread(() -> {
            try {
                var result = activityService.submitActivity(config, user);

                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        AlertUtil.showSuccess("提交成功！");
                        LogManager.addLog("提交", "提交成功: " + activity.getName() +
                            " | 模式: " + config.getMode() +
                            " | 次数:" + config.getSubmitCount() +
                            " | 间隔:" + config.getIntervalMs() + "ms" +
                            " | 提前:" + config.getLeadTimeMs() + "ms");
                    } else {
                        String reason = result.getMessage() != null ? result.getMessage() : "未知原因";
                        AlertUtil.showWarning("提交失败：" + reason);
                        LogManager.addLog("提交", "提交失败: " + activity.getName() + " | 原因: " + reason);
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
