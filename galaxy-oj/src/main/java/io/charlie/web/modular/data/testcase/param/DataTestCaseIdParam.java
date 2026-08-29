package io.charlie.web.modular.data.testcase.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 题目测试用例
*/
@Data
@Schema(name = "DataTestCase", description = "题目测试用例 ID参数")
public class DataTestCaseIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}