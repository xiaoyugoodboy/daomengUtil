package co.xiaoyuboy.ThreadEntity;

import cn.hutool.json.JSONObject;
import co.xiaoyuboy.activity.DaoMengDetail;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.queue.DelayQueue;
import co.xiaoyuboy.queue.Delayed;
import co.xiaoyuboy.queue.Queue;
import co.xiaoyuboy.util.BodyUtil;
import co.xiaoyuboy.util.LogConfigurator;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static co.xiaoyuboy.util.LogConfigurator.configure;

/**
 * @author xiaoyu
 * @date 2024-03-21 15:46
 * @Description:策略:用延迟队列间隔一毫秒提交一次
 */
public class DaoMengDelayQueueThread implements Runnable {
    private ActivityDetail activityDetail;
    private User user;
    //创建一个任务队列
    private Queue<Job> queue = new DelayQueue();
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(16, 50, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));
    Logger log;

    public DaoMengDelayQueueThread(ActivityDetail activityDetail, User user) {
        this.activityDetail = activityDetail;
        this.user = user;
        configure();
        // 以下是测试代码
        log = Logger.getLogger(LogConfigurator.class.getName());

    }

    /**
     * @param count 任务数量
     * @param time  间隔时间(sss)
     */
    private void initDelayQueue(long count, long time, long leadTime) {
        boolean falg = false;
        //活动开始时间
        Long activityCreateTime = activityDetail.getActivityCreateTime();
        long timeMillis = System.currentTimeMillis();

        if (timeMillis > activityCreateTime) {
            falg = true;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        BodyUtil bodyUtil = new BodyUtil();
        //活动id
        String activityId = activityDetail.getActivityId();
        for (int i = 1; i <= count; i++) {//构建相应数量的活动
            if (falg) {//如果活动已经开始了
                //计算每一个活动延迟时间,就用当前系统时间
                Long timestamp = i * time + System.currentTimeMillis();

                Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
                //任务开始时间
                job.setBegin(activityCreateTime);
                //任务延迟时间
                job.setDelayTime(i * time);
                //添加到延迟队列
                queue.add(job);
            } else {
                //计算每一个活动延迟时间
                Long timestamp = (i - 1) * time + activityCreateTime;
                Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
                //计算正确的提前发送时间
//                long beginTime = activityCreateTime - leadTime;
                //不提前
                long beginTime = activityCreateTime;
                //任务开始时间
                job.setBegin(beginTime);
                //任务延迟时间
                job.setDelayTime((i - 1) * time);
                //添加到延迟队列
                queue.add(job);
            }
        }


        Instant instant = null;
        //转换成毫秒
        if (falg) {
            instant = Instant.ofEpochMilli(timeMillis);
            //添加一个提前发送的任务
        } else {
//            getLeadTimeJob(activityCreateTime, leadTime);
            instant = Instant.ofEpochMilli(activityCreateTime);
        }
        // 使用ZoneId来指定时区，如果需要的话
        ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 例如：上海时区
        // 将Instant对象格式化为字符串
        String formattedDateTime = instant.atZone(zoneId).format(dateTimeFormatter);
        log.info("成功构建" + queue.size() + "个任务" + "第一个任务开始时间是--->" + formattedDateTime);
    }

    /**
     * 得到一个提前发送的请求
     *
     * @return
     */
    public void getLeadTimeJob(long activityCreateTime, long leadTime) {
        BodyUtil bodyUtil = new BodyUtil();
        //活动id
        String activityId = activityDetail.getActivityId();
        Long timestamp = activityCreateTime;
        Job job = bodyUtil.getSignatureData(activityId, this.user, timestamp);
        //计算正确的提前发送时间
        long beginTime = activityCreateTime - leadTime;
        //任务开始时间
        job.setBegin(beginTime);
        //任务延迟时间
        job.setDelayTime(0L);
        //添加到延迟队列
        queue.add(job);

    }

    @Override
    public void run() {
        //初始化队列 3个任务,间隔2毫秒,提前2毫秒发送
        //this.initDelayQueue(3, 2, 4);
        while (true) {
            //系统当前毫秒值
            long timeMillis = System.currentTimeMillis();
            //活动开始时间
            Long activityCreateTime = activityDetail.getActivityCreateTime();
            if (timeMillis > activityCreateTime) {
                log.info("开始发送请求--->系统时间--->" + timeMillis + "---计算得到的活动开始时间---->" + activityCreateTime);
                //那么活动已经开始了,多线程直接提交
                this.initDelayQueue(1, 2, 4);
                daoMengSubmit(activityDetail.getActivityId(), user);
                break;
            } else if ((activityCreateTime - timeMillis) < (1000 * 10)) {
                //初始化队列 3个任务,间隔2毫秒,提前2毫秒发送
                log.info("距离活动开始还有10秒--->已经初始化队列" + System.currentTimeMillis());
                this.initDelayQueue(1, 2, 4);
                log.info("---->唤醒延迟队列等待提交中" + System.currentTimeMillis());
                daoMengSubmit(activityDetail.getActivityId(), user);
                break;
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
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("退出程序");
        //结束程序运行
        System.exit(0);
    }

    public static void main(String[] args) {

        System.out.println(System.currentTimeMillis());
        // java >= 8
        // 创建一个ZoneId对象代表北京时区
        ZoneId beijingZoneId = ZoneId.of("Asia/Shanghai");

        // 通过ZonedDateTime获取北京的当前准确时间
        ZonedDateTime zonedDateTime = ZonedDateTime.now(beijingZoneId);
        // 将ZonedDateTime转换为Instant
        Instant instant = zonedDateTime.toInstant();

        // 获取自1970年1月1日0时0分0秒（UTC）以来的毫秒数
        long millis = instant.toEpochMilli();
        System.out.println(millis);


    }

    /**
     * 提交活动
     *
     * @param activityId
     * @param user
     */
    public void daoMengSubmit(String activityId, User user) {
        int maximumNumber=6;
        boolean falg = true;
        Long timeMillis = System.currentTimeMillis();

        while (true) {
            Job poll = queue.poll();
            //只执行一次
            if (falg){
                timeMillis = System.currentTimeMillis()+1000*15;
                falg=false;
            }

            if (null == poll&&System.currentTimeMillis()>timeMillis) {
                if (queue.size() <= 0) {
                    //延迟三秒查看详细
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
//                    sendXinxi("任务完成","延迟队列为空,程序退出");
                    JsonParsing jsonParsing = new JsonParsing();
                    String activityDetailJson = DaoMengDetail.getActivityDetail(activityId, user);
                  //  log.info("详细日志--->" + activityDetailJson);
                    System.out.println("正在为您查询" + activityDetail.getName() + "活动的详细情况------->");
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
                    //结束程序运行
                    System.exit(0);
                    break;
//                    System.out.println("-----输入任意字符退出------");
//                    try {
//                        new BufferedReader(new InputStreamReader(System.in)).readLine();
//                        break;
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }

                }
//                //延迟读取避免一直占用cpu
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
////                    sendXinxi("异常","线程出现异常继续执行---->");
//                }
            } else {
                if (null != poll){
                    //开启多线程执行任务
//                threadPool.execute(new DaoMengDelayQueueSubmitThread(poll));
                    //第二种方式提交任务
                    if ((maximumNumber--)<0){
                        break;
                    }
                    threadPool.execute(new DaoMengDelayQueueSubmitThread(poll, activityId, user, queue));
                }
            }
        }
    }
}
