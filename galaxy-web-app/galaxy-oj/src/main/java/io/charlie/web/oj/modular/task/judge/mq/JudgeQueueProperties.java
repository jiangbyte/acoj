package io.charlie.web.oj.modular.task.judge.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 判题队列配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "oj.mq.judge")
public class JudgeQueueProperties {
    
    /**
     * 普通判题队列配置
     */
    private QueueConfig common;
    
    /**
     * 判题结果队列配置
     */
    private QueueConfig result;
    
    @Data
    public static class QueueConfig {
        /**
         * 交换机名称
         */
        private String exchange;
        
        /**
         * 队列名称
         */
        private String queue;
        
        /**
         * 路由键
         */
        private String routingKey;
    }
}