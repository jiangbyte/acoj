import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const dictApi = createCrudApi({ basePath: '/api/v1/sys/dict', hasTree: true })

export function fetchDictList(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/dict/list', { params })
}
export function fetchDictGetChildren(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/dict/get-children', { params })
}
