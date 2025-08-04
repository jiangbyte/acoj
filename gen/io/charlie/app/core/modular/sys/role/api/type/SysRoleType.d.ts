/**
* 角色 类型
*/
type SysRoleType = {
    id: string
    name: string
    code: string
    description: string
    level: number
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 角色 增加参数类型
*/
type SysRoleAddParamType = {
    name: string
    code: string
    description: string
    level: number
}

/**
* 角色 编辑参数类型
*/
type SysRoleEditParamType = {
    id: string
    name: string
    code: string
    description: string
    level: number
}

/**
* 角色 ID参数类型
*/
type SysRoleIdParamType = {
    id: string
}

/**
* 角色 分页参数类型
*/
type SysRolePageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}