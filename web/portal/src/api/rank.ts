import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export type RankBoard = 'solved' | 'rating'

export interface SolvedRankItem {
  rank: number
  account_id: string
  nickname: string | null
  avatar: string | null
  solved: number
}

export interface RatingRankItem {
  rank: number
  account_id: string
  nickname: string | null
  avatar: string | null
  rating: number
  contests: number
  delta: number
}

export interface RankMe {
  board: RankBoard
  rank: number | null
  score: number
  nickname: string | null
  avatar: string | null
  contests: number
  delta: number
}

export interface RankSummary {
  board: RankBoard
  total_users: number
  top_score: number
  avg_score: number
  max_delta: number
}

export const rankApi = {
  solved: (params?: { current?: number; size?: number }) =>
    http.get<PageData<SolvedRankItem>>(`${prefix}/biz/rank/solved`, {
      params,
      addToken: false,
    }),
  rating: (params?: { current?: number; size?: number }) =>
    http.get<PageData<RatingRankItem>>(`${prefix}/biz/rank/rating`, {
      params,
      addToken: false,
    }),
  me: (board: RankBoard) =>
    http.get<RankMe>(`${prefix}/biz/rank/me`, { params: { board } }),
  summary: (board: RankBoard) =>
    http.get<RankSummary>(`${prefix}/biz/rank/summary`, {
      params: { board },
      addToken: false,
    }),
}
