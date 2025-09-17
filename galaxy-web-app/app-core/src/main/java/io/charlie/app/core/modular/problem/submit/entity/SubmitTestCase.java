package io.charlie.app.core.modular.problem.submit.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 测试用例
 */
@Data
public class SubmitTestCase implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "输入")
    private String input;

    @Schema(description = "输出")
    private String output;

    // 提交 附加参数

    @Schema(description = "期望输出")
    private String except;

    @Schema(description = "时间资源")
    private Integer maxTime;

    @Schema(description = "内存资源")
    private Integer maxMemory;

    @Schema(description = "信息")
    private String message;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "退出码")
    private Integer exitCode;
}
