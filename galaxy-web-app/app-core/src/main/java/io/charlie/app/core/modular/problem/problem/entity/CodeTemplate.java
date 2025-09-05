package io.charlie.app.core.modular.problem.problem.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 21/06/2025
 * @description 代码模板
 */
@Data
public class CodeTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String language;

    private String prefix;

    private String template;

    private String suffix;
}
