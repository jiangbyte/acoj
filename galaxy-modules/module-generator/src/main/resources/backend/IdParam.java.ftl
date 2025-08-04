package ${package.IdParam};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment?replace('表', '')}
*/
@Data
@Schema(name = "${entity}", description = "${table.comment?replace('表', '')} ID参数")
public class ${entity}IdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

<#list table.fields as field>
<#if field.keyFlag>
    @NotBlank(message = "${field.propertyName}不能为空")
    private ${field.propertyType} ${field.propertyName};

</#if>
</#list>
}