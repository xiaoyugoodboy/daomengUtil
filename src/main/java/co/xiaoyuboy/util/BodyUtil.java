package co.xiaoyuboy.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import co.xiaoyuboy.captcha.LocalCaptchaService;
import co.xiaoyuboy.config.RuntimeConfig;
import co.xiaoyuboy.daomenjava.i;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.DetailMsg;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 构建请求体.
 */
@Slf4j
public class BodyUtil {
    private static final String CAPTCHA_URL = "https://appdmkj.5idream.net/signup/captcha";
    public static volatile LocalCaptchaService localCaptchaService;

    public DaoMengSmile daoMengSmile = new DaoMengSmile();


    /**
     * 获取报名签名.
     */
    public Job getSignatureData(String activityId, User user, long timestamp) {
        Map<String, String> map = new HashMap<>();
        map.put("token", user.getToken());
        map.put("version", App.version);
        if (RuntimeConfig.isCaptchaEnabled()) {
            map.put("captchaValue", captchaValue(activityId, user));
        }
        map.put("uid", user.getUid());
        map.put("remark", "");
        DetailMsg detailMsg = new DetailMsg();
        detailMsg.setConent(co.xiaoyuboy.App.account);
        map.put("data", this.getToJson(Arrays.asList(detailMsg)));
        map.put("activityId", activityId);
        map.put("timestamp", String.valueOf(timestamp));
        map.put("signToken", i.d(map));
        String mapToString = this.daoMengSmile.getMapToString(map);
        String signatureData = this.daoMengSmile.getSignature(mapToString);
        Job job = new Job();
        job.setBody(signatureData);
        job.setJsonBody(mapToString);
        return job;
    }

    /**
     * 获取验证码签名.
     */
    public Job getSMSSignatureData(String activityId, User user) {
        Map<String, String> map = new HashMap<>();
        map.put("activityId", activityId);
        map.put("token", user.getToken());
        map.put("uid", user.getUid());
        map.put("signToken", i.d(map));
        String mapToString = this.daoMengSmile.getMapToString(map);
        String signatureData = this.daoMengSmile.getSignature(mapToString);
        Job job = new Job();
        job.setBody(signatureData);
        job.setJsonBody(mapToString);
        return job;
    }

    private String getToJson(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json转换异常" + o);
        }
    }

    /**
     * 使用本地识别服务获取验证码值.
     */
    public String captchaValue(String activityId, User user) {
        RuntimeConfig.CaptchaSettings settings = RuntimeConfig.getCaptchaSettings();
        Job smsSignatureData = this.getSMSSignatureData(activityId, user);
        Integer code = null;
        int attempt = 0;
        while (attempt++ < settings.getMaxAttempt()) {
            byte[] imageBytes = fetchCaptchaImage(CAPTCHA_URL, App.loginHead, smsSignatureData.getBody());
            if (imageBytes == null) {
                continue;
            }
            try {
//                long l = System.currentTimeMillis();
                JSONObject jsonObject = getLocalCaptchaService().recognizeWithJsonResponse(imageBytes);
                if (jsonObject.getBool("success", false)) {
                    String resultValue = jsonObject.getStr("calculationResult");
                    code = Integer.valueOf(resultValue);
//                    log.info("验证码识别成功，耗时：" + (System.currentTimeMillis() - l) + "ms");
                }
            } catch (Exception ignored) {
            }
            if (code != null && code < 100) {
                break;
            }
        }
        if (code == null) {
            throw new RuntimeException("验证码识别失败，请检查识别服务是否正常");
        }
        return code.toString();
    }

    public static byte[] fetchCaptchaImage(String url, String standardUA, String requestBody) {
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("standardUA", standardUA)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(requestBody)
                    .execute();
            if (response.isOk()) {
                return response.bodyBytes();
            } else {
                System.err.println("HTTP 请求失败，状态码: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LocalCaptchaService getLocalCaptchaService() {
        if (localCaptchaService == null) {
            synchronized (BodyUtil.class) {
                if (localCaptchaService == null) {
                    LocalCaptchaService service = LocalCaptchaService.getInstance();
                    if (service == null) {
                        service = new LocalCaptchaService();
                        service.init();
                    }
                    localCaptchaService = service;
                }
            }
        }
        return localCaptchaService;
    }
}
