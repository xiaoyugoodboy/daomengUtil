package co.xiaoyuboy.gui.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 网络授权验证服务（简化版）
 */
@Slf4j
public class NetworkLicenseService {

    private static final String VERIFY_URL = "http://38.207.176.57:5666/api/key/verify";
    private static final int TIMEOUT = 10000;

    // 开发模式：设置为true可以跳过激活码验证（仅用于测试）
    private static final boolean DEV_MODE = true;

    /**
     * 验证激活码
     *
     * @param activationCode 激活码
     * @param account        账号
     * @return 是否验证成功
     */
    public boolean verify(String activationCode, String account) {
        // 开发模式：跳过验证
        if (DEV_MODE) {
            log.warn("⚠️ 开发模式已启用，跳过激活码验证");
            return true;
        }

        try {
            log.info("开始验证激活码: {}", activationCode);

            JSONObject params = new JSONObject();
            params.set("key", activationCode);
            params.set("account", account);

            HttpResponse response = HttpRequest.post(VERIFY_URL)
                    .body(params.toString())
                    .contentType("application/json")
                    .timeout(TIMEOUT)
                    .execute();

            if (!response.isOk()) {
                log.error("激活码验证失败，HTTP状态码: {}", response.getStatus());
                return false;
            }

            String body = response.body();
            log.info("激活码验证响应: {}", body);

            JSONObject result = JSONUtil.parseObj(body);
            boolean success = result.getBool("success", false);

            if (success) {
                log.info("激活码验证成功");
            } else {
                log.warn("激活码验证失败: {}", result.getStr("message"));
            }

            return success;

        } catch (Exception e) {
            log.error("激活码验证异常", e);
            return false;
        }
    }
}
