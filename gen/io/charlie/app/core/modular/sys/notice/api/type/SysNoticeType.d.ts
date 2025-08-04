/**
* 公告 类型
*/
type SysNoticeType = {
    id: string
    title: string
    cover: string
    url: string
    sort: number
    content: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 公告 增加参数类型
*/
type SysNoticeAddParamType = {
    title: string
    cover: string
    url: string
    sort: number
    content: string
}

/**
* 公告 编辑参数类型
*/
type SysNoticeEditParamType = {
    id: string
    title: string
    cover: string
    url: string
    sort: number
    content: string
}

/**
* 公告 ID参数类型
*/
type SysNoticeIdParamType = {
    id: string
}

/**
* 公告 分页参数类型
*/
type SysNoticePageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}