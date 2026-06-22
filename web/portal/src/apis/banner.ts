import type { SysBannerItem } from '@/types/api'
import { request } from '@/utils/http'

export interface BannerQuery {
  position: SysBannerItem['position']
  category?: SysBannerItem['category']
  type?: SysBannerItem['type']
}

function compactParams(params: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export function listPublicBanners(query: BannerQuery) {
  return request.get<SysBannerItem[]>('/api/v1/portal/banner/sys/banners/list', {
    params: compactParams({ ...query }),
    meta: { authRole: 'visitor' },
  })
}

export function recordBannerInteraction(id: string) {
  return request.post<null>('/api/v1/portal/banner/sys/banners/interaction', { id }, { meta: { authRole: 'visitor' } })
}
