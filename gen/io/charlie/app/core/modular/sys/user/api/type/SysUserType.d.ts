/**
* 用户 类型
*/
type SysUserType = {
    id: string
    groupId: string
    username: string
    password: string
    nickname: string
    avatar: string
    background: string
    quote: string
    gender: boolean
    email: string
    telephone: string
    loginTime: number | null
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 用户 增加参数类型
*/
type SysUserAddParamType = {
    groupId: string
    username: string
    password: string
    nickname: string
    avatar: string
    background: string
    quote: string
    gender: boolean
    email: string
    telephone: string
    loginTime: number | null
}

/**
* 用户 编辑参数类型
*/
type SysUserEditParamType = {
    id: string
    groupId: string
    username: string
    password: string
    nickname: string
    avatar: string
    background: string
    quote: string
    gender: boolean
    email: string
    telephone: string
    loginTime: number | null
}

/**
* 用户 ID参数类型
*/
type SysUserIdParamType = {
    id: string
}

/**
* 用户 分页参数类型
*/
type SysUserPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}