import type { ResponseResult } from '@hei/shared'

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

async function send<T>(method: Promise<ResponseResult<T | null>> | Promise<T>) {
  const result = await method
  if (isResponseResult<T>(result)) {
    if (!result.isSuccess || result.data === null) {
      throw new Error(result.message)
    }
    return result.data
  }
  return result
}

function isResponseResult<T>(result: ResponseResult<T | null> | T): result is ResponseResult<T | null> {
  return Boolean(result && typeof result === 'object' && 'isSuccess' in result)
}

export function listPublicBanners(query: BannerQuery) {
  return send(
    request.get<ResponseResult<SysBannerItem[] | null>>('/api/v1/portal/banner/sys/banners/list', {
      params: compactParams({ ...query }),
      meta: { authRole: 'visitor' },
    }),
  )
}

export function recordBannerInteraction(id: string) {
  return send(
    request.post<ResponseResult<null>>(
      '/api/v1/portal/banner/sys/banners/interaction',
      { id },
      { meta: { authRole: 'visitor' } },
    ),
  )
}
