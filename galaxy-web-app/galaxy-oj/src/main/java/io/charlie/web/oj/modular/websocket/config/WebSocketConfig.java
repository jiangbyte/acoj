package io.charlie.web.oj.modular.websocket.config;

import cn.dev33.satoken.context.SaHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";

    // 主题常量定义
    public static final String TOPIC_JUDGE_STATUS = "/topic/judge/status";
    public static final String TOPIC_SIMILARITY_STATUS = "/topic/similarity/status";

    // 端点常量定义
    public static final String ENDPOINT_JUDGE_STATUS = "/ws/judge/status";
    public static final String ENDPOINT_SIMILARITY_STATUS = "/ws/similarity/status";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置多个主题前缀，按功能区分
        config.enableSimpleBroker(
                TOPIC_JUDGE_STATUS,     // 判题状态主题
                TOPIC_SIMILARITY_STATUS // 克隆状态主题
        );

        // 设置应用程序目的地的前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // OJ判题状态端点
        registry.addEndpoint(ENDPOINT_JUDGE_STATUS)
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 克隆状态端点
        registry.addEndpoint(ENDPOINT_SIMILARITY_STATUS)
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 原生WebSocket端点
        registry.addEndpoint(ENDPOINT_JUDGE_STATUS + "-native")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint(ENDPOINT_SIMILARITY_STATUS + "-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(512 * 1024);
        registry.setSendTimeLimit(20 * 1000);
        registry.setSendBufferSizeLimit(1024 * 1024);
    }


}