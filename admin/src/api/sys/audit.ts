/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const auditPrefix = `${API_PREFIX}/sys/audit`

export function page(params: any) {
  return http.get<any>(`${auditPrefix}/page`, {
    params,
  })
}

export function detail(params: any) {
  return http.get<any>(`${auditPrefix}/detail`, {
    params,
  })
}

/** 当前登录用户本人审计分页（强制本人，无需审计管理权限）。 */
export function myPage(params?: {
  current?: number
  size?: number
  module?: string
  action?: string
  exclude_action?: string
  success?: boolean
}) {
  return http.get<any>(`${auditPrefix}/my-page`, { params })
}

export function myDetail(id: string) {
  return http.get<any>(`${auditPrefix}/my-detail`, { params: { id } })
}
