package co.xiaoyuboy.util;

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
     * @param activityId
     * @param user
     * @param timestamp
     * @return
     */
    public Job getSignatureData(String activityId, User user, long timestamp) {
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
        //采用提交构建好的时间
        map.put("timestamp", String.valueOf(timestamp));
        map.put("signToken", i.d(map));
        String mapToString = this.daoMengSmile.getMapToString(map);
        String signatureData= this.daoMengSmile.getSignature(mapToString);
        Job job = new Job();
        job.setBody(signatureData);
        job.setJsonBody(mapToString);
        return job;
    }
    private   String getToJson(Object o){
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
