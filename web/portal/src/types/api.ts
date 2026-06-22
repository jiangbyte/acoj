export interface SysBannerItem {
  id: string
  title: string
  image: string
  url?: string | null
  link_type: 'URL' | 'ROUTE' | 'NONE'
  summary?: string | null
  description?: string | null
  category: 'HOME' | 'LOGIN' | 'WORKPLACE' | 'NOTICE' | 'ADMIN_DASHBOARD' | 'SYSTEM_UPGRADE'
  type: 'CAROUSEL' | 'HERO' | 'NOTICE' | 'CARD' | 'POPUP' | 'SIDEBAR'
  position:
    | 'HOME_TOP'
    | 'HOME_MIDDLE'
    | 'HOME_BOTTOM'
    | 'LOGIN_SIDE'
    | 'WORKPLACE_TOP'
    | 'NOTICE_AREA'
    | 'ADMIN_TOP'
    | 'ADMIN_SIDEBAR'
  display_scope: 'PORTAL' | 'ADMIN' | 'APP'
  sort: number
  interaction_count: number
  status: string
  start_at?: string | null
  end_at?: string | null
  created_at: string
  created_by?: string | null
  updated_at: string
  updated_by?: string | null
}
