package co.xiaoyuboy.parsing;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.xiaoyuboy.entity.Activity;
import co.xiaoyuboy.entity.User;


import java.util.List;

/**
 * @author Smile
 * @date 2023-11-06 16:35
 */
public class JsonParsing {
    public User loginJsonParsing(String loginJson){
        JSONObject json = new JSONObject(loginJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
           throw  new RuntimeException("登录json解析失败，登录失败");
        }
        User user = JSONUtil.toBean(json.get("data").toString(), User.class);
        return user;
    }
    public List<Activity>  ActivityListJsonParsing(String activityJson){
        JSONObject json = new JSONObject(activityJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
            System.out.println("活动列表获取失败");
            return null;
        }
        /**
         * 这里过滤报名中的活动和规划中的活动
         */
        List<Activity> activityList = JSONUtil.toList(json.getJSONObject("data").get("list").toString(), Activity.class)
                .stream()
                .filter((iten -> "3".equals(iten.getStatus())||"2".equals(iten.getStatus())))
                .toList();
        return activityList;
    }
    /**
     * 获取到活动的时间
     * @param activityDetailJson
     * @return
     */
    public Long getActivityDetailTime(String activityDetailJson){
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
            throw new RuntimeException("活动详情获取失败");
        }
        String joinstartdate = json.getJSONObject("data").get("joinstartdate").toString();
        return Long.parseLong(joinstartdate);
    }
    public String getActivityJoinId(String activityDetailJson){
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
            throw new RuntimeException("活动详情获取失败");
        }
        return json.getJSONObject("data").get("signUpId").toString();

    }
    public boolean isActivitySuccess(String activityDetailJson){
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
           return false;
        }else {
            String enrollResult = json.getJSONObject("data").get("enrollResult").toString();
            if ("2".equals(enrollResult)){
                return true;
            }else {
                return false;
            }

        }
    };
    /**
     * 判断活动是否能参加
     * @param activityDetailJson
     * @return
     */
    public boolean isActivity(String activityDetailJson){
        JSONObject json = new JSONObject(activityDetailJson);
        String code = json.get("code").toString();
        if (!"100".equals(code)){
            throw new RuntimeException("活动详情获取失败");
        }
        String ableJoinFlag = json.getJSONObject("data").get("ableJoinFlag").toString();
        return "1".equals(ableJoinFlag)?true:false;
    }
}
