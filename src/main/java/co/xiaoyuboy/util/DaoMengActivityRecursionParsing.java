package co.xiaoyuboy.util;

import cn.hutool.http.HttpUtil;
import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.App;
import co.xiaoyuboy.entity.User;
import co.xiaoyuboy.parsing.JsonParsing;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.xiaoyuboy.daomenjava.i;

/**
 * @Author: Smile
 * @Date: 2023-11-11 18:40
 * @Description: 到梦空间活动活动解析算法
 */
public class DaoMengActivityRecursionParsing {
    private static DaoMengSmile smile=new DaoMengSmile();
    public static List<Activity>  getActivityList(User user){
        List<Activity> activityList=new ArrayList<>();
        for (int i=2;i<=4;i++){//获取规划中,报名中和等待中的所有活动
            //页码
           int index=1;
            while (true){
                List<Activity> activity = getActivity(user, smile.getSignature(getData(user.getUid(), user.getToken(), i + "",index+"")));
                if (null==activity||activity.size()==0){//活动列表为空或者等于0,直接结束循环
                    break;
                }else {
                   //这里合并活动的列表
                    activityList.addAll(activity);
                }
                //页码
                index++;

            }
        }
        return activityList;
    }
    public static  List<Activity> getActivity(User user,String data){
        String json = HttpUtil.createPost("https://appdmkj.5idream.net/v2/activity/activities")
                .contentType("application/x-www-form-urlencoded")
                .header("standardUA", App.loginHead)
                .body(data)
                .execute().body();
        JsonParsing jsonParsing = new JsonParsing();
        //解析活动
        List<Activity> activities = jsonParsing.ActivityListJsonParsing(json);
        return activities;
    }

    public static String getData(String uid,String token,String status,String page){
        Map<String,String> map=new HashMap<>();
        map.put("token",token);
        map.put("version", App.version);
        map.put("uid",uid);
        map.put("page",page);
        map.put("status",status);
        map.put("signToken", i.d(map));
        return smile.getMapToString(map);
    }
}
