package github.jiangbyte.io.sys.modules.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.sys.modules.job.entity.SysJobLog;
import github.jiangbyte.io.sys.modules.job.param.SysJobLogPageParam;

/**
 * 任务执行记录服务接口。
 *
 * Author: Charlie
 */
public interface JobLogService extends IService<SysJobLog> {

    /** 分页查询执行记录。 */
    Page<SysJobLog> page(SysJobLogPageParam param);

    /**
     * 清理过期执行日志。
     *
     * @param retentionDays 保留天数；小于等于 0 时不删除
     * @param batchSize 单次删除上限；小于等于 0 时按 1000 处理
     * @return 实际删除行数
     */
    int cleanupExpired(int retentionDays, int batchSize);
}
