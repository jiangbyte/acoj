package io.charlie.app.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.app.gateway.config.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025/4/13
 * @description [Sa-Token 权限认证] 全局配置类
 */
@Configuration
@Slf4j
public class SaTokenConfigure {

    @Bean
    public SaReactorFilter getSaReactorFilter(SecurityProperties properties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                            SaRouter.match("/**")
                                    .notMatch(properties.getIgnore().getUrls())
                                    .check(r -> StpUtil.checkLogin());
                        }
                )
                .setError(Throwable::getMessage);
    }

    /**
     * 注册 [Sa-Token全局过滤器]
     */
//    @Bean
//    public SaReactorFilter getSaReactorFilter(SecurityProperties properties) {
//        return new SaReactorFilter()
//                .addInclude("/**")  /* 拦截全部path */
////                .addExclude("/favicon.ico")
//                // 鉴权方法：每次访问进入
//                .setAuth(obj -> {
////                             登录校验 -- 拦截所有路由，并排除用于开放登录
//                            SaRouter.match("/**")
//                                    .notMatch(properties.getIgnore().getUrls())
//                                    .check(r -> StpUtil.checkLogin());
//
//                            // 权限认证 -- 不同模块, 校验不同权限，针对管理端
//                            SaRouter.match("/contest/api/admin/**/get", r -> StpUtil.checkPermission("contest.view"));
//                            SaRouter.match("/contest/api/admin/**/options", r -> StpUtil.checkPermission("contest.view"));
//                            SaRouter.match("/contest/api/admin/**/page", r -> StpUtil.checkPermission("contest.view"));
//                            SaRouter.match("/contest/api/admin/**/update", r -> StpUtil.checkPermission("contest.manage"));
//                            SaRouter.match("/contest/api/admin/**/status/*", r -> StpUtil.checkPermission("contest.manage"));
//                            SaRouter.match("/contest/api/admin/**/create", r -> StpUtil.checkPermission("contest.manage"));
//                            SaRouter.match("/contest/api/admin/**/delete", r -> StpUtil.checkPermission("contest.manage"));
//
//                        }
//                )
//                .setError(e -> JSONUtil.toJsonStr(Result.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
//    }
}
