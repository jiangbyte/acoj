package io.charlie.app.core.modular.chat.config;

import io.charlie.app.core.modular.problem.problem.entity.TestCase;
import io.charlie.app.core.modular.problem.problem.service.ProProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final ProProblemService proProblemService;
    private final Random random = new Random();

    @Tool(description = "通过题目id获取题目描述")
    public String getProblemDescriptionById(@ToolParam(description = "题目ID") String id) {
        try {
            return proProblemService.getDescription(id);
        } catch (Exception e) {
            log.error("获取题目描述失败，题目ID: {}", id, e);
            return "无法获取题目描述";
        }
    }

    @Tool(description = "通过题目id随机获取一个测试用例（包含输入和输出）")
    public String getProblemTestCaseById(@ToolParam(description = "题目ID") String id) {
        try {
            return proProblemService.getTestCase(id);
        } catch (Exception e) {
            log.error("获取测试用例失败，题目ID: {}", id, e);
            return "无法获取测试用例";
        }
    }
}
