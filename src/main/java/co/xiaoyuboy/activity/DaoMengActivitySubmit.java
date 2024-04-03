package co.xiaoyuboy.activity;

import cn.hutool.http.HttpUtil;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.DetailMsg;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.util.DaoMengSmile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.xiaoyuboy.daomenjava.i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smile
 * @date 2023-11-06 22:10
 */
public class DaoMengActivitySubmit {
    public DaoMengSmile daoMengSmile = new DaoMengSmile();
    public String activtySubmit(String activityId, User user) {
         return HttpUtil.createPost("https://appdmkj.5idream.net/v2/signup/submit")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(this.daoMengSmile.getSignature(getData(activityId, user)))
                .execute().body();
    }

    public  String getData(String activityId, User user) {
        Map<String, String> map = new HashMap<>();
        map.put("token", user.getToken());
        map.put("version", App.version);
        map.put("uid", user.getUid());
        map.put("remark", "");
        //填写必填项
        DetailMsg detailMsg = new DetailMsg();
        //使用用户登录账号进行填充
        detailMsg.setConent(co.xiaoyuboy.App.account);
        map.put("data", this.getToJson(Arrays.asList(detailMsg)));
        map.put("activityId", activityId);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("signToken", i.d(map));
        return this.daoMengSmile.getMapToString(map);
    }
    public  String getToJson(Object o){
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json转换异常"+o);
        }
        return json;
    }
}
