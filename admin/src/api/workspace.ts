/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/workspace`

export function overview() {
  return http.get<any>(`${prefix}/overview`)
}

export function listShortcuts() {
  return http.get<any[]>(`${prefix}/shortcuts`)
}

export function saveShortcuts(resourceIds: string[]) {
  return http.post<any[]>(`${prefix}/shortcuts`, { resource_ids: resourceIds })
}
