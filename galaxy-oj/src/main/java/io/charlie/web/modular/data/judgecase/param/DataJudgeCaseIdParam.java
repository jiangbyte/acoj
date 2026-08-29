package io.charlie.web.modular.data.judgecase.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 判题结果用例
*/
@Data
@Schema(name = "DataJudgeCase", description = "判题结果用例 ID参数")
public class DataJudgeCaseIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}