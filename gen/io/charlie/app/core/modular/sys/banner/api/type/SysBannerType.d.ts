/**
* 横幅 类型
*/
type SysBannerType = {
    id: string
    title: string
    banner: string
    buttonText: string
    toUrl: string
    sort: number
    subtitle: string
    createTime: number | null
    createUser: string
    updateTime: number | null
    updateUser: string
}

/**
* 横幅 增加参数类型
*/
type SysBannerAddParamType = {
    title: string
    banner: string
    buttonText: string
    toUrl: string
    sort: number
    subtitle: string
}

/**
* 横幅 编辑参数类型
*/
type SysBannerEditParamType = {
    id: string
    title: string
    banner: string
    buttonText: string
    toUrl: string
    sort: number
    subtitle: string
}

/**
* 横幅 ID参数类型
*/
type SysBannerIdParamType = {
    id: string
}

/**
* 横幅 分页参数类型
*/
type SysBannerPageParamType = {
    current: number
    size: number
    sortField: string
    sortOrder: string
    keyword: string
}