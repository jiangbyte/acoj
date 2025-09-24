package io.charlie.web.oj.modular.llm.config;

import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 30/06/2025
 * @description AI 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemTools {
    private final DataProblemService dataProblemService;
    @Tool(description = "通过题目id获取题目描述")
    public String getProblemDescriptionById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetDescription(id);
    }
    @Tool(description = "通过题目id随机获取一个测试用例")
    public String getProblemTestCaseById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetTestCase(id);
    }
    @Tool(description = "通过题目id获取题目约束条件")
    public String getProblemConstraintsById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetConstraints(id);
    }
    @Tool(description = "通过题目id获取题目示例")
    public String getProblemExampleById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetExample(id);
    }
    @Tool(description = "通过题目id获取题目难度")
    public String getProblemDifficultyById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.getDifficulty(id);
    }
    @Tool(description = "通过题目id获取题目来源")
    public String getProblemSourceById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetSource(id);
    }
    @Tool(description = "通过题目id获取题目标签")
    public String getProblemTagsById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetTags(id);
    }

    @Tool(description = "通过题目id获取题目分类")
    public String getProblemCategoryById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetCategory(id);
    }

    @Tool(description = "通过题目id获取题目设置的允许提交语言")
    public String getProblemOpenLanguageById(@ToolParam(description = "题目ID") String id) {
        return dataProblemService.llmGetOpenLanguage(id);
    }
}
