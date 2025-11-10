package co.xiaoyuboy.ThreadEntity;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.config.RuntimeConfig;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.queue.Queue;
import co.xiaoyuboy.util.BodyUtil;
import co.xiaoyuboy.util.LogConfigurator;

import java.util.logging.Logger;

import static co.xiaoyuboy.util.LogConfigurator.configure;

/**
 * 延迟队列提交线程，支持失败后重新入队。
 */
public class DaoMengDelayQueueSubmitThread implements Runnable {
    private final Job job;
    private final String activityId;
    private final User user;
    private final Queue<Job> queue;
    private final BodyUtil bodyUtil = new BodyUtil();
    Logger log;

    public DaoMengDelayQueueSubmitThread(Job job) {
        this(job, null, null, null);
    }

    public DaoMengDelayQueueSubmitThread(Job job, String activityId, User user, Queue<Job> queue) {
        configure();
        log = Logger.getLogger(LogConfigurator.class.getName());
        this.job = job;
        this.activityId = activityId;
        this.user = user;
        this.queue = queue;
    }

    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + "线程开始提交--->" + System.currentTimeMillis());
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/signup/submit")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(this.job.getBody())
                .execute().body();
        if (RuntimeConfig.shouldRetryOnFailure() && activityId != null && user != null && queue != null) {
            JSONObject entries = JSONUtil.parseObj(json);
            String code = entries.getStr("code");
            if (!"100".equals(code)) {
                Long timestamp = System.currentTimeMillis();
                Job retryJob = bodyUtil.getSignatureData(activityId, this.user, timestamp);
                retryJob.setBegin(timestamp);
                retryJob.setDelayTime(0L);
                queue.add(retryJob);
                log.info("提交失败，重新入队 activityId=" + activityId);
            }
        }
        log.info("线程提交结果--->" + json);
    }
}
