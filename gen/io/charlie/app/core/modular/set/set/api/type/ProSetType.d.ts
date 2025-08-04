/**
* 题集 类型
*/
type ProSetType = {
    id: string
    setType: number
    title: string
    cover: string
    description: string
    categoryId: string
    difficulty: number
    startTime: number | null
    endTime: number | null
    config: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 题集 增加参数类型
*/
type ProSetAddParamType = {
    setType: number
    title: string
    cover: string
    description: string
    categoryId: string
    difficulty: number
    startTime: number | null
    endTime: number | null
    config: string
}

/**
* 题集 编辑参数类型
*/
type ProSetEditParamType = {
    id: string
    setType: number
    title: string
    cover: string
    description: string
    categoryId: string
    difficulty: number
    startTime: number | null
    endTime: number | null
    config: string
}

/**
* 题集 ID参数类型
*/
type ProSetIdParamType = {
    id: string
}

/**
* 题集 分页参数类型
*/
type ProSetPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}