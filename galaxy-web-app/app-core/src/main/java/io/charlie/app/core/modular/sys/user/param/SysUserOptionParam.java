package io.charlie.app.core.modular.sys.user.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 26/07/2025
 * @description 用户列表参数
 */
@Data
public class SysUserOptionParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
}
