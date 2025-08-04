/**
* 题单提交 类型
*/
type ProSetSubmitType = {
    id: string
    userId: string
    problemId: string
    setId: string
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
* 题单提交 增加参数类型
*/
type ProSetSubmitAddParamType = {
    userId: string
    problemId: string
    setId: string
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
* 题单提交 编辑参数类型
*/
type ProSetSubmitEditParamType = {
    id: string
    userId: string
    problemId: string
    setId: string
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
* 题单提交 ID参数类型
*/
type ProSetSubmitIdParamType = {
    id: string
}

/**
* 题单提交 分页参数类型
*/
type ProSetSubmitPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}