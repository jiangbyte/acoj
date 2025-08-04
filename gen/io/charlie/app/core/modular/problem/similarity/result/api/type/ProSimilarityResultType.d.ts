/**
* 代码相似度检测结果详情 类型
*/
type ProSimilarityResultType = {
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
* 代码相似度检测结果详情 增加参数类型
*/
type ProSimilarityResultAddParamType = {
    taskId: string
    originSubmitId: string
    comparedSubmitId: string
    similarity: number
    details: string
    matchDetails: string
    threshold: number
}

/**
* 代码相似度检测结果详情 编辑参数类型
*/
type ProSimilarityResultEditParamType = {
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
* 代码相似度检测结果详情 ID参数类型
*/
type ProSimilarityResultIdParamType = {
    id: string
}

/**
* 代码相似度检测结果详情 分页参数类型
*/
type ProSimilarityResultPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}