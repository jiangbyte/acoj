/**
* 用户组 类型
*/
type SysGroupType = {
    id: string
    parentId: string
    name: string
    code: string
    description: string
    sort: number
    adminId: string
    groupType: boolean
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 用户组 增加参数类型
*/
type SysGroupAddParamType = {
    parentId: string
    name: string
    code: string
    description: string
    sort: number
    adminId: string
    groupType: boolean
}

/**
* 用户组 编辑参数类型
*/
type SysGroupEditParamType = {
    id: string
    parentId: string
    name: string
    code: string
    description: string
    sort: number
    adminId: string
    groupType: boolean
}

/**
* 用户组 ID参数类型
*/
type SysGroupIdParamType = {
    id: string
}

/**
* 用户组 分页参数类型
*/
type SysGroupPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}