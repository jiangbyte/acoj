package io.charlie.app.core.modular.set.set.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.UserProblemPageParam;
import io.charlie.app.core.modular.set.set.entity.ProSet;
import io.charlie.app.core.modular.set.set.param.*;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题集 服务类
 */
public interface ProSetService extends IService<ProSet> {
    Page<ProSet> page(ProSetPageParam proSetPageParam);

    void add(ProSetAddParam proSetAddParam);

    void edit(ProSetEditParam proSetEditParam);

    void delete(List<ProSetIdParam> proSetIdParamList);

    ProSet detail(ProSetIdParam proSetIdParam);

    List<ProSet> latestN(int n);

    // 获得某个用户最近参与记录
    Page<ProSet> userRecentSolvedPage(UserSetPageParam userSetPageParam);

}