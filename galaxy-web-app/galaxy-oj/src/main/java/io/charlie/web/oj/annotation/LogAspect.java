package io.charlie.web.oj.annotation;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import io.charlie.web.oj.modular.sys.log.entity.SysLog;
import io.charlie.web.oj.modular.sys.log.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private HttpServletRequest request;

    // 定义切点：标注了@Log注解的方法
    @Pointcut("@annotation(io.charlie.web.oj.annotation.Log)")
    public void logPointCut() {
    }

    // 环绕通知
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取Log注解
        Log logAnnotation = method.getAnnotation(Log.class);

        // 创建日志对象
        SysLog sysLog = new SysLog();
        sysLog.setOperationTime(new Date());

        // 设置IP信息
        setIpInfo(sysLog);

        // 设置操作信息
        setOperationInfo(joinPoint, logAnnotation, sysLog);

        // 设置用户信息
        setUserInfo(sysLog);

        // 设置请求参数
        setRequestParams(joinPoint, sysLog);

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            sysLog.setStatus("SUCCESS");
        } catch (Exception e) {
            sysLog.setStatus("FAIL");
            sysLog.setMessage(e.getMessage());
            throw e;
        } finally {
            // 异步保存日志
            saveLog(sysLog);
        }

        return result;
    }

    private void setOperationInfo(ProceedingJoinPoint joinPoint, Log logAnnotation, SysLog sysLog) {
        // 从注解获取操作信息
        sysLog.setOperation(logAnnotation.value());
        sysLog.setCategory(logAnnotation.category());
        sysLog.setModule(logAnnotation.module());
        sysLog.setDescription(logAnnotation.description());

        // 获取方法名和类名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        sysLog.setMethod(className + "." + methodName);

        // 如果没有设置描述，使用Swagger的@Operation注解
        if (StringUtils.isEmpty(sysLog.getOperation())) {
            Operation operation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Operation.class);
            if (operation != null) {
                sysLog.setOperation(operation.summary());
            }
        }
    }

    private void setUserInfo(SysLog sysLog) {
        try {
            if (StpUtil.isLogin()) {
                sysLog.setUserId(StpUtil.getLoginIdAsString());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }
    }

    private void setRequestParams(ProceedingJoinPoint joinPoint, SysLog sysLog) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = JSON.toJSONString(args);
                // 过滤敏感信息
                params = filterSensitiveInfo(params);
                // 参数过长则截取
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                sysLog.setParams(params);
            }
        } catch (Exception e) {
            log.warn("记录请求参数失败: {}", e.getMessage());
        }
    }

    /**
     * 过滤敏感信息
     */
    private String filterSensitiveInfo(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return jsonString;
        }

        // 使用正则表达式替换敏感字段的值为******
        String[] sensitiveFields = {"password", "pwd", "pass", "username", "userName",
                "account", "email", "e-mail", "telephone", "phone",
                "mobile", "idCard", "identityCard", "bankCard"};

        String filteredJson = jsonString;
        for (String field : sensitiveFields) {
            // 匹配模式："field":"任意字符"
            String pattern = "\"" + field + "\"\\s*:\\s*\"[^\"]*\"";
            String replacement = "\"" + field + "\":\"******\"";
            filteredJson = filteredJson.replaceAll(pattern, replacement);
        }

        return filteredJson;
    }

    @Async
    public void saveLog(SysLog sysLog) {
        try {
            sysLogService.save(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }

    /**
     * 设置IP信息
     */
    private void setIpInfo(SysLog sysLog) {
        try {
            String ip = getClientIP();
            sysLog.setIp(ip);
        } catch (Exception e) {
            log.warn("获取IP地址失败: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIP() {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}