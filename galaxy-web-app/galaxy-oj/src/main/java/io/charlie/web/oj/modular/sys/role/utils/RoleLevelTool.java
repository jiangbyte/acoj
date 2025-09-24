package io.charlie.web.oj.modular.sys.role.utils;

import cn.hutool.core.util.ObjectUtil;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/09/2025
 * @description 角色层级工具
 */
public class RoleLevelTool {
    // 获取层级最高的角色
    public static SysRole getHighLevelRole(List<SysRole> roles) {
        SysRole highLevelRole = null;

        Integer level = 0;

        for (SysRole role : roles) {
            if (role.getLevel() > level) {
                highLevelRole = role;
                level = role.getLevel();
            }
        }

        return highLevelRole;
    }
}
