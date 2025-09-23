package io.charlie.web.oj.modular.sys.dict.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 系统字典
*/
@Data
public class SysDictTreeParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;

}