package co.xiaoyuboy.ThreadEntity;

import cn.hutool.http.HttpUtil;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.Job;
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
    public DaoMengDelayQueueSubmitThread(Job job) {
        configure();
        // 以下是测试代码
         log = Logger.getLogger(LogConfigurator.class.getName());
        this.job = job;
    }

    @Override
    public void run() {
      log.info("线程开始提交--->"+System.currentTimeMillis()+"----job信息"+job);
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/signup/submit")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(this.job.getBody())
                .execute().body();
        log.info("线程提交结果--->"+json+"----job信息--->"+job.toString());
    }
}
