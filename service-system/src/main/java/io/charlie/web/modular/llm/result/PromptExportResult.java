package io.charlie.web.modular.llm.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 当前生效的系统提示导出。
 */
@Data
@Schema(name = "PromptExportResult", description = "Prompt 导出结果")
public class PromptExportResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "默认系统提示全文")
    private String defaultPrompt;

    @Schema(description = "默认系统提示摘要")
    private String defaultPromptSha256;

    @Schema(description = "额外模板：名称 → 原文")
    private Map<String, String> templates = new LinkedHashMap<>();

    @Schema(description = "额外模板摘要")
    private Map<String, String> templateSha256 = new LinkedHashMap<>();
}
