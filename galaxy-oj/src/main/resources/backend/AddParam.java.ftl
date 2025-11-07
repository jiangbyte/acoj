package ${package.AddParam};

import com.baomidou.mybatisplus.annotation.TableField;
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
* @description ${table.comment?replace('表', '')} 增加参数
*/
@Data
@Schema(name = "${entity}", description = "${table.comment?replace('表', '')} 增加参数")
public class ${entity}AddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#-- 跳过 CommonEntity 常见字段 -->
    <#if !["id", "deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
        <#if field.comment!?length gt 0>
    @Schema(description = "${field.comment}")
        </#if>
    private ${field.propertyType} ${field.propertyName};

    </#if>
</#list>
<#------------  END 字段循环遍历  ---------->
}