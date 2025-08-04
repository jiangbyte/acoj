/**
* 代码相似度检测任务 类型
*/
type ProSimilarityTaskType = {
    id: string
    userId: string
    problemId: string
    status: string
    compareRange: string
    daysBefore: number
    totalCompared: number
    maxSimilarity: number
    isManual: boolean
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 代码相似度检测任务 增加参数类型
*/
type ProSimilarityTaskAddParamType = {
    userId: string
    problemId: string
    status: string
    compareRange: string
    daysBefore: number
    totalCompared: number
    maxSimilarity: number
    isManual: boolean
}

/**
* 代码相似度检测任务 编辑参数类型
*/
type ProSimilarityTaskEditParamType = {
    id: string
    userId: string
    problemId: string
    status: string
    compareRange: string
    daysBefore: number
    totalCompared: number
    maxSimilarity: number
    isManual: boolean
}

/**
* 代码相似度检测任务 ID参数类型
*/
type ProSimilarityTaskIdParamType = {
    id: string
}

/**
* 代码相似度检测任务 分页参数类型
*/
type ProSimilarityTaskPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}