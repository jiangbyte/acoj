/**
* 系统文章 类型
*/
type SysArticleType = {
    id: string
    title: string
    subtitle: string
    cover: string
    author: string
    summary: string
    sort: number
    toUrl: string
    parentId: string
    type: string
    category: string
    content: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 系统文章 增加参数类型
*/
type SysArticleAddParamType = {
    title: string
    subtitle: string
    cover: string
    author: string
    summary: string
    sort: number
    toUrl: string
    parentId: string
    type: string
    category: string
    content: string
}

/**
* 系统文章 编辑参数类型
*/
type SysArticleEditParamType = {
    id: string
    title: string
    subtitle: string
    cover: string
    author: string
    summary: string
    sort: number
    toUrl: string
    parentId: string
    type: string
    category: string
    content: string
}

/**
* 系统文章 ID参数类型
*/
type SysArticleIdParamType = {
    id: string
}

/**
* 系统文章 分页参数类型
*/
type SysArticlePageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}