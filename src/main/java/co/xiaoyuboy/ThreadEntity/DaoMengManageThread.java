package co.xiaoyuboy.ThreadEntity;

import cn.hutool.json.JSONObject;
import co.xiaoyuboy.activity.DaoMengDetail;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import lombok.extern.java.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Smile
 * @Date: 2023-11-08 17:33
 * @Description: 策略:多线程提交
 *
 */
@Log
public class DaoMengManageThread implements Runnable {
    private ActivityDetail activityDetail;
    private User user;
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(16, 50, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));

    public DaoMengManageThread(ActivityDetail activityDetail, User user) {
        this.activityDetail = activityDetail;
        this.user = user;
    }

    @Override
    public void run() {
        while (true) {
            //系统当前毫秒值
            long timeMillis = System.currentTimeMillis();
            //活动开始时间
            Long activityCreateTime = activityDetail.getActivityCreateTime();
            if (timeMillis > activityCreateTime) {
                //那么活动已经开始了,多线程直接提交
                daoMengSubmit(activityDetail.getActivityId(), user);
                return;
            } else if ((activityCreateTime - timeMillis) < 1200) {
                //提前三秒开始提交
                daoMengSubmit(activityDetail.getActivityId(), user);
                return;
            } else {
                if ((activityCreateTime - timeMillis) > (1000 * 20)) {
                    log.info("活动等待中.......距离开始还有-->" + (activityCreateTime - timeMillis) / 1000 + "秒");
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

    /**
     * 提交活动
     *
     * @param activityId
     * @param user
     */
    public void daoMengSubmit(String activityId, User user) {
        // 创建一个倒计时器，初始值为30
        CountDownLatch latch = new CountDownLatch(30);
        // 记录程序开始时间
        long startTime = System.currentTimeMillis();
        // 定义一个变量，记录执行次数
        int count = 0;
        // 定义一个循环，每次执行30个线程任务
        while (true) {
            // 提交30个线程任务
            for (int i = 0; i < 30; i++) {
                threadPool.execute(new DaoMengSubmitThread(activityId, user,latch));
                // 记录执行次数
                count++;
            }
            // 等待所有任务执行完毕，或者超过10秒
            try {
                boolean completed = latch.await(10, TimeUnit.SECONDS);
                if (!completed) {
                    log.info("Not all tasks completed within the timeout.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 重置中断状态
                log.info("Thread was interrupted---->"+e.getMessage());
            }
            // 打印执行情况
            log.info("执行了" + count + "次，耗时" + (System.currentTimeMillis() - startTime) + "毫秒");
            // 判断是否超过10秒
            if (System.currentTimeMillis() - startTime > 10 * 1000) {
                // 超过10秒，结束程序
                log.info("已经发起请求超过10秒，正在关闭线程池，具体还是速度受到网络瓶颈\n\n");
                break;
            } else {
                // 没有超过10秒，重置倒计时器，继续执行30次
                latch = new CountDownLatch(30);
            }
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // 取消当前执行的任务
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt(); // 重置中断状态
        }
        JsonParsing jsonParsing = new JsonParsing();
        String activityDetailJson = DaoMengDetail.getActivityDetail(activityDetail.getActivityId(), user);
        System.out.println("正在为您查询"+activityDetail.getName()+"活动的详细情况------->");
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
            System.out.println("活动详情获取失败---->正在退出程序(自行登录app查看)");
        }else{
            String joinId=jsonParsing.getActivityJoinId(activityDetailJson);
            if (!"0".equals(joinId)){
                activityDetail.setJoinId(joinId);
                boolean isSuccess=DaoMengDetail.isActivitySuccess(activityDetail,user);
                if (isSuccess){
                    System.out.println(activityDetail.getName()+"---->活动已被录取");
                }else{
                    System.out.println(activityDetail.getName()+"---->未被录取或者处于待录取状态(影响因素很多)");
                }
            }else{
                System.out.println("活动Id获取不到---->正在退出程序(自行登录app查看)");
            }


        }
        System.out.println("联系作者:2839706399(QQ)|by_Smlie(微信)\n\n");
        System.out.println("-----输入任意字符退出------");
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
