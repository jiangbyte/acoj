package github.jiangbyte.io.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 启动完成后打印 API / Docs / Health 地址。
 *
 * Author: Charlie
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class StartupReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupReadyListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext webCtx)) {
            return;
        }
        Environment env = webCtx.getEnvironment();
        int port = webCtx.getWebServer().getPort();
        String contextPath = env.getProperty("server.servlet.context-path", "");
        // 空串不 startsWith("/")，旧逻辑会误加成 "/"，打出 http://host:port//doc.html
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
            contextPath = "";
        } else {
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            if (contextPath.endsWith("/") && contextPath.length() > 1) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
        }
        String host = env.getProperty("server.address", "127.0.0.1");
        if (!StringUtils.hasText(host) || "0.0.0.0".equals(host)) {
            host = "127.0.0.1";
        }
        String base = "http://" + host + ":" + port + contextPath;
        String profiles = String.join(",", env.getActiveProfiles());
        if (!StringUtils.hasText(profiles)) {
            profiles = String.join(",", env.getDefaultProfiles());
        }

        boolean exposeDocs = env.getProperty("hei.security.expose-docs", Boolean.class, true);
        boolean docsEnabled = env.getProperty("springdoc.api-docs.enabled", Boolean.class, true)
                && env.getProperty("knife4j.enable", Boolean.class, true);

        StringBuilder sb = new StringBuilder(512);
        sb.append('\n')
                .append("----------------------------------------------------------\n")
                .append(" HEI Boot Admin started\n")
                .append(" Profile(s):  ").append(profiles).append('\n')
                .append(" API:         ").append(base).append('\n');
        if (exposeDocs && docsEnabled) {
            sb.append(" Docs:        ").append(base).append("/doc.html\n")
                    .append(" OpenAPI:     ").append(base).append("/v3/api-docs\n");
        }
        sb.append(" Health:      ").append(base).append("/actuator/health\n")
                .append("----------------------------------------------------------");
        log.info(sb.toString());
    }
}
