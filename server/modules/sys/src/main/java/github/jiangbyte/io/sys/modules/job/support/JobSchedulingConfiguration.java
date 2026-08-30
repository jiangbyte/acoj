package github.jiangbyte.io.sys.modules.job.support;

import github.jiangbyte.io.sys.modules.job.config.HeiJobProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 任务调度配置：启用调度并定义任务执行线程池（避免长任务阻塞轮询）。
 *
 * Author: Charlie
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(HeiJobProperties.class)
public class JobSchedulingConfiguration {

    /** 任务执行线程池：池满时由调度线程兜底执行（CallerRunsPolicy）。 */
    @Bean("jobTaskExecutor")
    public ThreadPoolTaskExecutor jobTaskExecutor(@Value("${hei.job.pool-size:4}") int poolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("job-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
