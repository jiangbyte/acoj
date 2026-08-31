package github.jiangbyte.io.oj.modules.tag.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门户题库标签选项（含已发布题数）。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户题库标签选项")
@Data
public class OjTagOptionItem {
    @Schema(description = "标签 ID")
    private String id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "已发布题目数")
    private Long problemCount;
}
