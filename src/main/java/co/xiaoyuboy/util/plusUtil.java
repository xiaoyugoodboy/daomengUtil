package co.xiaoyuboy.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

/**
 * @Author: Smile
 * @Date: 2023-11-11 22:44
 * @Description:
 */
public class plusUtil {
    public static void sendPlusUtil(String title,String content){
        String url = "http://www.pushplus.plus/send";
        String token = "570b2383a41148638debb12e6e098503";
        //String title = "账号绑定通知";
        String template = "html";
        String to = "fef7047d7019474ea87bea9b7003e032";
        String fullUrl = url + "?token=" + token + "&title=" + title + "&content=" + content + "&template=" + template + "&to=" + to;
        String response = HttpUtil.get(fullUrl);
        JSONObject json = new JSONObject(response);
        String code = json.get("code").toString();
        if (!"200".equals(code)){
            throw  new RuntimeException("sendPlus解析失败,程序失效");
        }
        System.out.println("Response: " + response);
    }

    public static void main(String[] args) {
        sendPlusUtil("测试消息","java测试消息11111");
    }
}
