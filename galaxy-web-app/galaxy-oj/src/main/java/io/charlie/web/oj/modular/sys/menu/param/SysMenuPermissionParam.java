package io.charlie.web.oj.modular.sys.menu.param;

import lombok.Data;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 25/09/2025
 * @description 菜单权限
 */
@Data
public class SysMenuPermissionParam {
    private String id;
    private List<String> permissions;
}
