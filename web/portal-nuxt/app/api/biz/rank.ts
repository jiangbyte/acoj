import { http } from '~/composables/useHttp'
import type { PageData } from './problem'

const prefix = '/api/v1/portal/biz/rank'

export type PortalRatingRankItem = {
  rank: number
  account_id: string
  nickname?: string | null
  avatar?: string | null
  rating: number
}

export async function fetchRatingRank(params: { current?: number, size?: number }) {
  const { data } = await http.get<PageData<PortalRatingRankItem>>(`${prefix}/rating`, { params })
  return data
}
