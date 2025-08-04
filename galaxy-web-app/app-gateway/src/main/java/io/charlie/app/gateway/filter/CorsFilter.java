package io.charlie.app.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025/4/13
 * @description 跨域过滤器
 */
@Component
public class CorsFilter implements WebFilter, Ordered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (CorsUtils.isCorsRequest(request)) {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            // 支持的请求头
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");

            // 支持的请求方法
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                    HttpMethod.GET.name() + "," +
                            HttpMethod.POST.name() + "," +
                            HttpMethod.PUT.name() + "," +
                            HttpMethod.DELETE.name() + "," +
                            HttpMethod.OPTIONS.name()
            );

            // 允许跨域的源
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

            // 允许前端访问的响应头
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");

            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

            // 预检请求缓存时间
            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600L");

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
        }

        return chain.filter(exchange);
    }
}
