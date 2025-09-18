package io.charlie.app.core.config;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionScanner {
    private final ApplicationContext applicationContext;

    /**
     * 扫描所有Controller，获取@SaCheckPermission注解的值
     * @return 权限字符串列表
     */
    public List<String> scanSaCheckPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // 获取所有Controller bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);
        
        // 合并所有Controller
        controllers.putAll(restControllers);
        
        // 遍历所有Controller
        for (Object controller : controllers.values()) {
            Class<?> controllerClass = controller.getClass();
            
            // 如果是CGLIB代理类，获取原始类
            if (controllerClass.getName().contains("$$")) {
                controllerClass = controllerClass.getSuperclass();
            }
            
            // 扫描类上的注解
            scanClassAnnotations(controllerClass, permissions);
            
            // 扫描方法上的注解
            scanMethodAnnotations(controllerClass, permissions);
        }
        
        return permissions;
    }

    /**
     * 扫描类级别的@SaCheckPermission注解
     */
    private void scanClassAnnotations(Class<?> clazz, List<String> permissions) {
        SaCheckPermission classAnnotation = AnnotationUtils.findAnnotation(clazz, SaCheckPermission.class);
        if (classAnnotation != null) {
            if (classAnnotation.value().length > 0) {
                for (String permission : classAnnotation.value()) {
                    permissions.add(permission);
                }
            }
        }
    }

    /**
     * 扫描方法级别的@SaCheckPermission注解
     */
    private void scanMethodAnnotations(Class<?> clazz, List<String> permissions) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            SaCheckPermission methodAnnotation = AnnotationUtils.findAnnotation(method, SaCheckPermission.class);
            if (methodAnnotation != null) {
                if (methodAnnotation.value().length > 0) {
                    for (String permission : methodAnnotation.value()) {
                        permissions.add(permission);
                    }
                }
            }
        }
    }

    /**
     * 获取去重后的权限列表
     */
    public Set<String> getUniquePermissions() {
        List<String> allPermissions = scanSaCheckPermissions();
        return new java.util.HashSet<>(allPermissions);
    }

    /**
     * 按Controller分组获取权限
     */
    public Map<String, List<String>> getGroupedPermissions() {
        Map<String, List<String>> groupedPermissions = new java.util.HashMap<>();
        
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);
        controllers.putAll(restControllers);
        
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            Object controller = entry.getValue();
            Class<?> controllerClass = controller.getClass();
            
            if (controllerClass.getName().contains("$$")) {
                controllerClass = controllerClass.getSuperclass();
            }
            
            List<String> controllerPermissions = new ArrayList<>();
            
            // 扫描类注解
            SaCheckPermission classAnnotation = AnnotationUtils.findAnnotation(controllerClass, SaCheckPermission.class);
            if (classAnnotation != null && classAnnotation.value().length > 0) {
                for (String permission : classAnnotation.value()) {
                    controllerPermissions.add(permission);
                }
            }
            
            // 扫描方法注解
            for (Method method : controllerClass.getDeclaredMethods()) {
                SaCheckPermission methodAnnotation = AnnotationUtils.findAnnotation(method, SaCheckPermission.class);
                if (methodAnnotation != null && methodAnnotation.value().length > 0) {
                    for (String permission : methodAnnotation.value()) {
                        controllerPermissions.add(permission);
                    }
                }
            }
            
            if (!controllerPermissions.isEmpty()) {
                groupedPermissions.put(controllerClass.getSimpleName(), controllerPermissions);
            }
        }
        
        return groupedPermissions;
    }
}