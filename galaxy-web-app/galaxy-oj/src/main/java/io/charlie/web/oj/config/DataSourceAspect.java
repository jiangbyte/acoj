package io.charlie.web.oj.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class DataSourceAspect {

    @Before("execution(* io.charlie.web.oj.modular..*.*(..))")
    public void before(JoinPoint point) {
        String ds = DynamicDataSourceContextHolder.peek();

        // 获取完整的类名和方法名
        String className = point.getTarget().getClass().getName();
        String simpleClassName = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();

        // 获取参数信息
        Object[] args = point.getArgs();
        String argsInfo = Arrays.stream(args)
                .map(arg -> arg != null ? arg.getClass().getSimpleName() : "null")
                .collect(Collectors.joining(", "));

        log.info("🔥 {}.{}({}) | 数据源: {}",
                simpleClassName,
                methodName,
                argsInfo,
                ds != null ? ds : "master（默认）");
    }
}