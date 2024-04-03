package co.xiaoyuboy.ThreadEntity;

import co.xiaoyuboy.activity.DaoMengActivitySubmit;
import co.xiaoyuboy.entity.User;
import lombok.extern.java.Log;


import java.util.concurrent.CountDownLatch;

/**
 * @Author: Smile
 * @Date: 2023-11-08 17:39
 * @Description: 提交任务
 */
@Log
public class DaoMengSubmitThread implements Runnable{
    private String activityId;
    private User user;
   private CountDownLatch latch;

    public DaoMengSubmitThread(String activityId, User user, CountDownLatch latch) {
        this.activityId = activityId;
        this.user = user;
        this.latch = latch;
    }

    @Override
    public void run() {
        DaoMengActivitySubmit daoMengActivitySubmit = new DaoMengActivitySubmit();
        String json = daoMengActivitySubmit.activtySubmit(activityId, user);
        log.info(Thread.currentThread().getName()+"---->线程提交结果"+json);
        // 每个任务执行完毕后，倒计时器减一
        latch.countDown();
    }
}
