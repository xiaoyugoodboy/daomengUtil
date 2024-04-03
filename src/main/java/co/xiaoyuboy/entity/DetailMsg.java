package co.xiaoyuboy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Smile
 * @Date: 2024-03-20 19:12
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DetailMsg {
    private String conent; // 注意这里的名称跟JSON字段应当一致，除非使用@JsonProperty注解来映射
    private String fullid;
    private int key;
    private boolean notList;
    private boolean notNull;
    private int system;
    private String title;
}
