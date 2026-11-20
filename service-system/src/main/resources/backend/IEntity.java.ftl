package ${package.Entity};

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
<#list table.importPackages as pkg>
import ${pkg};
</#list>
import java.io.Serial;
import java.util.Date;
<#if springdoc>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
<#if entityLombokModel>
import lombok.Data;
import lombok.EqualsAndHashCode;
</#if>

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment!}
*/
<#if entityLombokModel>
    <#if superEntityClass??>
@EqualsAndHashCode(callSuper = true)
    </#if>
@Data
</#if>
<#if table.convert>
@TableName("${schemaName}${table.name}")
</#if>
<#if springdoc>
@Schema(name = "${entity}", description = "${table.comment!}")
</#if>
<#if superEntityClass??>
public class ${entity} extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
</#if>
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if !["deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    @Schema(description = "${field.comment}")
    <#if field.keyFlag>
    <#-- 主键 -->
        <#if field.keyIdentityFlag>
    @TableId(value = "${field.annotationColumnName}", type = IdType.${idType})
        </#if>
    </#if>
    <#-- 普通字段 -->
    private ${field.propertyType} ${field.propertyName};
    </#if>
</#list>
<#------------  END 字段循环遍历  ---------->
}
