package io.charlie.app.core.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EntityAspect {
    
    @Before("execution(* io.charlie.galaxy.pojo.CommonEntity+.*(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        // 方法执行前的增强逻辑
    }
    
    @AfterReturning(pointcut = "execution(* io.charlie.galaxy.pojo.CommonEntity+.*(..))", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        // 方法执行后的增强逻辑
    }
}