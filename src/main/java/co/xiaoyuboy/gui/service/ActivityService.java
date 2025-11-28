package co.xiaoyuboy.gui.service;

import co.xiaoyuboy.activity.DaoMengActivitySubmitManage;
import co.xiaoyuboy.config.RuntimeConfig;
import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.gui.util.ActivitySubmitConfig;
import co.xiaoyuboy.gui.util.LogManager;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.util.DaoMengActivityRecursionParsing;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 活动服务
 */
@Slf4j
public class ActivityService {

    private final JsonParsing jsonParsing = new JsonParsing();

    /**
     * 获取可报名的活动列表（状态：规划中、报名中、等待中）
     * 并检查每个活动的 ableJoinFlag，过滤掉不能真正报名的活动
     */
    public List<Activity> getAvailableActivities(User user) {
        try {
            log.info("开始获取可报名活动列表");
            LogManager.addLog("活动", "🔄 开始获取可报名活动列表...");

            long startTime = System.currentTimeMillis();
            List<Activity> activities = DaoMengActivityRecursionParsing.getActivityList(user);
            long fetchTime = System.currentTimeMillis() - startTime;

            log.info("获取到 {} 个状态为可报名的活动，耗时 {}ms，正在检查 ableJoinFlag...", activities.size(), fetchTime);
            LogManager.addLog("活动", String.format("✅ 获取到 %d 个活动，耗时 %dms", activities.size(), fetchTime));
            LogManager.addLog("活动", "🔍 开始逐个检查活动是否真的可报名...");

            final int[] currentIndex = {0};
            final int[] validCount = {0};
            List<Activity> filteredActivities = activities.stream()
                .filter(activity -> {
                    currentIndex[0]++;
                    try {
                        log.info("正在检查活动 {}/{}: {} (ID:{})", currentIndex[0], activities.size(),
                            activity.getName(), activity.getActivityId());

                        LogManager.addLog("检查", String.format("📋 [%d/%d] %s",
                            currentIndex[0], activities.size(), activity.getName()));

                        String activityDetailJson = co.xiaoyuboy.activity.DaoMengDetail.getActivityDetail(
                            activity.getActivityId(), user);

                        boolean canJoin = jsonParsing.isActivity(activityDetailJson);
                        if (!canJoin) {
                            log.warn("活动 {} (ID:{}) ableJoinFlag!=1，已过滤", activity.getName(), activity.getActivityId());
                            LogManager.addLog("检查", String.format("   ✖ %s - 不可报名", activity.getName()));
                        } else {
                            validCount[0]++;
                            log.info("✅ 活动 {} 可报名", activity.getName());
                            LogManager.addLog("检查", String.format("   ✔ %s - 可报名(%d/%d)",
                                activity.getName(), validCount[0], currentIndex[0]));
                        }
                        return canJoin;
                    } catch (Exception e) {
                        log.error("检查活动 {} 失败: {}", activity.getName(), e.getMessage());
                        LogManager.addLog("检查", String.format("   ✖ %s - 检查失败 %s",
                            activity.getName(), e.getMessage()));
                        return false;
                    }
                })
                .toList();

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("过滤完成！剩余 {} 个真正可报名的活动，总耗时 {}ms", filteredActivities.size(), totalTime);
            LogManager.addLog("活动", String.format("✅ 完成！找到 %d 个可报名活动，总耗时 %dms",
                filteredActivities.size(), totalTime));

            return filteredActivities;
        } catch (Exception e) {
            log.error("获取可报名活动列表失败", e);
            LogManager.addLog("活动", "❌ 获取失败: " + e.getMessage());
            throw new RuntimeException("获取可报名活动失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取全部活动列表（所有状态）
     */
    public List<Activity> getAllActivities(User user) {
        try {
            log.info("开始获取全部活动列表");
            List<Activity> activities = DaoMengActivityRecursionParsing.getAllActivityList(user);
            log.info("获取全部活动成功，共 {} 个", activities.size());
            return activities;
        } catch (Exception e) {
            log.error("获取全部活动列表失败", e);
            throw new RuntimeException("获取全部活动失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提交活动报名（根据配置使用不同模式）
     */
    public ApiResult<Boolean> submitActivity(ActivitySubmitConfig config, User user) {
        try {
            Activity activity = config.getActivity();

            RuntimeConfig.setModeType(config.getMode());
            RuntimeConfig.updateQueueSettings(
                config.getSubmitCount(),
                config.getIntervalMs(),
                config.getLeadTimeMs()
            );

            log.info("提交活动: {} | 模式: {} | 提交{}次，间隔{}ms，提前{}ms",
                activity.getName(),
                config.getMode(),
                config.getSubmitCount(),
                config.getIntervalMs(),
                config.getLeadTimeMs());

            String activityDetailJson = co.xiaoyuboy.activity.DaoMengDetail.getActivityDetail(
                activity.getActivityId(), user);

            // ableJoinFlag 检查
            if (!jsonParsing.isActivity(activityDetailJson)) {
                String reason = "活动不可报名（ableJoinFlag!=1）";
                log.warn("{}: {}", activity.getName(), reason);
                LogManager.addLog("提交", activity.getName() + " 提交失败：" + reason);
                return ApiResult.fail(reason);
            }

            Long activityDetailTime = jsonParsing.getActivityDetailTime(activityDetailJson);
            String joinId = jsonParsing.getActivityJoinId(activityDetailJson);

            ActivityDetail activityDetail = new ActivityDetail(
                activity.getActivityId(),
                activityDetailTime,
                activity.getName(),
                activity.getStatusText(),
                joinId
            );

            DaoMengActivitySubmitManage.SubmitDaoMengManage(activityDetail, user);

            log.info("活动提交任务已启动");
            LogManager.addLog("提交", String.format("已提交任务：%s | 模式:%s | 次数:%d | 间隔:%dms | 提前:%dms",
                activity.getName(), config.getMode(), config.getSubmitCount(), config.getIntervalMs(), config.getLeadTimeMs()));
            return ApiResult.success(true);

        } catch (Exception e) {
            log.error("提交活动失败", e);
            String reason = "提交异常: " + e.getMessage();
            LogManager.addLog("提交", "提交异常: " + reason);
            return ApiResult.fail(reason);
        }
    }
}
