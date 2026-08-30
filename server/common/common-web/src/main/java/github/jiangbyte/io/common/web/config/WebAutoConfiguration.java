package github.jiangbyte.io.common.web.config;

import github.jiangbyte.io.common.web.filter.AccessLogFilter;
import github.jiangbyte.io.common.web.filter.SnakeCaseRequestParameterFilter;
import github.jiangbyte.io.common.web.filter.TraceIdFilter;
import github.jiangbyte.io.common.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Web 层自动配置：注册 TraceId/访问日志等过滤器与全局异常处理。
 *
 * Author: Charlie
 */
@AutoConfiguration
public class WebAutoConfiguration {

    /** 注册全局异常处理器。 */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /** 注册 TraceId 过滤器。 */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(Environment environment) {
        boolean trustForwarded = environment.getProperty(
                "hei.security.trust-forwarded-headers", Boolean.class, false);
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(trustForwarded));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /** 注册访问日志过滤器。 */
    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilterRegistration() {
        FilterRegistrationBean<AccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessLogFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }

    /**
     * 使 GET/表单 {@code @ModelAttribute} 绑定与 Jackson {@code SNAKE_CASE} 一致
     *（例如 {@code account_type} → {@code accountType}）。
     */
    @Bean
    public FilterRegistrationBean<SnakeCaseRequestParameterFilter> snakeCaseRequestParameterFilterRegistration() {
        FilterRegistrationBean<SnakeCaseRequestParameterFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SnakeCaseRequestParameterFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
