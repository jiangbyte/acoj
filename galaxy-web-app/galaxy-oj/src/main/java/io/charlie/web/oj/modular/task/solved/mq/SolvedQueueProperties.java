package io.charlie.web.oj.modular.task.solved.mq;

import io.charlie.web.oj.modular.task.config.QueueConfig;
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
@ConfigurationProperties(prefix = "oj.mq.solved")
public class SolvedQueueProperties {
    
    /**
     * 普通判题队列配置
     */
    private QueueConfig common;
    

}