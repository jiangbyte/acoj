package github.jiangbyte.io.sys.modules.job.support;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.entity.SysJobLog;
import github.jiangbyte.io.sys.modules.job.mapper.SysJobLogMapper;
import github.jiangbyte.io.sys.modules.job.mapper.SysJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 任务执行服务：以 Redis 分布式锁（@Lock4j）串行化同一任务的执行，防止多实例重复执行；
 * 锁内重查到期时间，规避「扫描-执行」间隙造成的双跑；执行完成后原子更新任务时间并写执行日志。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutionService {

    /** 执行锁持有时间（毫秒）：覆盖任务执行全程；实例崩溃后由锁自动过期放行重试。 */
    private static final long LOCK_EXPIRE_MS = 1_800_000L;

    /** 获取锁等待时间（毫秒）：等待期内锁被释放则可接力执行，超时抛 LockException 跳过。 */
    private static final long LOCK_ACQUIRE_TIMEOUT_MS = 1_000L;

    private static final String EXECUTOR_SYSTEM = "system";

    private static final String IP = resolveLocalIp();
    private static final String PROCESS_ID = String.valueOf(ProcessHandle.current().pid());
    private static final String APP_DIR = System.getProperty("user.dir", "");

    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final JobInvoker jobInvoker;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    /**
     * 执行指定任务。
     *
     * @param jobId    任务 ID
     * @param force    是否立即执行（true 表示人工触发，跳过到期校验）
     * @param executor 执行人（人工触发为账号 id，调度触发为 system）
     */
    @Lock4j(keys = {"'sys:job:run:' + #jobId"}, expire = LOCK_EXPIRE_MS, acquireTimeout = LOCK_ACQUIRE_TIMEOUT_MS)
    public void runJob(String jobId, boolean force, String executor) {
        SysJob job = jobMapper.selectById(jobId);
        if (job == null || !Boolean.TRUE.equals(job.getEnabled())) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (!force && job.getNextRunTime() != null && job.getNextRunTime().isAfter(now)) {
            // 锁内二次校验：到期时间已被推进，说明其他实例已执行，跳过
            return;
        }

        OffsetDateTime start = now;
        boolean success = false;
        String result;
        try {
            JobHandler handler = jobInvoker.resolve(job.getHandler());
            result = handler.execute(paramJson(job.getParams()));
            success = true;
        } catch (Exception ex) {
            log.error("Job execute failed, id={}, name={}", job.getId(), job.getName(), ex);
            result = "failed: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }

        long durationMs = Duration.between(start, OffsetDateTime.now()).toMillis();
        OffsetDateTime nextRunTime = JobCronUtil.computeNextRunTime(
                job.getTriggerType(), job.getTriggerConfig(), OffsetDateTime.now());
        String operator = executor == null || executor.isBlank() ? EXECUTOR_SYSTEM : executor;
        OffsetDateTime finalStart = start;
        boolean finalSuccess = success;
        String finalResult = result;
        transactionTemplate.executeWithoutResult(status -> {
            jobMapper.update(null, Wrappers.<SysJob>lambdaUpdate()
                    .eq(SysJob::getId, job.getId())
                    .set(SysJob::getLastRunTime, finalStart)
                    .set(SysJob::getNextRunTime, nextRunTime)
                    .set(SysJob::getLastResult, truncate(finalResult, 500))
                    .set(SysJob::getUpdatedAt, OffsetDateTime.now()));
            jobLogMapper.insert(buildLog(job, operator, finalStart, durationMs, finalSuccess, finalResult));
        });
        log.info("Job done, id={}, name={}, success={}, result={}",
                job.getId(), job.getName(), success, result);
    }

    private SysJobLog buildLog(SysJob job, String executor, OffsetDateTime start, long durationMs,
                               boolean success, String result) {
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(job.getId());
        jobLog.setParams(job.getParams());
        jobLog.setStartedAt(start);
        jobLog.setDurationMs(durationMs);
        jobLog.setSuccess(success);
        jobLog.setResult(result);
        jobLog.setExecutor(executor);
        jobLog.setIp(IP);
        jobLog.setProcessId(PROCESS_ID);
        jobLog.setAppDir(APP_DIR);
        return jobLog;
    }

    private String paramJson(Map<String, Object> param) {
        if (param == null || param.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(param);
        } catch (Exception ex) {
            return param.toString();
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static String resolveLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }
}
