/**
* 标签 类型
*/
type ProTagType = {
    id: string
    name: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 标签 增加参数类型
*/
type ProTagAddParamType = {
    name: string
}

/**
* 标签 编辑参数类型
*/
type ProTagEditParamType = {
    id: string
    name: string
}

/**
* 标签 ID参数类型
*/
type ProTagIdParamType = {
    id: string
}

/**
* 标签 分页参数类型
*/
type ProTagPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}