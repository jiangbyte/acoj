package io.charlie.web.oj.config;

import lombok.Data;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 25/09/2025
 * @description TODO
 */
@Data
public class PermissionGroupData {
    private String controller;        // 控制器类名
    private String controllerName;    // @Tag的name
    private String baseUrl;           // 基础路径，来自@RequestMapping

    private List<PermissionData> permissions;

    @Data
    public static class PermissionData {
        private String method;        // HTTP方法（GET、POST等）
        private String methodName;    // @Operation的summary
        private String permission;    // @SaCheckPermission的值
        private String url;           // 完整URL路径
        private String description;   // 方法描述（可选）
    }
}
