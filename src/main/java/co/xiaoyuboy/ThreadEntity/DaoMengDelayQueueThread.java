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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 延迟队列控制线程，负责在指定时间节点提交报名请求。
 */
public class DaoMengDelayQueueThread implements Runnable {
    private final ActivityDetail activityDetail;
    private final User user;
    private final Queue<Job> queue;
    private final ThreadPoolExecutor threadPool;
    Logger log;

    public DaoMengDelayQueueThread(ActivityDetail activityDetail, User user) {
        this.activityDetail = activityDetail;
        this.user = user;
        this.queue = new DelayQueue<>();
        this.threadPool = new ThreadPoolExecutor(16, 50, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));
        LogConfigurator.configure();
        this.log = Logger.getLogger(LogConfigurator.class.getName());
    }

    private void initDelayQueue(long submitCount, long intervalMs, long leadTimeMs) {
        boolean started = System.currentTimeMillis() > activityDetail.getActivityCreateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        BodyUtil bodyUtil = new BodyUtil();
        String activityId = activityDetail.getActivityId();
        for (int i = 1; i <= submitCount; i++) {
            Job job;
            if (started) {
                long timestamp = System.currentTimeMillis() + i * intervalMs;
                job = bodyUtil.getSignatureData(activityId, user, timestamp);
                job.setBegin(activityDetail.getActivityCreateTime());
                job.setDelayTime(i * intervalMs);
            } else {
                long timestamp = activityDetail.getActivityCreateTime() + (i - 1L) * intervalMs;
                job = bodyUtil.getSignatureData(activityId, user, timestamp);
                job.setBegin(activityDetail.getActivityCreateTime());
                job.setDelayTime((i - 1L) * intervalMs);
            }
            queue.add(job);
        }
        Instant target = started ? Instant.ofEpochMilli(System.currentTimeMillis())
                : Instant.ofEpochMilli(activityDetail.getActivityCreateTime());
        if (!started) {
            getLeadTimeJob(activityDetail.getActivityCreateTime(), leadTimeMs);
        }
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        String formattedDateTime = target.atZone(zoneId).format(formatter);
        log.info("当前队列长度: " + queue.size() + " | 预计开始时间: " + formattedDateTime);
    }

    public void getLeadTimeJob(long activityCreateTime, long leadTimeMs) {
        if (leadTimeMs <= 0) {
            return;
        }
        BodyUtil bodyUtil = new BodyUtil();
        String activityId = activityDetail.getActivityId();
        Job job = bodyUtil.getSignatureData(activityId, user, activityCreateTime);
        long begin = activityCreateTime - leadTimeMs;
        job.setBegin(begin);
        job.setDelayTime(0L);
        queue.add(job);
    }

    @Override
    public void run() {
        QueueSettings queueSettings = RuntimeConfig.getQueueSettings();
        if (queueSettings.getSubmitCount() <= 0) {
            throw new IllegalStateException("提交任务数量不能小于等于0，请配置参数后再提交");
        }
        if (RuntimeConfig.isCaptchaEnabled()) {
            runWithCaptcha(queueSettings);
        } else {
            runManual(queueSettings);
        }
    }

    private void runManual(QueueSettings queueSettings) {
        initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
        while (true) {
            long now = System.currentTimeMillis();
            long start = activityDetail.getActivityCreateTime();
            if (now > start) {
                log.info(now + "---->活动已开始，直接提交");
                daoMengSubmitManual(activityDetail.getActivityId(), user);
                break;
            } else if (start - now < 3000) {
                log.info(System.currentTimeMillis() + "---->进入三秒倒计时直接提交");
                daoMengSubmitManual(activityDetail.getActivityId(), user);
                break;
            } else if (start - now > 20000) {
                log.info("活动等待中......距离开始还有->" + (start - now) / 1000 + "秒");
                sleepSilently(10000);
            }
        }
        sleepSilently(10000);
        System.out.println("关闭循环");
        System.exit(0);
    }

    private void runWithCaptcha(QueueSettings queueSettings) {
        LocalCaptchaService.ensureInitialized().warmUpSampleIfNeeded();
        while (true) {
            long now = System.currentTimeMillis();
            long start = activityDetail.getActivityCreateTime();
            if (now > start) {
                LocalCaptchaService.ensureInitialized().triggerSampleWarmUp();
                log.info(now + "---->活动已开始，直接提交");
                initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
                daoMengSubmitWithRetry(activityDetail.getActivityId(), user);
                break;
            } else if (start - now < 10000) {
                LocalCaptchaService.ensureInitialized().triggerSampleWarmUp();
                log.info(System.currentTimeMillis() + "---->进入验证码预热，倒计时10秒内直接提交");
                initDelayQueue(queueSettings.getSubmitCount(), queueSettings.getIntervalMs(), queueSettings.getLeadTimeMs());
                log.info(System.currentTimeMillis() + "---->开始提交验证码任务");
                daoMengSubmitWithRetry(activityDetail.getActivityId(), user);
                break;
            } else if (start - now > 20000) {
                log.info("活动等待中......距离开始还有->" + (start - now) / 1000 + "秒");
                sleepSilently(10000);
            } else {
                // 时间窗口在10s~20s之间时，持续等待
            }
        }
        sleepSilently(10000);
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
            Job job = queue.poll();
            if (job != null) {
                threadPool.execute(new DaoMengDelayQueueSubmitThread(job));
                continue;
            }
            if (queue.size() > 0) {
                continue;
            }
            sleepSilently(3000);
            inspectResultAndExit(activityId, user);
            return;
        }
    }

    private void daoMengSubmitWithRetry(String activityId, User user) {
        int retry = 6;
        boolean firstRound = true;
        long waitUntil = System.currentTimeMillis();
        while (true) {
            Job job = queue.poll();
            if (firstRound) {
                waitUntil = System.currentTimeMillis() + 15000;
                firstRound = false;
            }
            if (job == null) {
                if (System.currentTimeMillis() > waitUntil && queue.size() <= 0) {
                    sleepSilently(3000);
                    inspectResultAndExit(activityId, user);
                    return;
                }
            } else {
                if (retry-- < 0) {
                    return;
                }
                threadPool.execute(new DaoMengDelayQueueSubmitThread(job, activityId, user, queue));
            }
        }
    }

    private void inspectResultAndExit(String activityId, User user) {
        JsonParsing jsonParsing = new JsonParsing();
        String activityDetailJson = DaoMengDetail.getActivityDetail(activityId, user);
        System.out.println("正在为您查询" + activityDetail.getName() + "活动的详细情况------>");
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)) {
            System.out.println("活动详情获取失败---->正在退出程序(自行登录app查看)");
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
                System.out.println("活动Id获取不到---->正在退出程序(自行登录app查看)");
            }
        }
        System.out.println("联系作者:2839706399(QQ)|by_Smlie(微信)\n\n");
        System.exit(0);
    }
}
