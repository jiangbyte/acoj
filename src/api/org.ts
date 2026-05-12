import { request } from '@/utils'

export function fetchOrgPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/org/page', {
    params,
  })
}
export function fetchOrgTree(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/org/tree', { params })
}
export function fetchOrgCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/org/create', data)
}
export function fetchOrgModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/org/modify', data)
}
export function fetchOrgRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/org/remove', data)
}
export function fetchOrgDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/org/detail', { params })
}
export function fetchOrgExport(params: any) {
  return request.Get('/api/v1/sys/org/export', { params, meta: { isBlob: true } }) as Promise<Blob>
}
export function fetchOrgTemplate() {
  return request.Get('/api/v1/sys/org/template', { meta: { isBlob: true } }) as Promise<Blob>
}
export function fetchOrgImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/org/import', formData)
}
