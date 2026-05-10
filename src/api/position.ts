import { request } from '@/utils'

export function fetchPositionPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/position/page', {
    params,
  })
}

export function fetchPositionCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/position/create', data)
}

export function fetchPositionModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/position/modify', data)
}

export function fetchPositionRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/position/remove', data)
}

export function fetchPositionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/position/detail', { params })
}

export function fetchPositionExport(params: any) {
  return request.Get('/api/v1/sys/position/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchPositionTemplate() {
  return request.Get('/api/v1/sys/position/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchPositionImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/position/import', formData)
}
