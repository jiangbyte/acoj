package io.charlie.app.core.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证 配置类 
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册 Sa-Token 全局过滤器 
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
//                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    SaSameUtil.checkCurrentRequestToken();
                })
//                .setError(Throwable::getMessage);
                .setError(e -> JSONUtil.toJsonStr(Result.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
    }
}