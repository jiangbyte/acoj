package io.charlie.app.core.modular.set.progress.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/08/2025
 * @description 题集进度数据分页参数
 */
@Data
public class ProSetProgressDataPageParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "题集ID")
    private String problemSetId;

    @Schema(description = "状态")
    private String status;
}
