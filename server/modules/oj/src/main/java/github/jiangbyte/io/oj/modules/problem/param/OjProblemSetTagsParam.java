package github.jiangbyte.io.oj.modules.problem.param;

/**
 * 设置题目标签入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "设置题目标签入参")
@Data
public class OjProblemSetTagsParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "题目ID")
    private String problemId;

    @NotNull
    @Schema(description = "标签ID列表")
    private List<String> tagIds;
}
