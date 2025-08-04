/**
* 题单代码相似度检测结果详情 类型
*/
type ProSetSimilarityResultType = {
    id: string
    taskId: string
    originSubmitId: string
    comparedSubmitId: string
    similarity: number
    details: string
    matchDetails: string
    threshold: number
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 题单代码相似度检测结果详情 增加参数类型
*/
type ProSetSimilarityResultAddParamType = {
    taskId: string
    originSubmitId: string
    comparedSubmitId: string
    similarity: number
    details: string
    matchDetails: string
    threshold: number
}

/**
* 题单代码相似度检测结果详情 编辑参数类型
*/
type ProSetSimilarityResultEditParamType = {
    id: string
    taskId: string
    originSubmitId: string
    comparedSubmitId: string
    similarity: number
    details: string
    matchDetails: string
    threshold: number
}

/**
* 题单代码相似度检测结果详情 ID参数类型
*/
type ProSetSimilarityResultIdParamType = {
    id: string
}

/**
* 题单代码相似度检测结果详情 分页参数类型
*/
type ProSetSimilarityResultPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}