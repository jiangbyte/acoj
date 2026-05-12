import { request } from '@/utils'

export function fetchRolePage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/role/page', {
    params,
  })
}
export function fetchRoleCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/create', data)
}
export function fetchRoleModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/modify', data)
}
export function fetchRoleRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/remove', data)
}
export function fetchRoleDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/detail', { params })
}

/** 导出角色（返回 blob） */
export function fetchRoleExport(params: {
  export_type: 'current' | 'selected' | 'all'
  current?: number
  size?: number
  selected_id?: string
}) {
  return request.Get('/api/v1/sys/role/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}

/** 下载导入模板（返回 blob） */
export function fetchRoleTemplate() {
  return request.Get('/api/v1/sys/role/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}

/** 导入角色 */
export function fetchRoleImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/import', formData)
}

export function fetchRoleGrantPermission(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-permission', data)
}
export function fetchRoleOwnPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-permission-detail', { params })
}
export function fetchRoleGrantResource(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-resource', data)
}
export function fetchRoleOwnPermission(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-permission', { params })
}
export function fetchRoleOwnResource(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-resource', { params })
}
