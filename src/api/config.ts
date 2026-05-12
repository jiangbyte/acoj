import { request } from '@/utils'

export function fetchConfigPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/config/page', { params })
}
export function fetchConfigListByCategory(params: { category: string }) {
  return request.Get<Service.ResponseResult<any[]>>('/api/v1/sys/config/list-by-category', { params })
}
export function fetchConfigCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/create', data)
}
export function fetchConfigModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/modify', data)
}
export function fetchConfigRemove(data: { ids: string[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/remove', data)
}
export function fetchConfigDetail(params: { id: string }) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/config/detail', { params })
}
export function fetchConfigEditBatch(data: { configs: { config_key: string; config_value: string }[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/edit-batch', data)
}
export function fetchConfigEditByCategory(data: {
  category: string
  configs: { config_key: string; config_value: string }[]
}) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/edit-by-category', data)
}
