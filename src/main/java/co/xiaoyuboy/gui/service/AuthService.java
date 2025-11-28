package co.xiaoyuboy.gui.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.util.DaoMengSmile;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录认证服务
 */
@Slf4j
public class AuthService {

    private static final String LOGIN_URL = "https://appdmkj.5idream.net/v2/login/phone";
    private final DaoMengSmile daoMengSmile = new DaoMengSmile();
    private final JsonParsing jsonParsing = new JsonParsing();

    /**
     * 用户登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录结果
     */
    public ApiResult<User> login(String account, String password) {
        try {
            log.info("开始登录: {}", account);

            // 获取加密数据
            String data = daoMengSmile.getLoginData(account, password);
            String signature = daoMengSmile.getSignature(data);
            String standardUA = daoMengSmile.getHead();

            // 发送登录请求
            HttpResponse response = HttpRequest.post(LOGIN_URL)
                    .header("standardUA", standardUA)
                    .body(signature)
                    .timeout(10000)
                    .execute();

            if (!response.isOk()) {
                return ApiResult.fail("网络请求失败: HTTP " + response.getStatus());
            }

            String body = response.body();
            log.info("登录响应: {}", body);

            // 解析响应
            User user = jsonParsing.loginJsonParsing(body);

            if (user != null && user.getUid() != null) {
                log.info("登录成功: {}", account);
                return ApiResult.success(user);
            } else {
                return ApiResult.fail("登录失败，请检查账号密码");
            }

        } catch (Exception e) {
            log.error("登录异常", e);
            return ApiResult.fail("登录异常: " + e.getMessage());
        }
    }
}
