package io.charlie.web.modular.data.library.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库
*/
@Data
@Schema(name = "DataLibrary", description = "提交样本库 ID参数")
public class DataLibraryUserPageParam implements Serializable {
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
    private String keyword; // 昵称

    @Schema(description = "用户组Id")
    private String groupId;


    private String type;

    @Schema(description = "模块类型 SET/CONTEST")
    private String moduleType;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "题目ID")
    private String problemId;

    /** @deprecated 兼容旧字段，优先用 problemId */
    private List<String> problemIds;

    /** @deprecated 兼容旧字段，优先用 moduleId */
    private String setId;

    private String language;

}