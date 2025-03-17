package co.xiaoyuboy.util;

import cn.hutool.core.io.resource.BytesResource;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.DetailMsg;
import co.xiaoyuboy.entity.Job;
import co.xiaoyuboy.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.xiaoyuboy.daomenjava.i;

/**
 * @author xiaoyu
 * @date 2024-03-21 15:58
 * 构建请求体
 */
public class BodyUtil {
    public DaoMengSmile daoMengSmile = new DaoMengSmile();

    /**
     * 获取到签名数据
     *
     * @param activityId
     * @param user
     * @param timestamp
     * @return
     */
    public Job getSignatureData(String activityId, User user, long timestamp) {
        Map<String, String> map = new HashMap<>();
        map.put("token", user.getToken());
        map.put("version", App.version);
        map.put("captchaValue", captchaValue(activityId,user));

        map.put("uid", user.getUid());
        map.put("remark", "");
        //填写必填项
        DetailMsg detailMsg = new DetailMsg();
        //使用用户登录账号进行填充
        detailMsg.setConent(co.xiaoyuboy.App.account);
        map.put("data", this.getToJson(Arrays.asList(detailMsg)));
        map.put("activityId", activityId);
        //采用提交构建好的时间
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
     * 获取到验证码的接口
     *
     * @param activityId
     * @param user
     * @return
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
        String json = "";
        try {
            json = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json转换异常" + o);
        }
        return json;
    }

    public String captchaValue(String activityId, User user) {
        Job smsSignatureData = this.getSMSSignatureData(activityId, user);
        Integer code = null;
        int cont = 0;
        while (true) {
            byte[] imageBytes = fetchCaptchaImageHutool("https://appdmkj.5idream.net/signup/captcha",
                    co.xiaoyuboy.entity.App.loginHead, smsSignatureData.getBody());
            if (imageBytes != null) {
                HttpResponse predictResponse = uploadImageHutool("http://127.0.0.1:5000/predict", imageBytes); // 上传图片并获取响应
                if (predictResponse != null) {
                    try {
                        // 将 JSON 字符串解析成 JSONObject 对象
                        JSONObject jsonObject = JSONUtil.parseObj(predictResponse.body());
                        // 从 JSONObject 中获取 "result" 对应的字符串值
                        String resultValue = jsonObject.getStr("result");
                        code = Integer.valueOf(resultValue);
                    } catch (Exception e) {
                        //不处理
                    }
                }
            }
            if ((code != null&&code<100)||cont>15){
                break;
            }
        }
        return code.toString();
    }

    public static byte[] fetchCaptchaImageHutool(String url, String standardUA, String requestBody) {
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("standardUA", standardUA)
                    .header("Content-Type", "application/x-www-form-urlencoded") // Hutool 默认会自动设置 Content-Type 为 application/x-www-form-urlencoded，但显式声明更清晰
                    .body(requestBody)
                    .execute();

            if (response.isOk()) { // 使用 isOk() 判断状态码是否为 2xx
                return response.bodyBytes(); // 使用 bodyBytes() 直接获取字节数组
            } else {
                System.err.println("HTTP 请求失败，状态码: " + response.getStatus()); // 使用 getStatus() 获取状态码
                return null;
            }
        } catch (Exception e) { // Hutool 的 HTTP 操作可能会抛出 Exception，需要捕获
            e.printStackTrace();
            return null;
        }
    }

    public static HttpResponse uploadImageHutool(String predictApiUrl, byte[] imageBytes) {
        try {
            Map<String, Object> formMap = new HashMap<>();
            // 使用 BytesResource 包装 byte[]
            formMap.put("file", new BytesResource(imageBytes, "captcha.jpg")); //  BytesResource 构造函数可以传入 byte[] 和 文件名 (可选)

            HttpRequest request = HttpRequest.post(predictApiUrl)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("User-Agent", "ApipostRuntime/1.1.0")
                    .header("Connection", "keep-alive")
                    .form(formMap); // 使用 form(Map) 方法设置 multipart/form-data 请求体

            return request.execute();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
