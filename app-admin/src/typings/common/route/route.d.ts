declare namespace SiteRoute {

  interface RouteMeta {
    /* ==================================== */
    title?: string
    sort?: string
    icon?: string
    keepAlive?: boolean
    extraLink?: string
    activeMenu?: boolean
    withoutTab?: string
    menuType?: string
    parameter?: boolean
    pined?: boolean
    visible?: boolean
    parameters?: { [key: string]: string }
  }

  type MetaKeys = keyof RouteMeta

  interface BaseRoute {
    id: string
    pid: string
    name: string
    path: string
    redirect?: string
    componentPath?: string | null
  }

  type RowRoute = RouteMeta & BaseRoute

  interface Route extends BaseRoute {
    children?: Route[]
    component: any
    meta: RouteMeta
  }

}
