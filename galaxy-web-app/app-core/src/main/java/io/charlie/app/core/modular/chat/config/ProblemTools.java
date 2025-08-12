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
            return proProblemService.getById(id).getDescription();
        } catch (Exception e) {
            log.error("获取题目描述失败，题目ID: {}", id, e);
            return "无法获取题目描述";
        }
    }

    @Tool(description = "通过题目id获取题目标题")
    public String getProblemTitleById(@ToolParam(description = "题目ID") String id) {
        try {
            return proProblemService.getById(id).getTitle();
        } catch (Exception e) {
            log.error("获取题目标题失败，题目ID: {}", id, e);
            return "未知标题";
        }
    }

    @Tool(description = "通过题目id随机获取一个测试用例（包含输入和输出）")
    public String getProblemTestCaseById(@ToolParam(description = "题目ID") String id) {
        try {
            List<TestCase> testCases = proProblemService.getById(id).getTestCase();

            if (testCases == null || testCases.isEmpty()) {
                return "该题目暂无测试用例";
            }

            // 随机选择一个测试用例
            TestCase selectedCase = testCases.get(random.nextInt(testCases.size()));

            return String.format("""
                            输入:
                            %s

                            预期输出:
                            %s
                            """,
                    selectedCase.getInput(),
                    selectedCase.getOutput());
        } catch (Exception e) {
            log.error("获取测试用例失败，题目ID: {}", id, e);
            return "无法获取测试用例";
        }
    }
}
