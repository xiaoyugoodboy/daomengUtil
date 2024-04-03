package co.xiaoyuboy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Smile
 * @date 2023-11-06 17:38
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Activity {
    private String aid;
    private String activityId;
    private String imageUrl;
    private String name;
    private String status;
    private String statusText;
    private String activitytime;
    private String catalog2name;
    private int boutique;
    private String boutiqueStr;
}
