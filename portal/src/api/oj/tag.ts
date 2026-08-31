/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj/tags`

export type OjTagOption = {
  id: string
  name: string
  problem_count?: number
}

export type OjPortalTagOptions = {
  tags: OjTagOption[]
  published_count?: number
  accepted_count?: number | null
}

/** 题库筛选项：标签计数 + 全局统计（匿名可读）。 */
export function options() {
  return http.get<OjPortalTagOptions>(`${prefix}/options`, { public: true })
}
