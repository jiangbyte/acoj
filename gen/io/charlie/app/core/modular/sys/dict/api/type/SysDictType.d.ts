/**
* 系统字典 类型
*/
type SysDictType = {
    id: string
    dictType: string
    typeLabel: string
    dictValue: string
    dictLabel: string
    sortOrder: number
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 系统字典 增加参数类型
*/
type SysDictAddParamType = {
    dictType: string
    typeLabel: string
    dictValue: string
    dictLabel: string
    sortOrder: number
}

/**
* 系统字典 编辑参数类型
*/
type SysDictEditParamType = {
    id: string
    dictType: string
    typeLabel: string
    dictValue: string
    dictLabel: string
    sortOrder: number
}

/**
* 系统字典 ID参数类型
*/
type SysDictIdParamType = {
    id: string
}

/**
* 系统字典 分页参数类型
*/
type SysDictPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}