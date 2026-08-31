/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj/problems`

export function page(params?: Record<string, unknown>) {
  return http.get<any>(`${prefix}/page`, { params, public: true })
}

export function detail(params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params, public: true })
}

export function run(data: {
  problem_id: string
  language: string
  source_code: string
  cases?: Array<{ input?: string; output?: string | null }>
}) {
  return http.post<any>(`${prefix}/run`, data, { timeout: 120_000 })
}
