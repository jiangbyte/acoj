package ${package.PageParam};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment?replace('表', '')} 分页参数
*/
@Data
@Schema(name = "${entity}", description = "${table.comment?replace('表', '')} 分页参数")
public class ${entity}PageParam implements Serializable {
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
}