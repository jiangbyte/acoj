/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj/submissions`

export function create(data: { problem_id: string; language: string; source_code: string }) {
  return http.post<any>(`${prefix}/create`, data)
}

export function detail(params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function page(params?: Record<string, unknown>) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function updateNote(data: { id: string; note?: string | null }) {
  return http.post<any>(`${prefix}/update-note`, data)
}
