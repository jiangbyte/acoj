import { request } from '@/utils'

export function fetchBannerPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/banner/page', {
    params,
  })
}
export function fetchBannerCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/banner/create', data)
}
export function fetchBannerModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/banner/modify', data)
}
export function fetchBannerRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/banner/remove', data)
}
export function fetchBannerDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/banner/detail', { params })
}

export function fetchBannerExport(params: any) {
  return request.Get('/api/v1/sys/banner/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchBannerTemplate() {
  return request.Get('/api/v1/sys/banner/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}

export function fetchBannerImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/banner/import', formData)
}
