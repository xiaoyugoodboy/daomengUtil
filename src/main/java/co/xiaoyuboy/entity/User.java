package co.xiaoyuboy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Smile
 * @date 2023-11-06 16:28
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String token;
    //名字
    private String name;
    //账号名
    private String nickname;
    //学院id
    private String collegeId;
    //学院名称
    private String collegeName;
    //学校
    private String myIdentityInfo;

}
