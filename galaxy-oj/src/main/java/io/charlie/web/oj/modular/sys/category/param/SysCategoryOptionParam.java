package io.charlie.web.oj.modular.sys.category.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 分类
*/
@Data
public class SysCategoryOptionParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;

}