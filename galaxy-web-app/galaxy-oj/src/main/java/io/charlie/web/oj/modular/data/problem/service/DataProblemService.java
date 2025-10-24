package io.charlie.web.oj.modular.data.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.entity.DataProblemCount;
import io.charlie.web.oj.modular.data.problem.param.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题目表 服务类
*/
public interface DataProblemService extends IService<DataProblem> {
    Page<DataProblem> page(DataProblemPageParam dataProblemPageParam);

    Page<DataProblem> pageClient(DataProblemPageParam dataProblemPageParam);

    Page<DataProblem> setPage(DataProblemPageParam dataProblemPageParam);

    void add(DataProblemAddParam dataProblemAddParam);

    void edit(DataProblemEditParam dataProblemEditParam);

    void delete(List<DataProblemIdParam> dataProblemIdParamList);

    DataProblem detail(DataProblemIdParam dataProblemIdParam);

    List<DataProblem> latestN(int n);

    DataProblemCount getProblemCount();

    List<DataProblem> getHotN(int n);

    List<DifficultyDistribution> difficultyDistribution();

    void importProblems(MultipartFile file);

    // LLM 工具调用接口
    // 获取题目描述
    String llmGetDescription(String id);

    // 获取题目测试用例（随机一个）
    String llmGetTestCase(String id);

    // 获取题目约束条件
    String llmGetResourceConstraints(String id);

    // 获取题目示例（第一个测试用例）
    String llmGetExample(String id);

    // 获取题目难度
    String getDifficulty(String id);

    // 获取题目来源
    String llmGetSource(String id);

    // 获取题目标签
    String llmGetTags(String id);

    // 获取题目分类
    String llmGetCategory(String id);

    // 获取题目支持语言
    String llmGetOpenLanguage(String id);
}