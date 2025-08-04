package io.charlie.app.core.modular.sys.article.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 系统文章
*/
@Data
public class SysArticleOptionParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;

}