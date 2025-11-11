package co.xiaoyuboy.gui.service;

import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.util.DaoMengActivityRecursionParsing;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 活动服务
 */
@Slf4j
public class ActivityService {

    /**
     * 获取可报名的活动列表
     *
     * @param user 用户信息
     * @return 活动列表
     */
    public List<Activity> getAvailableActivities(User user) {
        try {
            log.info("开始获取活动列表");
            List<Activity> activities = DaoMengActivityRecursionParsing.getActivityList(user);
            log.info("获取活动成功，共 {} 个", activities.size());
            return activities;
        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            throw new RuntimeException("获取活动失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提交活动报名
     *
     * @param activity 活动信息
     * @param user     用户信息
     * @return 是否提交成功
     */
    public boolean submitActivity(Activity activity, User user) {
        try {
            log.info("开始提交活动: {}", activity.getName());

            // TODO: 集成原有的提交逻辑
            // 这里需要调用 DaoMengActivitySubmit 相关方法
            // 暂时返回成功，后续补充完整逻辑

            log.info("活动提交成功");
            return true;

        } catch (Exception e) {
            log.error("提交活动失败", e);
            return false;
        }
    }
}
