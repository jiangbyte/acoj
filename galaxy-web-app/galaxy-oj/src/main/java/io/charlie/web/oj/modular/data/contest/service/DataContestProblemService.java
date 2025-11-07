package io.charlie.web.oj.modular.data.contest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.contest.entity.DataContestProblem;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestProblemPageParam;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛题目表 服务类
*/
public interface DataContestProblemService extends IService<DataContestProblem> {
    Page<DataContestProblem> page(DataContestProblemPageParam dataContestProblemPageParam);

    void add(DataContestProblemAddParam dataContestProblemAddParam);

    void edit(DataContestProblemEditParam dataContestProblemEditParam);

    void delete(List<DataContestProblemIdParam> dataContestProblemIdParamList);

    DataContestProblem detail(DataContestProblemIdParam dataContestProblemIdParam);

    List<DataContestProblem> lists(String contestId);

    DataProblem getProblemDetail(String contestId, String problemId);
}