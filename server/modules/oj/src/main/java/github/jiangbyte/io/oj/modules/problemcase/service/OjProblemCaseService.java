package github.jiangbyte.io.oj.modules.problemcase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseAddParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseEditParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseItemParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCasePageParam;

import java.util.List;

/**
 * OJ 题目测例服务接口：CRUD 与整包升版本。
 *
 * Author: Charlie
 */
public interface OjProblemCaseService extends IService<OjProblemCase> {

    /** 创建。 */
    void create(OjProblemCaseAddParam param);

    /** 更新。 */
    void update(OjProblemCaseEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    OjProblemCase detail(String id);

    /** 分页查询。 */
    Page<OjProblemCase> page(OjProblemCasePageParam param);

    /**
     * 递增题目 caseVersion，并按新版本批量插入测例（保留历史版本行）。
     */
    void replaceCasesForNewVersion(String problemId, List<OjProblemCaseItemParam> cases);
}
