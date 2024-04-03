package co.xiaoyuboy.activity;

import cn.hutool.http.HttpUtil;
import co.xiaoyuboy.entity.ActivityDetail;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;
import co.xiaoyuboy.util.DaoMengSmile;
import co.xiaoyuboy.daomenjava.i;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Smile
 * @date 2023-11-06 22:23
 * 获取活动详情(已更新加密4.6.0)
 */
public class DaoMengDetail {
    private static DaoMengSmile smile = new DaoMengSmile();

    public static String getActivityDetail(String activityId, User user){
       return HttpUtil.createPost("https://appdmkj.5idream.net/v2/activity/detail")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(smile.getSignature(getData(user.getUid(),user.getToken(),activityId)))
                .execute().body();

    }
    public static   String getData(String uid,String token,String activityId){
        Map<String,String> map=new HashMap<>();
        map.put("token",token);
        map.put("version",App.version);
        map.put("uid",uid);
        map.put("activityId",activityId);
        map.put("signToken", i.d(map));
        return smile.getMapToString(map);
    }



    public static boolean isActivitySuccess(ActivityDetail activityDetail, User user){
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/signup/detail")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(smile.getSignature(getActivitySuccessData(user.getUid(), user.getToken(), activityDetail)))
                .execute().body();
        JsonParsing jsonParsing = new JsonParsing();
        return jsonParsing.isActivitySuccess(json);

    }
    public static String getActivitySuccessData(String uid,String token,ActivityDetail activityDetail){
        Map<String,String> map=new HashMap<>();
        map.put("token",token);
        map.put("version",App.version);
        map.put("uid",uid);
        map.put("joinId",activityDetail.getJoinId());
        map.put("signToken", i.d(map));
        return smile.getMapToString(map);
    }
}
