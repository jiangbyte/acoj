/**
* 题集进度 类型
*/
type ProSetProgressType = {
    id: string
    userId: string
    problemSetId: string
    progress: string
    completed: boolean
    completedAt: number | null
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 题集进度 增加参数类型
*/
type ProSetProgressAddParamType = {
    userId: string
    problemSetId: string
    progress: string
    completed: boolean
    completedAt: number | null
}

/**
* 题集进度 编辑参数类型
*/
type ProSetProgressEditParamType = {
    id: string
    userId: string
    problemSetId: string
    progress: string
    completed: boolean
    completedAt: number | null
}

/**
* 题集进度 ID参数类型
*/
type ProSetProgressIdParamType = {
    id: string
}

/**
* 题集进度 分页参数类型
*/
type ProSetProgressPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}