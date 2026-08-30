package github.jiangbyte.io.common.satoken.config;

import cn.dev33.satoken.stp.StpLogic;
import github.jiangbyte.io.common.satoken.StpKit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Sa-Token 模块自动配置：注册登录辅助与权限相关 Bean。
 *
 * Author: Charlie
 */
@AutoConfiguration
public class SaTokenAutoConfiguration {

    /** 暴露管理端 StpLogic。 */
    @Bean
    @Primary
    public StpLogic stpLogicAdmin() {
        return StpKit.ADMIN;
    }

    /** 暴露门户端 StpLogic。 */
    @Bean
    public StpLogic stpLogicPortal() {
        return StpKit.PORTAL;
    }
}
