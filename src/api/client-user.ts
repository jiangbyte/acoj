import { request } from '@/utils'

export function fetchClientUserPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/client-user/page', {
    params,
  })
}

export function fetchClientUserCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/client-user/create', data)
}

export function fetchClientUserModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/client-user/modify', data)
}

export function fetchClientUserRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/client-user/remove', data)
}

export function fetchClientUserDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/client-user/detail', { params })
}

export function fetchClientUserExport(params: any) {
  return request.Get('/api/v1/client-user/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchClientUserTemplate() {
  return request.Get('/api/v1/client-user/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchClientUserImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/client-user/import', formData)
}
