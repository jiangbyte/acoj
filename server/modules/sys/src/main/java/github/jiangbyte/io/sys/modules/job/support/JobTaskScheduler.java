package github.jiangbyte.io.sys.modules.job.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.mapper.SysJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 任务调度器：周期轮询到期任务并提交线程池异步执行；
 * 并发防护由 JobExecutionService 的 Redis 分布式锁承担，锁被其他实例持有时静默跳过。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobTaskScheduler {

    /** 单轮最多提交的任务数，防止积压风暴。 */
    private static final int MAX_SCAN_LIMIT = 50;

    private final SysJobMapper jobMapper;
    private final JobExecutionService jobExecutionService;
    private final TaskExecutor jobTaskExecutor;

    @Scheduled(fixedDelayString = "${hei.job.scan-interval-ms:1000}")
    public void scan() {
        List<SysJob> dueJobs = jobMapper.selectList(Wrappers.<SysJob>lambdaQuery()
                .eq(SysJob::getEnabled, true)
                .isNotNull(SysJob::getNextRunTime)
                .le(SysJob::getNextRunTime, OffsetDateTime.now())
                .orderByAsc(SysJob::getSort)
                .orderByAsc(SysJob::getNextRunTime)
                .last("LIMIT " + MAX_SCAN_LIMIT));
        for (SysJob job : dueJobs) {
            jobTaskExecutor.execute(() -> {
                try {
                    jobExecutionService.runJob(job.getId(), false, "system");
                } catch (Exception ex) {
                    // 锁获取超时（其他实例执行中）或执行期异常，下个周期重试
                    log.debug("Job skipped, id={}, name={}, msg={}",
                            job.getId(), job.getName(), ex.getMessage());
                }
            });
        }
    }
}
