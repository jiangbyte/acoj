package github.jiangbyte.io.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.security.satoken.SessionPermissionProvider;
import github.jiangbyte.io.common.security.web.AccountMdcInterceptor;
import github.jiangbyte.io.common.security.web.CsrfDoubleSubmitFilter;
import github.jiangbyte.io.common.security.web.SessionCookiePathFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 统一安全配置：鉴权白名单、安全响应头、Cookie Path 改写、CSRF 双提交。
 * <p>
 * Cookie 会话启用时，非安全方法校验 {@code HEI_CSRF} / {@code X-HEI-CSRF}。
 *
 * Author: Charlie
 */
@AutoConfiguration
@EnableConfigurationProperties(HeiSecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SaTokenSecurityConfig implements WebMvcConfigurer {

    public static final String TOKEN_COOKIE = "Authorization";

    private final HeiSecurityProperties securityProperties;
    private final Environment environment;
    private final boolean cookieAuthEnabled;

    public SaTokenSecurityConfig(
            HeiSecurityProperties securityProperties,
            Environment environment) {
        this.securityProperties = securityProperties;
        this.environment = environment;
        this.cookieAuthEnabled = environment.getProperty("sa-token.is-read-cookie", Boolean.class, false);
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SecurityHeadersFilter(securityProperties));
        reg.addUrlPatterns("/**");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<SessionCookiePathFilter> sessionCookiePathFilter() {
        String cookieName = environment.getProperty("sa-token.token-name", TOKEN_COOKIE);
        FilterRegistrationBean<SessionCookiePathFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SessionCookiePathFilter(cookieAuthEnabled, cookieName));
        reg.addUrlPatterns("/**");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 12);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<CsrfDoubleSubmitFilter> csrfDoubleSubmitFilter() {
        String cookieName = environment.getProperty("sa-token.token-name", TOKEN_COOKIE);
        FilterRegistrationBean<CsrfDoubleSubmitFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CsrfDoubleSubmitFilter(cookieAuthEnabled, cookieName));
        reg.addUrlPatterns("/**");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 13);
        return reg;
    }

    public static class SecurityHeadersFilter extends OncePerRequestFilter {
        private final HeiSecurityProperties securityProperties;

        public SecurityHeadersFilter(HeiSecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            if (StringUtils.hasText(securityProperties.getContentSecurityPolicy())) {
                response.setHeader("Content-Security-Policy", securityProperties.getContentSecurityPolicy());
            }
            long hsts = securityProperties.getHstsMaxAgeSeconds();
            if (hsts > 0) {
                StringBuilder value = new StringBuilder("max-age=").append(hsts);
                if (securityProperties.isHstsIncludeSubDomains()) {
                    value.append("; includeSubDomains");
                }
                if (securityProperties.isHstsPreload()) {
                    value.append("; preload");
                }
                response.setHeader("Strict-Transport-Security", value.toString());
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public StpInterface stpInterface() {
        return new SessionPermissionProvider();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] ignorePatterns = buildIgnorePatterns();
        registry.addInterceptor(new SaInterceptor(handler -> {
                    SaRouter.match("/api/*/admin/**")
                            .notMatch(ignorePatterns)
                            .check(r -> StpKit.ADMIN.checkLogin());
                    SaRouter.match("/api/*/portal/**")
                            .notMatch(ignorePatterns)
                            .check(r -> StpKit.PORTAL.checkLogin());
                }))
                .addPathPatterns("/**");
        registry.addInterceptor(new AccountMdcInterceptor()).addPathPatterns("/**");
    }

    private String[] buildIgnorePatterns() {
        List<String> patterns = new ArrayList<>(securityProperties.getIgnoreUrls());
        if (securityProperties.isExposeDocs()) {
            patterns.add("/doc.html");
            patterns.add("/doc.html/**");
            patterns.add("/webjars/**");
            patterns.add("/v3/api-docs");
            patterns.add("/v3/api-docs/**");
            patterns.add("/swagger-ui/**");
            patterns.add("/swagger-ui.html");
        }
        if (securityProperties.isExposeActuator()) {
            patterns.add("/actuator");
            patterns.add("/actuator/**");
        }
        if (securityProperties.isExposeDruid()) {
            patterns.add("/druid");
            patterns.add("/druid/**");
        }
        return patterns.toArray(String[]::new);
    }
}
