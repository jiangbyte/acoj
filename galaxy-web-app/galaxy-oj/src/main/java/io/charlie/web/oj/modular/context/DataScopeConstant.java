package io.charlie.web.oj.modular.context;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/10/2025
 */
public interface DataScopeConstant {
    String ALL = "ALL"; // 全部数据权限
    String SELF_GROUP = "SELF_GROUP"; // 自己及子用户组数据权限
    String SELF_GROUP_ONLY = "SELF_GROUP_ONLY"; // 仅自己用户组数据权限
    String SPECIFIED_GROUP = "SPECIFIED_GROUP"; // 指定用户组数据权限
    String SELF = "SELF"; // 自己数据权限

    /**
     * 数据权限级别映射（数值越小权限越大）
     */
    Map<String, Integer> DATA_SCOPE_LEVEL = Map.of(
            DataScopeConstant.ALL, 1,
            DataScopeConstant.SELF_GROUP, 2,
            DataScopeConstant.SELF_GROUP_ONLY, 3,
            DataScopeConstant.SPECIFIED_GROUP, 4,
            DataScopeConstant.SELF, 5
    );
}
