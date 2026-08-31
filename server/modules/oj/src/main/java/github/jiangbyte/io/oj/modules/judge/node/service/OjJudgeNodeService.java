package github.jiangbyte.io.oj.modules.judge.node.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeAddParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeEditParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodePageParam;
import github.jiangbyte.io.oj.modules.judge.node.result.OjJudgeLanguagesResult;

/**
 * OJ 执行机服务接口：CRUD。
 *
 * Author: Charlie
 */
public interface OjJudgeNodeService extends IService<OjJudgeNode> {

    /** 创建。 */
    void create(OjJudgeNodeAddParam param);

    /** 更新。 */
    void update(OjJudgeNodeEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    OjJudgeNode detail(String id);

    /** 分页查询。 */
    Page<OjJudgeNode> page(OjJudgeNodePageParam param);

    /**
     * 聚合 ENABLED 执行机的非空 supportedLanguages（去重排序）。
     * 空列表节点跳过（未上报不算进目录）。
     */
    OjJudgeLanguagesResult listAggregatedLanguages();
}
