package github.jiangbyte.io.common.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全模块配置属性：匿名 URL、CORS、HSTS/CSP 与转发头信任等。
 * <p>
 * Cookie 安全属性请使用 {@code sa-token.cookie.*}，不另设 CSRF 开关。
 *
 * Author: Charlie
 */
@Getter
@ConfigurationProperties(prefix = "hei.security")
public class HeiSecurityProperties {

    /**
     * 额外匿名 URL 模式（Ant 风格），与 expose-* 条件规则合并后交给 SaRouter.notMatch。
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /** 无需鉴权暴露 Swagger / Knife4j / OpenAPI（生产环境请关闭）。 */
    @Setter
    private boolean exposeDocs = true;

    /** 无需 Sa-Token 暴露 /actuator/**（生产环境请关闭；配合网络 ACL）。 */
    @Setter
    private boolean exposeActuator = true;

    /** 无需 Sa-Token 暴露 /druid/**（生产环境请关闭）。 */
    @Setter
    private boolean exposeDruid = true;

    /**
     * 为 true 时，Filter / 解析器可信任 X-Forwarded-For / X-Real-IP。
     * 除非位于会覆写这些头的可信反向代理之后，否则保持 false。
     */
    @Setter
    private boolean trustForwardedHeaders = false;

    /**
     * 允许的 CORS 源（由 Sa-Token {@code SaStrategy.corsHandle} 使用）。
     * 为空：本地默认 5173/5174/5163 + 追加 {@code hei.app.frontend-base-url}。
     * 含 {@code *}：通配且强制关闭 credentials（对齐 fastapi）。
     */
    private List<String> corsAllowedOrigins = new ArrayList<>();

    /**
     * API 响应的 HSTS max-age（秒）。{@code 0} 不写该头（默认）。
     * 仅在经 HTTPS 访问服务时开启。
     */
    @Setter
    private long hstsMaxAgeSeconds = 0;

    @Setter
    private boolean hstsIncludeSubDomains = true;

    @Setter
    private boolean hstsPreload = false;

    /** API 响应可选 CSP；为空则跳过该头（通常由 SPA nginx 负责 CSP）。 */
    private String contentSecurityPolicy = "";

    public void setIgnoreUrls(List<String> ignoreUrls) {
        this.ignoreUrls = ignoreUrls == null ? new ArrayList<>() : ignoreUrls;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins == null ? new ArrayList<>() : corsAllowedOrigins;
    }

    public void setContentSecurityPolicy(String contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy == null ? "" : contentSecurityPolicy;
    }
}
