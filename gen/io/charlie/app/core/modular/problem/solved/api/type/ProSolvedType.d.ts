/**
* 用户解决 类型
*/
type ProSolvedType = {
    id: string
    userId: string
    problemId: string
    submitId: string
    solved: boolean
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 用户解决 增加参数类型
*/
type ProSolvedAddParamType = {
    userId: string
    problemId: string
    submitId: string
    solved: boolean
}

/**
* 用户解决 编辑参数类型
*/
type ProSolvedEditParamType = {
    id: string
    userId: string
    problemId: string
    submitId: string
    solved: boolean
}

/**
* 用户解决 ID参数类型
*/
type ProSolvedIdParamType = {
    id: string
}

/**
* 用户解决 分页参数类型
*/
type ProSolvedPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}