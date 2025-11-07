package io.charlie.web.oj.modular.sys.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 系统配置 分页参数
*/
@Data
@Schema(name = "SysConfig", description = "系统配置 分页参数")
public class SysConfigListParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词")
    private String keyword;
}