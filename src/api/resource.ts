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

export function fetchResourceTree() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/resource/tree')
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
export function fetchModuleExport(params: any) {
  return request.Get('/api/v1/sys/module/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchModuleTemplate() {
  return request.Get('/api/v1/sys/module/template', { meta: { isBlob: true } }) as Promise<Blob>
}
export function fetchModuleImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/module/import', formData)
}
export function fetchResourceExport(params: any) {
  return request.Get('/api/v1/sys/resource/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchResourceTemplate() {
  return request.Get('/api/v1/sys/resource/template', { meta: { isBlob: true } }) as Promise<Blob>
}
export function fetchResourceImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/resource/import', formData)
}

/** 获取资源已绑定的权限ID列表 */
export function fetchResourceOwnPermissions(params: { resource_id: string }) {
  return request.Get<Service.ResponseResult<string[]>>('/api/v1/sys/resource/own-permissions', { params })
}

/** 绑定权限到资源（覆盖式替换） */
export function fetchResourceBindPermissions(data: { resource_id: string; permission_ids: string[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/resource/bind-permissions', data)
}
