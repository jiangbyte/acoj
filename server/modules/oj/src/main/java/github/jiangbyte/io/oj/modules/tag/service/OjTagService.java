package github.jiangbyte.io.oj.modules.tag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import github.jiangbyte.io.oj.modules.tag.param.OjTagAddParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagEditParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagPageParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OJ 标签服务接口：CRUD 与题目打标。
 *
 * Author: Charlie
 */
public interface OjTagService extends IService<OjTag> {

    /** 创建。 */
    void create(OjTagAddParam param);

    /** 更新。 */
    void update(OjTagEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    OjTag detail(String id);

    /** 分页查询。 */
    Page<OjTag> page(OjTagPageParam param);

    /** 覆盖设置题目的标签关联。 */
    void setProblemTags(String problemId, List<String> tagIds);

    /** 查询题目已绑定标签。 */
    List<OjTag> listByProblemId(String problemId);

    /**
     * 批量查询题目已绑定标签。
     * 单个 IN 超过 500 时分批。
     */
    Map<String, List<OjTag>> mapByProblemIds(Collection<String> problemIds);

    /** 启用中的标签列表（选择器用）。 */
    List<OjTag> listEnabled();

    /** 填充题目 tags / tagIds。 */
    void fillProblemTags(OjProblem problem);

    /** 批量填充题目 tags / tagIds。 */
    void fillProblemTags(List<OjProblem> problems);
}
