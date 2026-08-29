package io.charlie.web.modular.sys.log.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录
*/
@Data
@Schema(name = "SysLog", description = "系统活动/日志记录 ID参数")
public class SysLogIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}