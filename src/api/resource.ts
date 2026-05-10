import { request } from '@/utils'

export function fetchModulePage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/module/page', {
    params,
  })
}
export function fetchModuleCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/module/create', data)
}
export function fetchModuleModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/module/modify', data)
}
export function fetchModuleRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/module/remove', data)
}
export function fetchModuleDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/module/detail', { params })
}

export function fetchResourcePage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/resource/page', {
    params,
  })
}
export function fetchResourceCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/resource/create', data)
}
export function fetchResourceModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/resource/modify', data)
}
export function fetchResourceRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/resource/remove', data)
}
export function fetchResourceDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/resource/detail', { params })
}
