/**
* 系统配置 类型
*/
type SysConfigType = {
    id: string
    name: string
    code: string
    value: string
    componentType: string
    description: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 系统配置 增加参数类型
*/
type SysConfigAddParamType = {
    name: string
    code: string
    value: string
    componentType: string
    description: string
}

/**
* 系统配置 编辑参数类型
*/
type SysConfigEditParamType = {
    id: string
    name: string
    code: string
    value: string
    componentType: string
    description: string
}

/**
* 系统配置 ID参数类型
*/
type SysConfigIdParamType = {
    id: string
}

/**
* 系统配置 分页参数类型
*/
type SysConfigPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}