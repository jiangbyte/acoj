/**
* 题目 类型
*/
type ProProblemType = {
    id: string
    categoryId: string
    title: string
    source: string
    url: string
    maxTime: number
    maxMemory: number
    description: string
    testCase: string
    allowedLanguages: string
    difficulty: number
    useTemplate: boolean
    codeTemplate: string
    solved: number
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 题目 增加参数类型
*/
type ProProblemAddParamType = {
    categoryId: string
    title: string
    source: string
    url: string
    maxTime: number
    maxMemory: number
    description: string
    testCase: string
    allowedLanguages: string
    difficulty: number
    useTemplate: boolean
    codeTemplate: string
    solved: number
}

/**
* 题目 编辑参数类型
*/
type ProProblemEditParamType = {
    id: string
    categoryId: string
    title: string
    source: string
    url: string
    maxTime: number
    maxMemory: number
    description: string
    testCase: string
    allowedLanguages: string
    difficulty: number
    useTemplate: boolean
    codeTemplate: string
    solved: number
}

/**
* 题目 ID参数类型
*/
type ProProblemIdParamType = {
    id: string
}

/**
* 题目 分页参数类型
*/
type ProProblemPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}