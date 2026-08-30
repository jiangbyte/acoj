package github.jiangbyte.io.common.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 数据权限范围类型：全部、仅本人、本部门、本部门及子级、自定义。
 *
 * Author: Charlie
 */
@Schema(description = "数据权限范围类型：全部、仅本人、本部门、本部门及子级、自定义。")
public enum DataScopeType {
    ALL,
    SELF,
    DEPT,
    DEPT_AND_CHILD,
    CUSTOM
}
