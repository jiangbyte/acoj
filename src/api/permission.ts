import { request } from '@/utils'

export function fetchPermissionPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/permission/page', {
    params,
  })
}
export function fetchPermissionCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/create', data)
}
export function fetchPermissionModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/modify', data)
}
export function fetchPermissionRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/remove', data)
}
export function fetchPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/permission/detail', { params })
}
export function fetchPermissionExport(params: any) {
  return request.Get('/api/v1/sys/permission/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchPermissionTemplate() {
  return request.Get('/api/v1/sys/permission/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchPermissionImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/import', formData)
}
export function fetchPermissionModules() {
  return request.Get<Service.ResponseResult<string[]>>('/api/v1/sys/permission/modules')
}
export function fetchPermissionByModule(params: { module: string }) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/permission/by-module', { params })
}

