package github.jiangbyte.io.sys.modules.job.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.sys.modules.job.convert.SysJobConvert;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.mapper.SysJobMapper;
import github.jiangbyte.io.sys.modules.job.param.SysJobAddParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEditParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEnabledParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobPageParam;
import github.jiangbyte.io.sys.modules.job.service.JobService;
import github.jiangbyte.io.sys.modules.job.support.JobCronUtil;
import github.jiangbyte.io.sys.modules.job.support.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 任务服务实现：维护任务定义并触发立即执行。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements JobService {

    private final SysJobConvert jobConvert;
    private final JobExecutionService jobExecutionService;
    private final TaskExecutor jobTaskExecutor;

    @Override
    @Transactional
    public void create(SysJobAddParam param) {
        SysJob job = jobConvert.toEntity(param);
        JobCronUtil.validate(job.getTriggerType(), job.getTriggerConfig());
        job.setNextRunTime(JobCronUtil.computeNextRunTime(
                job.getTriggerType(), job.getTriggerConfig(), OffsetDateTime.now()));
        this.save(job);
        AuditSnapshots.created(job);
    }

    @Override
    @Transactional
    public void update(SysJobEditParam param) {
        SysJob job = this.getById(param.getId());
        if (job == null) {
            throw new BizException(404, "Job not found");
        }
        String oldType = job.getTriggerType();
        String oldConfig = job.getTriggerConfig();
        AuditSnapshots.before(job);
        jobConvert.update(param, job);
        JobCronUtil.validate(job.getTriggerType(), job.getTriggerConfig());
        // 触发配置变更后按新配置重置下次执行时间
        if (!Objects.equals(oldType, job.getTriggerType()) || !Objects.equals(oldConfig, job.getTriggerConfig())) {
            job.setNextRunTime(JobCronUtil.computeNextRunTime(
                    job.getTriggerType(), job.getTriggerConfig(), OffsetDateTime.now()));
        }
        this.updateById(job);
        AuditSnapshots.after(job);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<SysJob> jobs = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(jobs);
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public SysJob detail(String id) {
        SysJob job = this.getById(id);
        if (job == null) {
            throw new BizException(404, "Job not found");
        }
        return job;
    }

    @Override
    @ReadDataSource
    public Page<SysJob> page(SysJobPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysJob>lambdaQuery()
                        .like(StringUtils.hasText(param.getName()), SysJob::getName, param.getName())
                        .eq(StringUtils.hasText(param.getTriggerType()), SysJob::getTriggerType, param.getTriggerType())
                        .eq(param.getEnabled() != null, SysJob::getEnabled, param.getEnabled())
                        .orderByAsc(SysJob::getSort)
                        .orderByDesc(SysJob::getCreatedAt));
    }

    @Override
    @Transactional
    public void updateEnabled(SysJobEnabledParam param) {
        SysJob job = this.getById(param.getId());
        if (job == null) {
            throw new BizException(404, "Job not found");
        }
        AuditSnapshots.before(job);
        job.setEnabled(param.getEnabled());
        // 重新启用时按当前时间重置调度，避免沿用暂停前的过期时间立即触发
        if (Boolean.TRUE.equals(param.getEnabled())) {
            job.setNextRunTime(JobCronUtil.computeNextRunTime(
                    job.getTriggerType(), job.getTriggerConfig(), OffsetDateTime.now()));
        }
        this.updateById(job);
        AuditSnapshots.after(job);
    }

    @Override
    public void runNow(String id) {
        SysJob job = this.getById(id);
        if (job == null) {
            throw new BizException(404, "Job not found");
        }
        String executor = LoginHelper.currentUser()
                .map(LoginUser::getAccountId)
                .orElse("system");
        // 提交线程池异步执行，立即返回；结果写入执行日志
        jobTaskExecutor.execute(() -> {
            try {
                jobExecutionService.runJob(job.getId(), true, executor);
            } catch (Exception ex) {
                // 执行锁被其他实例持有时跳过（如任务正在执行中）
                log.warn("Job run skipped, id={}, name={}, msg={}",
                        job.getId(), job.getName(), ex.getMessage());
            }
        });
    }
}
