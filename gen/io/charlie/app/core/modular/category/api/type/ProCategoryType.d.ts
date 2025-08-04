/**
* 分类 类型
*/
type ProCategoryType = {
    id: string
    name: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 分类 增加参数类型
*/
type ProCategoryAddParamType = {
    name: string
}

/**
* 分类 编辑参数类型
*/
type ProCategoryEditParamType = {
    id: string
    name: string
}

/**
* 分类 ID参数类型
*/
type ProCategoryIdParamType = {
    id: string
}

/**
* 分类 分页参数类型
*/
type ProCategoryPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}