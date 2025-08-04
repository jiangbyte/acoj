/**
* 用户题集解决记录 类型
*/
type ProSetSolvedType = {
    id: string
    userId: string
    problemId: string
    problemSetId: string
    submitId: string
    solved: boolean
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 用户题集解决记录 增加参数类型
*/
type ProSetSolvedAddParamType = {
    userId: string
    problemId: string
    problemSetId: string
    submitId: string
    solved: boolean
}

/**
* 用户题集解决记录 编辑参数类型
*/
type ProSetSolvedEditParamType = {
    id: string
    userId: string
    problemId: string
    problemSetId: string
    submitId: string
    solved: boolean
}

/**
* 用户题集解决记录 ID参数类型
*/
type ProSetSolvedIdParamType = {
    id: string
}

/**
* 用户题集解决记录 分页参数类型
*/
type ProSetSolvedPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}