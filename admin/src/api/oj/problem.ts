/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj/problems`

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${prefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function replaceCases(data: any) {
  return http.post<any>(`${prefix}/replace-cases`, data)
}

export function setTags(data: any) {
  return http.post<any>(`${prefix}/set-tags`, data)
}

export function dryRun(data: any) {
  return http.post<any>(`${prefix}/dry-run`, data)
}

export function dryRunsPage(params: any) {
  return http.get<any>(`${prefix}/dry-runs/page`, { params })
}

export function dryRunsDetail(params: any) {
  return http.get<any>(`${prefix}/dry-runs/detail`, { params })
}

export function applyLimits(data: any) {
  return http.post<any>(`${prefix}/apply-limits`, data)
}
