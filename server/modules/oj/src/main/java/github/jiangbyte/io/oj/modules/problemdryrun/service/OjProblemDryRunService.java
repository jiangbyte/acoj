package github.jiangbyte.io.oj.modules.problemdryrun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemApplyLimitsParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunPageParam;
import github.jiangbyte.io.oj.modules.problemdryrun.param.OjProblemDryRunParam;

/**
 * OJ 管理端试跑与限额写回。
 *
 * Author: Charlie
 */
public interface OjProblemDryRunService extends IService<OjProblemDryRun> {

    /** 同步试跑并落历史。 */
    OjProblemDryRun dryRun(OjProblemDryRunParam param);

    /** 试跑历史分页。 */
    Page<OjProblemDryRun> page(OjProblemDryRunPageParam param);

    /** 试跑详情。 */
    OjProblemDryRun detail(String id);

    /** 写回题目时限/内存。 */
    void applyLimits(OjProblemApplyLimitsParam param);
}
