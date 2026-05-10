import { request } from '@/utils'

export function fetchUserPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/user/page', {
    params,
  })
}
export function fetchUserCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/create', data)
}
export function fetchUserModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/modify', data)
}
export function fetchUserRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/remove', data)
}
export function fetchUserDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/detail', { params })
}

/** 导出用户（返回 blob） */
export function fetchUserExport(params: {
  export_type: 'current' | 'selected' | 'all'
  current?: number
  size?: number
  selected_id?: string
}) {
  return request.Get('/api/v1/sys/user/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}

/** 下载导入模板（返回 blob） */
export function fetchUserTemplate() {
  return request.Get('/api/v1/sys/user/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}

/** 导入用户 */
export function fetchUserImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/import', formData)
}

export function fetchUserGrantRole(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-role', data)
}
export function fetchUserOwnRoles(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-roles', { params })
}
