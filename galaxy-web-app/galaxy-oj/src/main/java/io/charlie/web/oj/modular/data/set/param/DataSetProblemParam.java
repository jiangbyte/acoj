package io.charlie.web.oj.modular.data.set.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集
*/
@Data
@Schema(name = "DataSet", description = "题集 ID参数")
public class DataSetProblemParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}