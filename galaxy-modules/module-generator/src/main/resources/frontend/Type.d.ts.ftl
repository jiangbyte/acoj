/**
* ${table.comment?replace('表', '')} 类型
*/
type ${table.entityName}Type = {
<#list table.fields as field>
    ${field.propertyName}: <#if field.propertyType == 'String'>string
<#elseif ['Long','Integer','Short','Byte']?seq_contains(field.propertyType)>number
<#elseif ['Double','Float','BigDecimal']?seq_contains(field.propertyType)>number
<#elseif field.propertyType == 'Boolean'>boolean
<#elseif ['LocalDateTime','Date']?seq_contains(field.propertyType)>number | null
<#elseif field.propertyType == 'List'>Array<any>
<#elseif ['Map','Object']?seq_contains(field.propertyType)>object
<#else>any
</#if>
</#list>
}

/**
* ${table.comment?replace('表', '')} 增加参数类型
*/
type ${table.entityName}AddParamType = {
<#list table.fields as field>
<#if !["id", "deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
    ${field.propertyName}: <#if field.propertyType == 'String'>string
<#elseif ['Long','Integer','Short','Byte']?seq_contains(field.propertyType)>number
<#elseif ['Double','Float','BigDecimal']?seq_contains(field.propertyType)>number
<#elseif field.propertyType == 'Boolean'>boolean
<#elseif ['LocalDateTime','Date']?seq_contains(field.propertyType)>number | null
<#elseif field.propertyType == 'List'>Array<any>
<#elseif ['Map','Object']?seq_contains(field.propertyType)>object
<#else>any
</#if>
</#if>
</#list>
}

/**
* ${table.comment?replace('表', '')} 编辑参数类型
*/
type ${table.entityName}EditParamType = {
<#list table.fields as field>
<#if !["deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
    ${field.propertyName}: <#if field.propertyType == 'String'>string
<#elseif ['Long','Integer','Short','Byte']?seq_contains(field.propertyType)>number
<#elseif ['Double','Float','BigDecimal']?seq_contains(field.propertyType)>number
<#elseif field.propertyType == 'Boolean'>boolean
<#elseif ['LocalDateTime','Date']?seq_contains(field.propertyType)>number | null
<#elseif field.propertyType == 'List'>Array<any>
<#elseif ['Map','Object']?seq_contains(field.propertyType)>object
<#else>any
</#if>
</#if>
</#list>
}

/**
* ${table.comment?replace('表', '')} ID参数类型
*/
type ${table.entityName}IdParamType = {
<#list table.fields as field>
<#if field.keyFlag>
    ${field.propertyName}: <#if field.propertyType == 'String'>string
<#elseif ['Long','Integer','Short','Byte']?seq_contains(field.propertyType)>number
<#elseif ['Double','Float','BigDecimal']?seq_contains(field.propertyType)>number
<#elseif field.propertyType == 'Boolean'>boolean
<#elseif ['LocalDateTime','Date']?seq_contains(field.propertyType)>number | null
<#elseif field.propertyType == 'List'>Array<any>
<#elseif ['Map','Object']?seq_contains(field.propertyType)>object
<#else>any
</#if>
</#if>
</#list>
}

/**
* ${table.comment?replace('表', '')} 分页参数类型
*/
type ${table.entityName}PageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}