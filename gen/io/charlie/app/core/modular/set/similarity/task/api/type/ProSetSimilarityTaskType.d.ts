/**
* 题单代码相似度检测任务 类型
*/
type ProSetSimilarityTaskType = {
    id: string
    userId: string
    problemId: string
    setId: string
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
* 题单代码相似度检测任务 增加参数类型
*/
type ProSetSimilarityTaskAddParamType = {
    userId: string
    problemId: string
    setId: string
    status: string
    compareRange: string
    daysBefore: number
    totalCompared: number
    maxSimilarity: number
    isManual: boolean
}

/**
* 题单代码相似度检测任务 编辑参数类型
*/
type ProSetSimilarityTaskEditParamType = {
    id: string
    userId: string
    problemId: string
    setId: string
    status: string
    compareRange: string
    daysBefore: number
    totalCompared: number
    maxSimilarity: number
    isManual: boolean
}

/**
* 题单代码相似度检测任务 ID参数类型
*/
type ProSetSimilarityTaskIdParamType = {
    id: string
}

/**
* 题单代码相似度检测任务 分页参数类型
*/
type ProSetSimilarityTaskPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}