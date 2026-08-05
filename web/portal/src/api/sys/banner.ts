import { http } from '@/utils'

const bannerPrefix = '/api/v1/portal/sys/banners'

export type BannerLinkType = 'URL' | 'ROUTE' | 'NONE'

export interface PortalBanner {
  id: string
  title: string
  image: string
  url: string | null
  link_type: BannerLinkType
  summary: string | null
  description: string | null
  category: string
  type: string
  position: string
  display_scope: string
  sort: number
  interaction_count: number
  status: string
  start_at: string | null
  end_at: string | null
}

export type BannerListQuery = {
  position: string
  category?: string
  type?: string
}

/** 门户公开展示图列表（不携带 token） */
export function listBanners(params: BannerListQuery) {
  return http.get<PortalBanner[]>(`${bannerPrefix}/list`, {
    params,
    addToken: false,
  })
}

/** 记录展示图点击交互 */
export function recordBannerInteraction(id: string) {
  return http.post<null>(
    `${bannerPrefix}/interaction`,
    { id },
    { addToken: false },
  )
}
