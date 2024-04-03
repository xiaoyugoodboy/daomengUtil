package co.xiaoyuboy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Smile
 * @date 2023-11-06 22:50
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActivityDetail {
    //活动的id
    private String activityId;
    //活动的报名时间
    private Long activityCreateTime;
    //活动的名字
    private String name;
    //活动的状态
    private String statusText;
    //查询活动是否录取需要的id
    private String joinId;
}
