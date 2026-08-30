package github.jiangbyte.io.common.job;

/**
 * 任务处理器 SPI：调度引擎按 sys_job.handler 查找容器中对应的 Spring Bean 并调用。
 *
 * Author: Charlie
 */
@FunctionalInterface
public interface JobHandler {

    /**
     * 执行任务。
     *
     * @param params 任务参数 JSON 串（sys_job.params 序列化结果）；无参数时为 null
     * @return 执行结果摘要（写入 sys_job_log.result 与 sys_job.last_result）
     * @throws Exception 执行失败时抛出，由调度引擎记录失败日志
     */
    String execute(String params) throws Exception;
}
