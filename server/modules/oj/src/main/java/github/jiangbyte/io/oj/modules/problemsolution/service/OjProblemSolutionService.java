package github.jiangbyte.io.oj.modules.problemsolution.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionAddParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionEditParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionPageParam;

import java.util.List;

/**
 * OJ 参考答案服务：CRUD 与按题列表。
 *
 * Author: Charlie
 */
public interface OjProblemSolutionService extends IService<OjProblemSolution> {

    /** 创建。 */
    void create(OjProblemSolutionAddParam param);

    /** 更新。 */
    void update(OjProblemSolutionEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    OjProblemSolution detail(String id);

    /** 分页查询。 */
    Page<OjProblemSolution> page(OjProblemSolutionPageParam param);

    /** 题目下全部参考答案（真 list）。 */
    List<OjProblemSolution> listByProblemId(String problemId);
}
