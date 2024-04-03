
package co.xiaoyuboy.activity;


import co.xiaoyuboy.ThreadEntity.DaoMengDelayQueueThread;
import co.xiaoyuboy.ThreadEntity.DaoMengManageThread;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.User;

/**
 * @Author: Smile
 * @Date: 2023-11-08 16:56
 * @Description: 管理提交任务的线程
 */
public class DaoMengActivitySubmitManage {

//    static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    /**
     * 任务提交调度管理器
     * @param activityDetail
     * @param user
     */
    public static void SubmitDaoMengManage(ActivityDetail activityDetail, User user){
        //多线程方式
//        new Thread(new DaoMengManageThread(activityDetail,user)).start();
        //延迟队列方式
        new Thread(new DaoMengDelayQueueThread(activityDetail,user)).start();
    }
}
