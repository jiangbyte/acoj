package io.charlie.app.core.modular.problem.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.ProProblemAddParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemEditParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemIdParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemPageParam;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题目表 服务类
 */
public interface ProProblemService extends IService<ProProblem> {
    Page<ProProblem> page(ProProblemPageParam proProblemPageParam);

    void add(ProProblemAddParam proProblemAddParam);

    void edit(ProProblemEditParam proProblemEditParam);

    void delete(List<ProProblemIdParam> proProblemIdParamList);

    ProProblem detail(ProProblemIdParam proProblemIdParam);

    ProProblem detailC(ProProblemIdParam proProblemIdParam);

    Page<ProProblem> pageC(ProProblemPageParam proProblemPageParam);

    List<ProProblem> latestN(int n);
}