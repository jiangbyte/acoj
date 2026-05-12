import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const configApi = createCrudApi({ basePath: '/api/v1/sys/config' })

export function fetchConfigListByCategory(params: { category: string }) {
  return request.Get<Service.ResponseResult<any[]>>('/api/v1/sys/config/list-by-category', { params })
}
export function fetchConfigEditBatch(data: { configs: { config_key: string; config_value: string }[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/edit-batch', data)
}
export function fetchConfigEditByCategory(data: { category: string; configs: { config_key: string; config_value: string }[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/edit-by-category', data)
}
