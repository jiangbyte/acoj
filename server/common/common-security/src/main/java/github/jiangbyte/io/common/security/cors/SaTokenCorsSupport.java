package github.jiangbyte.io.common.security.cors;

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaStrategy;
import github.jiangbyte.io.common.security.config.HeiSecurityProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Sa-Token CORS 支持：安装策略并按解析结果设置跨域响应头。
 *
 * Author: Charlie
 */
public final class SaTokenCorsSupport {

    private SaTokenCorsSupport() {
    }

    /** 安装 CORS 处理回调到 SaStrategy。 */
    public static void install(HeiSecurityProperties securityProperties, Environment environment) {
        List<String> allowed = List.copyOf(CorsOriginResolver.resolve(securityProperties, environment));
        boolean allowAny = CorsOriginResolver.allowsAny(allowed);
        SaStrategy.instance.corsHandle = (req, res, sto) -> apply(req, res, allowed, allowAny);
    }

    static void apply(SaRequest req, SaResponse res, List<String> allowed, boolean allowAny) {
        String origin = req.getHeader("Origin");
        if (allowAny) {
            res.setHeader("Access-Control-Allow-Origin", "*");
            // 浏览器禁止 * 与 credentials 同开
        } else if (StringUtils.hasText(origin) && CorsOriginResolver.isAllowed(origin, allowed)) {
            res.setHeader("Access-Control-Allow-Origin", origin.trim());
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Vary", "Origin");
        } else {
            // 非跨域或不在白名单：不写 CORS 头
            if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                SaRouter.back("");
            }
            return;
        }

        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "*");
        res.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(200);
            SaRouter.back("");
        }
    }
}
