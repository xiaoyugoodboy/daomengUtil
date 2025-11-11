package co.xiaoyuboy.gui.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API统一返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, "操作成功", data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(false, message, null);
    }
}
