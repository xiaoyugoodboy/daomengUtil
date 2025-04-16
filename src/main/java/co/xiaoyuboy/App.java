package co.xiaoyuboy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.activity.DaoMengActivitySubmitManage;
import co.xiaoyuboy.activity.DaoMengDetail;
import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.util.DaoMengActivityRecursionParsing;
import co.xiaoyuboy.util.DaoMengSmile;
import lombok.extern.java.Log;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaoyu
 * @date 2024-03-21 8:16
 */
@Log
public class App {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static String account;
    public static String pwd;

    public static void main(String[] args) throws IOException {
//        System.out.println(" .----------------.  .----------------.  .----------------.  .----------------.  .----------------. \n" +
//                "| .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |\n" +
//                "| |    _______   | || | ____    ____ | || |     _____    | || |   _____      | || |  _________   | |\n" +
//                "| |   /  ___  |  | || ||_   \\  /   _|| || |    |_   _|   | || |  |_   _|     | || | |_   ___  |  | |\n" +
//                "| |  |  (__ \\_|  | || |  |   \\/   |  | || |      | |     | || |    | |       | || |   | |_  \\_|  | |\n" +
//                "| |   '.___`-.   | || |  | |\\  /| |  | || |      | |     | || |    | |   _   | || |   |  _|  _   | |\n" +
//                "| |  |`\\____) |  | || | _| |_\\/_| |_ | || |     _| |_    | || |   _| |__/ |  | || |  _| |___/ |  | |\n" +
//                "| |  |_______.'  | || ||_____||_____|| || |    |_____|   | || |  |________|  | || | |_________|  | |\n" +
//                "| |              | || |              | || |              | || |              | || |              | |\n" +
//                "| '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |\n" +
//                " '----------------'  '----------------'  '----------------'  '----------------'  '----------------' ");
        //数据加密使用
        DaoMengSmile daoMengSmile = new DaoMengSmile();
        //数据解析使用
        JsonParsing jsonParsing = new JsonParsing();
        //时间格式化器
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //活动解析器
//        DaoMengActivity daoMengActivity = new DaoMengActivity(daoMengSmile);
        //活动详情解析器
        // DaoMengDetail daoMengDetail = new DaoMengDetail(daoMengSmile);
        System.out.println("\n\n=======DaoMeng系统启动（针对无验证码）======");
        System.out.println("程序仅供逆向学习交流，请于24小时内删除");
        System.out.print("账号:");
        account = new BufferedReader(new InputStreamReader(System.in)).readLine();
        System.out.print("密码:");
        pwd = new BufferedReader(new InputStreamReader(System.in)).readLine();
        //获取到登录请求体
        String data = daoMengSmile.getLoginData(account, pwd);
        //获得加密的请求题
        String signature = daoMengSmile.getSignature(data);
        //获取到登录请求头
        String head = daoMengSmile.getHead();
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/login/phone")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", head)
                .body(signature).execute().body();
        //解析出
        User user = jsonParsing.loginJsonParsing(json);
        log.info(account + "账号---->登录成功");
        //验证激活码
        verify();
        //利用解析算法获取出可以报名的活动
        List<Activity> activities = DaoMengActivityRecursionParsing.getActivityList(user);
        Map<String, ActivityDetail> map = new HashMap<>();
        int r = 0;
        for (int i = 0; i < activities.size(); i++) {
            Activity activity = activities.get(i);
            //获取一个活动就等待一秒
            try {
                log.info("获取到第" + i + "个活动，休息300毫秒(模拟人手点击)，在继续运行（会过滤出可以报名的活动）");
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //解析出活动详情
            String activityDetailJson = DaoMengDetail.getActivityDetail(activity.getActivityId(), user);
            //判断是否可以报名
            if (!jsonParsing.isActivity(activityDetailJson)) {
                continue;
            }
            //读取出活动开始时间
            Long activityDetailTime = jsonParsing.getActivityDetailTime(activityDetailJson);
            //读取出活动是否录取的joinId
            String joinId = jsonParsing.getActivityJoinId(activityDetailJson);
            map.put(((++r) + ""), new ActivityDetail(activity.getActivityId(), activityDetailTime, activity.getName(), activity.getStatusText(), joinId));
        }
        //打印出可以报名的活动
        map.forEach((key, value) -> {
            Long activityCreateTime = value.getActivityCreateTime();
            //转换成毫秒
            Instant instant = Instant.ofEpochMilli(activityCreateTime);
            // 使用ZoneId来指定时区，如果需要的话
            ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 例如：上海时区
            // 将Instant对象格式化为字符串
            String formattedDateTime = instant.atZone(zoneId).format(dateTimeFormatter);

            System.out.println("\n" + key + "--->活动名字:" + value.getName() + "----活动状态:" + value.getStatusText() + "------活动开始报名时间:" + formattedDateTime + "\n");
        });
        System.out.println("=============================================================");
        System.out.print("输入对应的数字即可:");
        String readLine = new BufferedReader(new InputStreamReader(System.in)).readLine();
        //获取到需要抓的活动的id
        String activityId = map.get(readLine).getActivityId();
        if (StrUtil.hasEmpty(activityId)) {
            throw new RuntimeException("你的输入不符合要求");
        }
        ActivityDetail activityDetail1 = map.get(readLine);
        String name = activityDetail1.getName();
        System.out.println("你选择的活动序号是--->" + readLine + "------活动名字是---->" + name + "\n------请你输入yes(纯小写)--确认活动-----输入其他结束程序");
        String readStr = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (!"yes".equals(readStr)) {
            return;
        }

        DaoMengActivitySubmitManage.SubmitDaoMengManage(activityDetail1, user);


    }
    private static final String BASE_URL = "http://74.48.116.4:5666/api/key";

    public static void verify() throws IOException {
        int i = 0;
        while (true){
            System.out.println("\n请输入激活码:");
            String generatedKey = new BufferedReader(new InputStreamReader(System.in)).readLine();
            // 2. 请求验证 Key
            String verifyUrl = BASE_URL + "/verify";
            HttpResponse verifyResponse = HttpRequest.post(verifyUrl)
                    .form("key", generatedKey) // 设置请求参数
                    .execute();
            JSONObject entries = JSONUtil.parseObj(verifyResponse.body());
            String str = entries.getStr("code");
            if (!str.equals("200")){
                System.out.println("激活码错误，请获取正确激活码------>"+generatedKey);
                if (i++>3){
                    System.out.println("激活码多次输入错误，退出程序");
                    System.exit(0);
                }

            }else if (str.equals("200")){
                System.out.println("激活码正确，开始抢活动------>"+generatedKey);
                break;
            }
        }
}
}
