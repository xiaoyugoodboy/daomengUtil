package co.xiaoyuboy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.activity.DaoMengActivitySubmitManage;
import co.xiaoyuboy.activity.DaoMengDetail;
import co.xiaoyuboy.config.NoCaptchaCustomConfig;
import co.xiaoyuboy.config.RuntimeConfig;
import co.xiaoyuboy.config.RuntimeConfig.ModeType;
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
    private static final String BASE_URL = "http://38.207.176.57:5666/api/key";
    private static final long DEFAULT_CAPTCHA_SUBMIT_COUNT = 1L;
    private static final long DEFAULT_CAPTCHA_INTERVAL_MS = 2L;
    private static final long DEFAULT_CAPTCHA_LEAD_TIME_MS = 4L;
    private static final long DEFAULT_NO_CAPTCHA_SUBMIT_COUNT = 5L;
    private static final long DEFAULT_NO_CAPTCHA_INTERVAL_MS = 2L;
    private static final long DEFAULT_NO_CAPTCHA_LEAD_TIME_MS = 0L;

    public static void main(String[] args) throws IOException {
        System.out.println(" .----------------.  .----------------.  .----------------.  .----------------.  .----------------. \n" +
                "| .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |\n" +
                "| |    _______   | || | ____    ____ | || |     _____    | || |   _____      | || |  _________   | |\n" +
                "| |   /  ___  |  | || ||_   \\  /   _|| || |    |_   _|   | || |  |_   _|     | || | |_   ___  |  | |\n" +
                "| |  |  (__ \\_|  | || |  |   \\/   |  | || |      | |     | || |    | |       | || |   | |_  \\_|  | |\n" +
                "| |   '.___`-.   | || |  | |\\  /| |  | || |      | |     | || |    | |   _   | || |   |  _|  _   | |\n" +
                "| |  |`\\____) |  | || | _| |_\\/_| |_ | || |     _| |_    | || |   _| |__/ |  | || |  _| |___/ |  | |\n" +
                "| |  |_______.'  | || ||_____||_____|| || |    |_____|   | || |  |________|  | || | |_________|  | |\n" +
                "| |              | || |              | || |              | || |              | || |              | |\n" +
                "| '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |\n" +
                " '----------------'  '----------------'  '----------------'  '----------------'  '----------------' ");
        DaoMengSmile daoMengSmile = new DaoMengSmile();
        JsonParsing jsonParsing = new JsonParsing();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\n\n=======DaoMeng系统启动======");
        System.out.println("程序仅供逆向学习交流，请于24小时内删除");
        configureMode(reader);
        System.out.print("账号:");
        account = reader.readLine();
        System.out.print("密码:");
        pwd = reader.readLine();
        String data = daoMengSmile.getLoginData(account, pwd);
        String signature = daoMengSmile.getSignature(data);
        String head = daoMengSmile.getHead();
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/login/phone")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", head)
                .body(signature).execute().body();
        User user = jsonParsing.loginJsonParsing(json);
        log.info(account + "账号---->登录成功");
        verifyLicense(reader);
        //利用解析算法获取出可以报名的活动
        List<Activity> activities = DaoMengActivityRecursionParsing.getActivityList(user);
        Map<String, ActivityDetail> map = new HashMap<>();
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
            map.put((activity.getActivityId() + ""), new ActivityDetail(activity.getActivityId(), activityDetailTime, activity.getName(), activity.getStatusText(), joinId));
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

            System.out.println("\n活动ID--->" + key + "--->活动名字:" + value.getName() + "----活动状态:" + value.getStatusText() + "------活动开始报名时间:" + formattedDateTime + "\n");
        });
        System.out.println("=============================================================");
        System.out.print("输入对应的活动Id即可:");
        String readLine = reader.readLine();
        ActivityDetail activityDetail1 = map.get(readLine);
        if (activityDetail1 == null) {
            throw new RuntimeException("未找到对应的活动Id");
        }
        String activityId = activityDetail1.getActivityId();
        if (StrUtil.hasEmpty(activityId)) {
            throw new RuntimeException("你的输入不符合要求");
        }
        String name = activityDetail1.getName();
        System.out.println("你选择的活动ID是--->" + readLine + "------活动名字是---->" + name + "\n------请你输入yes(纯小写)--确认活动-----输入其他结束程序");
        String readStr = reader.readLine();
        if (!"yes".equals(readStr)) {
            return;
        }

        DaoMengActivitySubmitManage.SubmitDaoMengManage(activityDetail1, user);
    }

    private static void configureMode(BufferedReader reader) throws IOException {
        while (true) {
            System.out.println("\n请选择运行模式: ");
            System.out.println("1. 无验证码模式");
            System.out.println("2. 验证码识别模式");
            System.out.print("请输入数字选择模式:");
            String choice = reader.readLine();
            if ("1".equals(choice)) {
                configureNoCaptchaMode(reader);
                break;
            } else if ("2".equals(choice)) {
                RuntimeConfig.setModeType(ModeType.CAPTCHA);
                RuntimeConfig.updateQueueSettings(DEFAULT_CAPTCHA_SUBMIT_COUNT, DEFAULT_CAPTCHA_INTERVAL_MS, DEFAULT_CAPTCHA_LEAD_TIME_MS);
                System.out.println("已启用验证码识别模式，系统将使用预设参数运行。");
                break;
            } else {
                System.out.println("输入无效，请重新输入。");
            }
        }
    }

    private static void configureNoCaptchaMode(BufferedReader reader) throws IOException {
        while (true) {
            System.out.println("\n无验证码模式参数选择:");
            System.out.println("1. 默认参数（推荐）");
            System.out.println("2. 自定义参数（读取/修改配置后可回车使用默认值）");
            System.out.print("请输入数字选择模式:");
            String choice = reader.readLine();
            if ("1".equals(choice)) {
                RuntimeConfig.setModeType(ModeType.NO_CAPTCHA_DEFAULT);
                RuntimeConfig.updateQueueSettings(
                        DEFAULT_NO_CAPTCHA_SUBMIT_COUNT,
                        DEFAULT_NO_CAPTCHA_INTERVAL_MS,
                        DEFAULT_NO_CAPTCHA_LEAD_TIME_MS);
                System.out.println("已启用无验证码默认模式，提交任务数量:" + DEFAULT_NO_CAPTCHA_SUBMIT_COUNT
                        + "，间隔:" + DEFAULT_NO_CAPTCHA_INTERVAL_MS + "ms，提前:" + DEFAULT_NO_CAPTCHA_LEAD_TIME_MS + "ms。");
                break;
            } else if ("2".equals(choice)) {
                RuntimeConfig.setModeType(ModeType.NO_CAPTCHA_CUSTOM);
                configureQueueParameters(reader,
                        NoCaptchaCustomConfig.SUBMIT_COUNT,
                        NoCaptchaCustomConfig.INTERVAL_MS,
                        NoCaptchaCustomConfig.LEAD_TIME_MS,
                        true);
                break;
            } else {
                System.out.println("输入无效，请重新输入。");
            }
        }
    }

    private static void configureQueueParameters(BufferedReader reader,
                                                 long defaultSubmitCount,
                                                 long defaultInterval,
                                                 long defaultLead,
                                                 boolean allowBlank) throws IOException {
        long submitCount = promptLong(reader,
                "请输入提交任务次数(默认 " + defaultSubmitCount + "):",
                defaultSubmitCount, allowBlank);
        long interval = promptLong(reader,
                "请输入任务间隔(毫秒, 默认 " + defaultInterval + "):",
                defaultInterval, allowBlank);
        long lead = promptLong(reader,
                "请输入提前发送时间(毫秒, 默认 " + defaultLead + "):",
                defaultLead, allowBlank);
        RuntimeConfig.updateQueueSettings(submitCount, interval, lead);
    }

    private static long promptLong(BufferedReader reader, String prompt, long defaultValue, boolean allowBlank) throws IOException {
        while (true) {
            System.out.print(prompt);
            String line = reader.readLine();
            if (StrUtil.isBlank(line)) {
                if (allowBlank) {
                    System.out.println("未输入内容，使用默认值:" + defaultValue);
                    return defaultValue;
                }
                System.out.println("输入不能为空，请重试。");
                continue;
            }
            try {
                return Long.parseLong(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字。");
            }
        }
    }

    private static void verifyLicense(BufferedReader reader) throws IOException {
        if (RuntimeConfig.isCaptchaEnabled()) {
            verifyCaptchaMode(reader);
        } else {
            verifyNoCaptchaMode(reader);
        }
    }

    private static void verifyNoCaptchaMode(BufferedReader reader) throws IOException {
        int retry = 0;
        while (true) {
            System.out.println("\n请输入激活码:");
            String generatedKey = reader.readLine();
            String verifyUrl = BASE_URL + "/verify";
            HttpResponse verifyResponse = HttpRequest.post(verifyUrl)
                    .form("key", generatedKey)
                    .form("account", account)
                    .execute();
            JSONObject entries = JSONUtil.parseObj(verifyResponse.body());
            String code = entries.getStr("code");
            if (!"200".equals(code)) {
                System.out.println("激活码错误，请获取正确激活码------>" + generatedKey + "---[日志问题]--->" + entries.getStr("msg"));
                if (retry++ > 3) {
                    System.out.println("激活码多次输入错误，退出程序");
                    System.exit(0);
                }
            } else {
                System.out.println("激活码正确，开始抢活动------>" + generatedKey);
                break;
            }
        }
    }

    private static void verifyCaptchaMode(BufferedReader reader) throws IOException {
        int retry = 0;
        while (true) {
            System.out.println("\n请输入激活码:");
            String generatedKey = reader.readLine();
            String verifyUrl = BASE_URL + "/verify";
            HttpResponse verifyResponse = HttpRequest.post(verifyUrl)
                    .form("key", generatedKey)
                    .form("account", account)
                    .execute();
            JSONObject entries = JSONUtil.parseObj(verifyResponse.body());
            String code = entries.getStr("code");
            if (!"200".equals(code)) {
                System.out.println("激活码错误，请获取正确激活码------>" + generatedKey);
                if (retry++ > 3) {
                    System.out.println("激活码多次输入错误，退出程序");
                    System.exit(0);
                }
            } else {
                System.out.println("激活码正确，开始抢活动------>" + generatedKey);
                break;
            }
        }
    }
}
