package co.xiaoyuboy.activity;

import cn.hutool.http.HttpUtil;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.util.DaoMengSmile;


import java.util.HashMap;
import java.util.Map;
import co.xiaoyuboy.daomenjava.i;

/**
 * @author Smile
 * @date 2023-11-06 16:27
 */
public class DaoMengActivity {
    private static DaoMengSmile smile=new DaoMengSmile();



    public static String getActivityList(User user){
        return HttpUtil.createPost("https://appdmkj.5idream.net/v2/activity/activities")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(smile.getSignature(getData(user.getUid(),user.getToken())))
                .execute().body();
    }
    public static   String getData(String uid,String token){
        Map<String,String> map=new HashMap<>();
        map.put("token",token);
        map.put("version",App.version);
        map.put("uid",uid);
        map.put("page","1");
        map.put("signToken", i.d(map));
        return smile.getMapToString(map);
    }
}
