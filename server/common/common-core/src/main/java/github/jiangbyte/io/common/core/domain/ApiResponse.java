package github.jiangbyte.io.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一 API 响应包装：业务码、提示文案与可选载荷。
 * 供 Controller / 全局异常处理统一出口，避免各模块自定义返回结构。
 *
 * Author: Charlie
 */
@Schema(description = "统一 API 响应包装：业务码、提示文案与可选载荷。")
@Data
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "编码")

    private int code;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "响应数据")
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 构造成功响应（无载荷）。 */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    /** 构造成功响应并携带数据。 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /** 构造失败响应（指定业务码与提示）。 */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

}
