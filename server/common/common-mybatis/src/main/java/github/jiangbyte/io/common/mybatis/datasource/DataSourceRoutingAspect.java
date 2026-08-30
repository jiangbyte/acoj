package github.jiangbyte.io.common.mybatis.datasource;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 数据源路由切面：根据 @ReadDataSource 切换到读库；写后粘主时跳过。
 *
 * Author: Charlie
 */
@Aspect
@Order(-1)
public class DataSourceRoutingAspect {

    /** 切换到读库数据源。 */
    @Around("@within(github.jiangbyte.io.common.mybatis.datasource.ReadDataSource) || @annotation(github.jiangbyte.io.common.mybatis.datasource.ReadDataSource)")
    public Object routeRead(ProceedingJoinPoint joinPoint) throws Throwable {
        if (DataSourceSticky.isSticky()) {
            return joinPoint.proceed();
        }
        return route(joinPoint, DataSourceNames.SLAVE);
    }

    private Object route(ProceedingJoinPoint joinPoint, String dataSourceName) throws Throwable {
        DynamicDataSourceContextHolder.push(dataSourceName);
        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
