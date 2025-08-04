package io.charlie.app.core.modular.set.param.solved;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 用户题集解决记录表
*/
@Data
@Schema(name = "ProSetSolved", description = "用户题集解决记录表")
public class ProSetSolvedIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}