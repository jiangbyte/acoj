package io.charlie.app.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.app.gateway.config.properties.SecurityProperties;
import io.charlie.app.gateway.result.Result;
import io.charlie.app.gateway.result.ResultCode;
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
//                .setError(Throwable::getMessage);
                .setError(e -> JSONUtil.toJsonStr(Result.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
    }
}
