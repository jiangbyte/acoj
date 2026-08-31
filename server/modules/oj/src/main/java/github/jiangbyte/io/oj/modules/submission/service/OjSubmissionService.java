package github.jiangbyte.io.oj.modules.submission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionCreateParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionPageParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionUpdateNoteParam;

/**
 * OJ 提交服务接口：管理端查询与门户创建。
 *
 * Author: Charlie
 */
public interface OjSubmissionService extends IService<OjSubmission> {

    /** 查询详情。 */
    OjSubmission detail(String id);

    /** 分页查询。 */
    Page<OjSubmission> page(OjSubmissionPageParam param);

    /**
     * 门户用户提交代码并入队判题（账户取自当前登录用户）。
     */
    OjSubmission createForPortal(OjSubmissionCreateParam param);

    /** 门户：本人提交详情。 */
    OjSubmission portalDetail(String id);

    /** 门户：本人提交分页。 */
    Page<OjSubmission> portalPage(OjSubmissionPageParam param);

    /**
     * 门户：更新本人提交备注。
     */
    OjSubmission updateNoteForPortal(OjSubmissionUpdateNoteParam param);
}
