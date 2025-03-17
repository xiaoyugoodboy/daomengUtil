package co.xiaoyuboy.ThreadEntity;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.queue.Queue;
import co.xiaoyuboy.util.BodyUtil;
import co.xiaoyuboy.util.LogConfigurator;
import lombok.extern.java.Log;

import java.util.logging.Logger;

import static co.xiaoyuboy.util.LogConfigurator.configure;

/**
 * @author xiaoyu
 * @date 2024-03-21 16:48
 */

public class DaoMengDelayQueueSubmitThread implements Runnable{
    //请求体
    private Job job;
    Logger log;
    private String activityId;
    private User user;
    private BodyUtil bodyUtil = new BodyUtil();
    private Queue<Job> queue;
    public DaoMengDelayQueueSubmitThread(Job job) {
        configure();
        // 以下是测试代码
         log = Logger.getLogger(LogConfigurator.class.getName());
        this.job = job;
    }
    public DaoMengDelayQueueSubmitThread(Job job,String activityId, User user,Queue<Job> queue) {
        configure();
        // 以下是测试代码
        log = Logger.getLogger(LogConfigurator.class.getName());
        this.job = job;
        this.activityId = activityId;
        this.user = user;
        this.queue = queue;
    }

    @Override
    public void run() {
      log.info(Thread.currentThread().getName()+"线程开始提交--->"+System.currentTimeMillis());
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/signup/submit")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(this.job.getBody())
                .execute().body();
        JSONObject entries = JSONUtil.parseObj(json);
        String str = entries.getStr("code");
        if (!str.equals("100")){
            //计算每一个活动延迟时间,就用当前系统时间
            Long timestamp =System.currentTimeMillis();
            Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
            //任务开始时间
            job.setBegin(timestamp);
            //任务延迟时间
            job.setDelayTime(0L);
            //添加到延迟队列
            queue.add(job);
            log.info("正在添加队列-->");
        }

        log.info("线程提交结果--->"+json);

    }
}
