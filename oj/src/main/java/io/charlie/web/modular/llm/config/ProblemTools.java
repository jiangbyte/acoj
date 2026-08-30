package io.charlie.web.modular.llm.config;

import io.charlie.web.modular.data.problem.service.DataProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 30/06/2025
 * @description AI tool methods for problem context
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemTools {
    private final DataProblemService dataProblemService;

    @Tool(description = "Get problem description by problem ID")
    public String getProblemDescriptionById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetDescription(id);
    }

    @Tool(description = "Get problem resource constraints (time/memory limits) by problem ID")
    public String getProblemResourceConstraintsById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetResourceConstraints(id);
    }

    @Tool(description = "Get problem difficulty by problem ID")
    public String getProblemDifficultyById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.getDifficulty(id);
    }

    @Tool(description = "Get problem source by problem ID")
    public String getProblemSourceById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetSource(id);
    }

    @Tool(description = "Get problem tags by problem ID")
    public String getProblemTagsById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetTags(id);
    }

    @Tool(description = "Get problem category by problem ID")
    public String getProblemCategoryById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetCategory(id);
    }

    @Tool(description = "Get allowed submission languages for the problem by problem ID")
    public String getProblemOpenLanguageById(@ToolParam(description = "Problem ID") String id) {
        return dataProblemService.llmGetOpenLanguage(id);
    }
}
