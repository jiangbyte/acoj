package io.charlie.web.gateway.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.web.gateway.config.properties.SecurityProperties;
import io.charlie.web.gateway.result.Result;
import io.charlie.web.gateway.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025/4/13
 * @description [Sa-Token 权限认证] 全局配置类
 */
@Configuration
@Slf4j
public class SaTokenConfigure {
    // 从 0 分钟开始 每隔 5 分钟执行一次 Same-Token
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void refreshToken() {
        SaSameUtil.refreshToken();
    }

    @Bean
    public SaReactorFilter getSaReactorFilter(SecurityProperties properties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                            SaRouter.match("/**")
                                    .notMatch(properties.getIgnore().getUrls())
                                    .check(r -> StpUtil.checkLogin());
                            log.info("请求 path={} 提交 token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue());
                        }
                )
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
                    ;

                    // 如果是预检请求，则立即返回到前端
//                    SaRouter.match(SaHttpMethod.OPTIONS)
//                            .free(r -> log.info("OPTIONS 预检请求(不做处理) 请求 path={}  提交 token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue()))
//                            .back();
                })
//                .setError(Throwable::getMessage);
                .setError(e -> JSONUtil.toJsonStr(Result.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
    }


}
