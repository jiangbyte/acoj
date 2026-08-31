package github.jiangbyte.io.oj.modules.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemAddParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemEditParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPageParam;

/**
 * OJ 题目服务接口：CRUD。
 *
 * Author: Charlie
 */
public interface OjProblemService extends IService<OjProblem> {

    /** 创建。 */
    void create(OjProblemAddParam param);

    /** 更新。 */
    void update(OjProblemEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    OjProblem detail(String id);

    /** 分页查询。 */
    Page<OjProblem> page(OjProblemPageParam param);

    /** 门户：已发布题目分页。 */
    Page<OjProblem> portalPage(OjProblemPageParam param);

    /** 门户：已发布题目详情。 */
    OjProblem portalDetail(String id);
}
