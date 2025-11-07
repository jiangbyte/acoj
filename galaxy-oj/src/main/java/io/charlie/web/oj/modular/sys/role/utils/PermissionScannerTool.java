package io.charlie.web.oj.modular.sys.role.utils;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionScannerTool {
    private final ApplicationContext applicationContext;

    /**
     * 获取分组的权限数据
     */
    public List<PermissionGroupData> getGroupedPermissionData() {
        List<PermissionGroupData> result = new ArrayList<>();

        // 获取所有Controller bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(Controller.class);
        controllers.putAll(controllerBeans);

        for (Object controller : controllers.values()) {
            Class<?> controllerClass = getOriginalClass(controller);

            PermissionGroupData groupData = extractControllerPermissionData(controllerClass);
            if (groupData != null && !groupData.getPermissions().isEmpty()) {
                result.add(groupData);
            }
        }

        return result;
    }

    /**
     * 提取控制器的权限数据
     */
    private PermissionGroupData extractControllerPermissionData(Class<?> controllerClass) {
        PermissionGroupData groupData = new PermissionGroupData();

        // 设置控制器信息
        groupData.setController(controllerClass.getSimpleName());

        // 获取@Tag注解（控制器名称）
        Tag tag = AnnotationUtils.findAnnotation(controllerClass, Tag.class);
        if (tag != null && tag.name() != null && !tag.name().isEmpty()) {
            groupData.setControllerName(tag.name());
        } else {
            groupData.setControllerName(controllerClass.getSimpleName());
        }

        // 获取基础URL路径
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerClass, RequestMapping.class);
        String baseUrl = "";
        if (requestMapping != null && requestMapping.value().length > 0) {
            baseUrl = requestMapping.value()[0];
        }
        groupData.setBaseUrl(baseUrl);

        // 提取方法级别的权限信息
        List<PermissionGroupData.PermissionData> permissionDataList = extractMethodPermissions(controllerClass, baseUrl);
        groupData.setPermissions(permissionDataList);

        return groupData;
    }

    /**
     * 提取方法级别的权限信息
     */
    private List<PermissionGroupData.PermissionData> extractMethodPermissions(Class<?> controllerClass, String baseUrl) {
        List<PermissionGroupData.PermissionData> permissionDataList = new ArrayList<>();

        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            PermissionGroupData.PermissionData permissionData = extractMethodPermissionData(method, baseUrl);
            if (permissionData != null) {
                permissionDataList.add(permissionData);
            }
        }

        return permissionDataList;
    }

    /**
     * 提取单个方法的权限数据
     */
    private PermissionGroupData.PermissionData extractMethodPermissionData(Method method, String baseUrl) {
        // 检查是否有权限注解
        SaCheckPermission saCheckPermission = AnnotationUtils.findAnnotation(method, SaCheckPermission.class);
        if (saCheckPermission == null || saCheckPermission.value().length == 0) {
            return null;
        }

        PermissionGroupData.PermissionData permissionData = new PermissionGroupData.PermissionData();

        // 设置权限信息（取第一个权限值）
        permissionData.setPermission(saCheckPermission.value()[0]);

        // 获取HTTP方法
        permissionData.setMethod(extractHttpMethod(method));

        // 获取URL路径
        String methodUrl = extractMethodUrl(method);
        String fullUrl = combineUrls(baseUrl, methodUrl);
        permissionData.setUrl(fullUrl);

        // 获取方法名称（@Operation的summary）
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null && operation.summary() != null && !operation.summary().isEmpty()) {
            permissionData.setMethodName(operation.summary());
        } else {
            permissionData.setMethodName(method.getName());
        }

        // 设置描述（可以使用description，如果没有就使用summary）
        if (operation != null && operation.description() != null && !operation.description().isEmpty()) {
            permissionData.setDescription(operation.description());
        } else if (operation != null && operation.summary() != null && !operation.summary().isEmpty()) {
            permissionData.setDescription(operation.summary());
        } else {
            permissionData.setDescription(method.getName());
        }

        return permissionData;
    }

    /**
     * 提取HTTP方法
     */
    private String extractHttpMethod(Method method) {
        // 检查各种Mapping注解
        GetMapping getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
        if (getMapping != null) return "GET";

        PostMapping postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
        if (postMapping != null) return "POST";

        PutMapping putMapping = AnnotationUtils.findAnnotation(method, PutMapping.class);
        if (putMapping != null) return "PUT";

        DeleteMapping deleteMapping = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
        if (deleteMapping != null) return "DELETE";

        PatchMapping patchMapping = AnnotationUtils.findAnnotation(method, PatchMapping.class);
        if (patchMapping != null) return "PATCH";

        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (requestMapping != null && requestMapping.method().length > 0) {
            return requestMapping.method()[0].name();
        }

        return "UNKNOWN";
    }

    /**
     * 提取方法URL路径
     */
    private String extractMethodUrl(Method method) {
        // 检查各种Mapping注解的路径
        String[] urls = extractUrlFromMapping(method);
        if (urls.length > 0) {
            return urls[0];
        }
        return "";
    }

    /**
     * 从各种Mapping注解中提取URL
     */
    private String[] extractUrlFromMapping(Method method) {
        GetMapping getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
        if (getMapping != null) return getMapping.value();

        PostMapping postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
        if (postMapping != null) return postMapping.value();

        PutMapping putMapping = AnnotationUtils.findAnnotation(method, PutMapping.class);
        if (putMapping != null) return putMapping.value();

        DeleteMapping deleteMapping = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
        if (deleteMapping != null) return deleteMapping.value();

        PatchMapping patchMapping = AnnotationUtils.findAnnotation(method, PatchMapping.class);
        if (patchMapping != null) return patchMapping.value();

        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (requestMapping != null) return requestMapping.value();

        return new String[0];
    }

    /**
     * 合并基础URL和方法URL
     */
    private String combineUrls(String baseUrl, String methodUrl) {
        if (baseUrl.isEmpty()) return methodUrl;
        if (methodUrl.isEmpty()) return baseUrl;

        // 确保没有双斜杠
        if (baseUrl.endsWith("/") && methodUrl.startsWith("/")) {
            return baseUrl + methodUrl.substring(1);
        } else if (!baseUrl.endsWith("/") && !methodUrl.startsWith("/")) {
            return baseUrl + "/" + methodUrl;
        } else {
            return baseUrl + methodUrl;
        }
    }

    /**
     * 获取原始类（处理代理情况）
     */
    private Class<?> getOriginalClass(Object bean) {
        Class<?> clazz = bean.getClass();
        if (clazz.getName().contains("$$")) {
            return clazz.getSuperclass();
        }
        return clazz;
    }

    /**
     * 获取所有唯一的权限字符串（兼容旧方法）
     */
    public Set<String> getUniquePermissions() {
        List<PermissionGroupData> groupedData = getGroupedPermissionData();
        return groupedData.stream()
                .flatMap(group -> group.getPermissions().stream())
                .map(PermissionGroupData.PermissionData::getPermission)
                .collect(Collectors.toSet());
    }
}