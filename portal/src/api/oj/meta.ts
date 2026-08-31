/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

const prefix = `${API_PREFIX}/oj`

/** 聚合 ENABLED 执行机支持的语言（去重）。 */
export function languages() {
  return http.get<{ languages: string[]; node_count: number }>(`${prefix}/languages`, { public: true })
}
