package io.charlie.web.oj.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证 配置类 
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenConfigure implements WebMvcConfigurer {
    private final SecurityProperties properties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            log.debug("请求 path={} 提交 token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue());
            SaRouter
                    .match("/**")
                    .notMatch(properties.getIgnore().getUrls())
                    .notMatch(SaHttpMethod.OPTIONS)
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }


    // 注册 Sa-Token 全局过滤器
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico")
//                .setAuth(obj -> {
//                    // 校验 Same-Token 身份凭证     —— 以下两句代码可简化为：SaSameUtil.checkCurrentRequestToken();
//                    String token = SaHolder.getRequest().getHeader(SaSameUtil.SAME_TOKEN);
//                    SaSameUtil.checkToken(token);
//                })
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {


                    SaHolder.getResponse()

                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                            // 允许凭证
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                            // 允许所有请求方式
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*")
                            // 允许的header参数
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                            // 有效时间
                            .setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600")
                            // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff")
                    ;

                    //                     如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS)
                            .free(r -> log.info("OPTIONS 预检请求(不做处理) 请求 path={}  提交 token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue()))
                            .back();
                })
                .setError(e -> JSONUtil.toJsonStr(Result.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
    }
}
