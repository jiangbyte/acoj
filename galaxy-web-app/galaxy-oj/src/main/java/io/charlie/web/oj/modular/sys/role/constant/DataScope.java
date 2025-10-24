package io.charlie.web.oj.modular.sys.role.constant;

// 数据权限范围枚举
public enum DataScope {
    SELF,           // 仅自己
    SELF_GROUP,     // 本组及下级
    SELF_GROUP_ONLY, // 仅本组
    SPECIFIED_GROUP, // 指定组
    ALL             // 所有数据
}