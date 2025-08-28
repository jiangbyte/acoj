package io.charlie.app.core.modular.set.problems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.SetProblemPageParam;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description TODO
 */
public interface ProSetProblemService extends IService<ProSetProblem> {
    // 获得某个用户最近解题记录
    Page<ProProblem> setProblemPage(SetProblemPageParam setProblemPageParam);
}
