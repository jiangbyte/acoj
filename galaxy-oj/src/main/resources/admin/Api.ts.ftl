import { request as $alova } from '@/utils'

/**
* ${table.comment?replace('表', '')} API请求
*/
export function use${entity}Fetch() {
    const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
    const pathPrefix = context + '/api/v1/'
  const table = '${table.name?replace('_', '/')}'

    /*
     * ${table.comment?replace('表', '')} 默认数据
     */
    const ${table.entityPath}DefaultData = {
    <#-- ----------  BEGIN 字段循环遍历  ---------->
    <#list table.fields as field>
    ${field.propertyName}: <#if field.propertyType == 'String'>'',<#-- 字符串 -->
    <#elseif field.propertyType == 'Long' || field.propertyType == 'Integer' || field.propertyType == 'Short' || field.propertyType == 'Byte'>0,<#-- 所有整数类型 -->
    <#elseif field.propertyType == 'Double' || field.propertyType == 'Float' || field.propertyType == 'BigDecimal'>0.0,<#-- 浮点类型 -->
    <#elseif field.propertyType == 'Boolean'>false,<#-- 布尔值 -->
    <#elseif field.propertyType == 'LocalDateTime' || field.propertyType == 'LocalDate' || field.propertyType == 'Date'>'',<#-- 日期类型初始化为空字符串 -->
    <#elseif field.propertyType == 'List' || field.propertyType == 'Set'>[],<#-- 集合类型 -->
    <#elseif field.propertyType == 'Map' || field.propertyType == 'Object'>{},<#-- 对象类型 -->
    <#else>null,<#-- 其他类型 -->
    </#if>
    </#list>
    <#------------  END 字段循环遍历  ---------->
}
 return {
    /*
    * ${table.comment?replace('表', '')} 默认数据
    */
    ${table.entityPath}DefaultData,

    /*
     * ${table.comment?replace('表', '')} 分页接口
     */
    ${table.entityPath}Page(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * ${table.comment?replace('表', '')} 新增接口
     */
    ${table.entityPath}Add(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * ${table.comment?replace('表', '')} 修改接口
     */
    ${table.entityPath}Edit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * ${table.comment?replace('表', '')} 删除接口
     */
    ${table.entityPath}Delete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * ${table.comment?replace('表', '')} 详情接口
     */
    ${table.entityPath}Detail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}