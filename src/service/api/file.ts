import { request } from '../http'
export function fetchFilePage(params: any) { return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/file/page', { params }) }
export function fetchFileDetail(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/file/detail', { params }) }
export function fetchFileRemove(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/file/remove', data) }
export function fetchFileRemoveAbsolute(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/file/remove-absolute', data) }
export function uploadFile(file: File, engine?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (engine) formData.append('engine', engine)
  return request.Post<Service.ResponseResult>('/api/v1/sys/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
