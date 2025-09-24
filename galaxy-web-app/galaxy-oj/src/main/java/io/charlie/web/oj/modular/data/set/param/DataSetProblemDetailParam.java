package io.charlie.web.oj.modular.data.set.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/09/2025
 * @description TODO
 */
@Data
public class DataSetProblemDetailParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "problemId不能为空")
    private String problemId;

}
