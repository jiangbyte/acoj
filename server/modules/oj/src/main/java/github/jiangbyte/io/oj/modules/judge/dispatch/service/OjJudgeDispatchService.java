package github.jiangbyte.io.oj.modules.judge.dispatch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.param.OjJudgeDispatchPageParam;

/**
 * OJ 派发审计服务接口：管理端查询。
 *
 * Author: Charlie
 */
public interface OjJudgeDispatchService extends IService<OjJudgeDispatch> {

    /** 分页查询。 */
    Page<OjJudgeDispatch> page(OjJudgeDispatchPageParam param);
}
