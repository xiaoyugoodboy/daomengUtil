package co.xiaoyuboy.ThreadEntity;

import cn.hutool.json.JSONObject;
import co.xiaoyuboy.activity.DaoMengDetail;
import co.xiaoyuboy.captcha.LocalCaptchaService;
import co.xiaoyuboy.config.RuntimeConfig;
import co.xiaoyuboy.config.RuntimeConfig.QueueSettings;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.queue.DelayQueue;
import co.xiaoyuboy.queue.Queue;
import co.xiaoyuboy.util.BodyUtil;
import co.xiaoyuboy.util.LogConfigurator;
import lombok.extern.java.Log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static co.xiaoyuboy.util.LogConfigurator.configure;

/**
 * 延迟队列线程，支持无验证码和验证码识别模式。
 */
@Log
public class DaoMengDelayQueueThread implements Runnable {
    private final ActivityDetail activityDetail;
    private final User user;
    private final Queue<Job> queue = new DelayQueue();
    private final ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(16, 50, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));
    Logger log;

    public DaoMengDelayQueueThread(ActivityDetail activityDetail, User user) {
        this.activityDetail = activityDetail;
        this.user = user;
        configure();
        log = Logger.getLogger(LogConfigurator.class.getName());
    }

    private void initDelayQueue(long count, long time, long leadTime) {
        boolean started = false;
        Long activityCreateTime = activityDetail.getActivityCreateTime();
        long now = System.currentTimeMillis();
        if (now > activityCreateTime) {
            started = true;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        BodyUtil bodyUtil = new BodyUtil();
        String activityId = activityDetail.getActivityId();
        for (int i = 1; i <= count; i++) {
            if (started) {
                Long timestamp = i * time + System.currentTimeMillis();
                Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
                job.setBegin(activityCreateTime);
                job.setDelayTime(i * time);
                queue.add(job);
            } else {
                Long timestamp = (i - 1) * time + activityCreateTime;
                Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
                long beginTime = activityCreateTime;
                job.setBegin(beginTime);
                job.setDelayTime((i - 1) * time);
                queue.add(job);
            }
        }
        Instant instant;
        if (started) {
            instant = Instant.ofEpochMilli(now);
        } else {
            getLeadTimeJob(activityCreateTime, leadTime);
            instant = Instant.ofEpochMilli(activityCreateTime);
        }
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        String formattedDateTime = instant.atZone(zoneId).format(dateTimeFormatter);
        log.info("成功构建" + queue.size() + "个任务，第一个任务开始时间--->" + formattedDateTime);
    }

    /**
     * 额外添加一个提前发送的任务。
     */
    public void getLeadTimeJob(long activityCreateTime, long leadTime) {
        if (leadTime <= 0) {
            return;
        }
        BodyUtil bodyUtil = new BodyUtil();
        String activityId = activityDetail.getActivityId();
        Long timestamp = activityCreateTime;
        Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
        long beginTime = activityCreateTime - leadTime;
        job.setBegin(beginTime);
        job.setDelayTime(0L);
        queue.add(job);
    }

    @Override
    public void run() {
        QueueSettings queueSettings = RuntimeConfig.getQueueSettings();
        if (queueSettings.getSubmitCount() <= 0) {
            throw new IllegalStateException("提交任务数量必须大于0，请重新配置参数后再启动程序。");
        }
        if (RuntimeConfig.isCaptchaEnabled()) {
            runWithCaptcha(queueSettings);
        } else {
            runManual(queueSettings);
        }
    }

    private void runManual(QueueSettings queueSettings) {
        this.initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
        while (true) {
            long timeMillis = System.currentTimeMillis();
            Long activityCreateTime = activityDetail.getActivityCreateTime();
            if (timeMillis > activityCreateTime) {
                log.info("开始发送请求--->系统时间--->" + timeMillis + "---计算得到的活动开始时间---->" + activityCreateTime);
                daoMengSubmitManual(activityDetail.getActivityId(), user);
                break;
            } else if ((activityCreateTime - timeMillis) < (1000 * 3)) {
                log.info("距离活动开始还有三秒---->唤醒延迟队列等待" + System.currentTimeMillis());
                daoMengSubmitManual(activityDetail.getActivityId(), user);
                break;
            } else if ((activityCreateTime - timeMillis) > (1000 * 20)) {
                log.info("活动等待中......距离开始还有---->" + (activityCreateTime - timeMillis) / 1000 + "秒");
                sleepSilently(1000 * 10);
            }
        }
        sleepSilently(1000 * 10);
        System.out.println("关闭循环");
        System.exit(0);
    }

    private void runWithCaptcha(QueueSettings queueSettings) {
        LocalCaptchaService.ensureInitialized().warmUpSampleIfNeeded();
        while (true) {
            long timeMillis = System.currentTimeMillis();
            Long activityCreateTime = activityDetail.getActivityCreateTime();
            if (timeMillis > activityCreateTime) {
                LocalCaptchaService.ensureInitialized().triggerSampleWarmUp();
                log.info("开始发送请求--->系统时间--->" + timeMillis + "---计算得到的活动开始时间---->" + activityCreateTime);
                this.initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
                daoMengSubmitWithRetry(activityDetail.getActivityId(), user);
                break;
            } else if ((activityCreateTime - timeMillis) < (1000 * 10)) {
                LocalCaptchaService.ensureInitialized().triggerSampleWarmUp();
                log.info("距离活动开始还有10秒--->已经初始化队列" + System.currentTimeMillis());
                this.initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
                log.info("---->唤醒延迟队列等待提交中" + System.currentTimeMillis());
                daoMengSubmitWithRetry(activityDetail.getActivityId(), user);
                break;
            } else if ((activityCreateTime - timeMillis) > (1000 * 20)) {
                log.info("活动等待中......距离开始还有---->" + (activityCreateTime - timeMillis) / 1000 + "秒");
                sleepSilently(1000 * 10);
            }
        }
        sleepSilently(1000 * 10);
        System.out.println("退出程序");
        System.exit(0);
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void daoMengSubmitManual(String activityId, User user) {
        while (true) {
            Job poll = queue.poll();
            if (poll == null) {
                if (queue.size() <= 0) {
                    sleepSilently(3000);
                    inspectResultAndExit(activityId, user);
                    break;
                }
            } else {
                threadPool.execute(new DaoMengDelayQueueSubmitThread(poll));
            }
        }
    }

    private void daoMengSubmitWithRetry(String activityId, User user) {
        int maximumNumber = 6;
        boolean firstLoop = true;
        long expireTime = System.currentTimeMillis();
        while (true) {
            Job poll = queue.poll();
            if (firstLoop) {
                expireTime = System.currentTimeMillis() + 1000 * 15;
                firstLoop = false;
            }
            if (poll == null && System.currentTimeMillis() > expireTime) {
                if (queue.size() <= 0) {
                    sleepSilently(3000);
                    inspectResultAndExit(activityId, user);
                    break;
                }
            } else if (poll != null) {
                if ((maximumNumber--) < 0) {
                    break;
                }
                threadPool.execute(new DaoMengDelayQueueSubmitThread(poll, activityId, user, queue));
            }
        }
    }

    private void inspectResultAndExit(String activityId, User user) {
        JsonParsing jsonParsing = new JsonParsing();
        String activityDetailJson = DaoMengDetail.getActivityDetail(activityId, user);
        System.out.println("正在为您查询" + activityDetail.getName() + "活动的详细情况------->");
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)) {
            System.out.println("活动详情获取失败---->正在退出程序(自行登录APP查看)");
        } else {
            String joinId = jsonParsing.getActivityJoinId(activityDetailJson);
            if (!"0".equals(joinId)) {
                activityDetail.setJoinId(joinId);
                boolean isSuccess = DaoMengDetail.isActivitySuccess(activityDetail, user);
                if (isSuccess) {
                    System.out.println(activityDetail.getName() + "---->活动已被录取");
                } else {
                    System.out.println(activityDetail.getName() + "---->未被录取或者处于待录取状态(影响因素很多)");
                }
            } else {
                System.out.println("活动Id获取不到---->正在退出程序(自行登录APP查看)");
            }
        }
        System.exit(0);
    }
}
