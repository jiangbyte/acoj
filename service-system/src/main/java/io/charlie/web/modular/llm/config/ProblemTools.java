package io.charlie.web.modular.llm.config;

import io.charlie.web.modular.data.problem.service.DataProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 题目上下文工具；调用过程写入工具轨迹。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemTools {
    private final DataProblemService dataProblemService;
    private final LlmToolTraceRecorder toolTraceRecorder;

    @Tool(description = "Get problem description by problem ID")
    public String getProblemDescriptionById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemDescriptionById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetDescription(id));
    }

    @Tool(description = "Get problem sample input/output cases by problem ID")
    public String getProblemSamplesById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemSamplesById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetSamples(id));
    }

    @Tool(description = "Get problem resource constraints (time/memory limits) by problem ID")
    public String getProblemResourceConstraintsById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemResourceConstraintsById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetResourceConstraints(id));
    }

    @Tool(description = "Get problem difficulty by problem ID")
    public String getProblemDifficultyById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemDifficultyById", Map.of("id", id), toolContext,
                () -> dataProblemService.getDifficulty(id));
    }

    @Tool(description = "Get problem source by problem ID")
    public String getProblemSourceById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemSourceById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetSource(id));
    }

    @Tool(description = "Get problem tags by problem ID")
    public String getProblemTagsById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemTagsById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetTags(id));
    }

    @Tool(description = "Get problem category by problem ID")
    public String getProblemCategoryById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemCategoryById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetCategory(id));
    }

    @Tool(description = "Get allowed submission languages for the problem by problem ID")
    public String getProblemOpenLanguageById(@ToolParam(description = "Problem ID") String id, ToolContext toolContext) {
        return toolTraceRecorder.trace("getProblemOpenLanguageById", Map.of("id", id), toolContext,
                () -> dataProblemService.llmGetOpenLanguage(id));
    }
}
