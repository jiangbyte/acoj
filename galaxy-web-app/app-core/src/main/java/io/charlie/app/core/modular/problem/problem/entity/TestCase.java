package io.charlie.app.core.modular.problem.problem.entity;

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
public class TestCase implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "输入")
    private String input;

    @Schema(description = "输出")
    private String output;

}
