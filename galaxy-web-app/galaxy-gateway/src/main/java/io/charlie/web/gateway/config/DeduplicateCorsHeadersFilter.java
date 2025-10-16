//package io.charlie.web.gateway.config;
//
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Component
//public class DeduplicateCorsHeadersFilter implements GlobalFilter, Ordered {
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//            ServerHttpResponse response = exchange.getResponse();
//            if (response.getHeaders().containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
//                // 如果存在多个值，只保留第一个
//                List<String> origins = response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
//                if (origins != null && origins.size() > 1) {
//                    response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origins.getFirst());
//                }
//            }
//        }));
//    }
//
//    @Override
//    public int getOrder() {
//        return Ordered.LOWEST_PRECEDENCE; // 最后执行
//    }
//}