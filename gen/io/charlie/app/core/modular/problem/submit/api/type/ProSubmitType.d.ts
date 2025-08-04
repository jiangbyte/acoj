/**
* 提交 类型
*/
type ProSubmitType = {
    id: string
    userId: string
    problemId: string
    language: string
    code: string
    submitType: boolean
    maxTime: number
    maxMemory: number
    message: string
    testCases: string
    status: string
    similarity: number
    taskId: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 提交 增加参数类型
*/
type ProSubmitAddParamType = {
    userId: string
    problemId: string
    language: string
    code: string
    submitType: boolean
    maxTime: number
    maxMemory: number
    message: string
    testCases: string
    status: string
    similarity: number
    taskId: string
}

/**
* 提交 编辑参数类型
*/
type ProSubmitEditParamType = {
    id: string
    userId: string
    problemId: string
    language: string
    code: string
    submitType: boolean
    maxTime: number
    maxMemory: number
    message: string
    testCases: string
    status: string
    similarity: number
    taskId: string
}

/**
* 提交 ID参数类型
*/
type ProSubmitIdParamType = {
    id: string
}

/**
* 提交 分页参数类型
*/
type ProSubmitPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}