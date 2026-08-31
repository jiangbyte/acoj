/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj/users`

export type OjDifficultyBucket = {
  solved?: number
  total?: number
}

export type OjSolveProgress = {
  accepted?: number
  attempting?: number
  easy?: OjDifficultyBucket
  medium?: OjDifficultyBucket
  hard?: OjDifficultyBucket
}

export type OjLanguageStat = {
  language: string
  solved_count?: number
}

export type OjHeatmapDay = {
  date: string
  count?: number
}

export type OjHeatmapStat = {
  total_submissions?: number
  active_days?: number
  max_streak?: number
  current_streak?: number
  days?: OjHeatmapDay[]
}

export type OjRecentAccepted = {
  problem_id: string
  problem_key?: string
  title?: string
  difficulty?: string
  accepted_at?: string
}

export type OjUserHomepage = {
  account_id: string
  solved?: OjSolveProgress
  languages?: OjLanguageStat[]
  heatmap?: OjHeatmapStat
  recent_accepted?: OjRecentAccepted[]
}

/** 用户主页公开 OJ 统计（匿名可读）。 */
export function homepage(accountId: string) {
  return http.get<OjUserHomepage>(`${prefix}/homepage`, {
    public: true,
    params: { account_id: accountId },
  })
}
