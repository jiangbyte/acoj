package github.jiangbyte.io.sys.modules.job.sample;

import github.jiangbyte.io.common.job.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例任务：回显执行参数，用于验证调度链路（任务定义在 sys_job 中预置）。
 *
 * Author: Charlie
 */
@Slf4j
@Component
public class SysJobSample implements JobHandler {

    @Override
    public String execute(String params) {
        log.info("SysJobSample execute, params={}", params);
        return "echo: " + (params == null ? "(empty)" : params);
    }
}
