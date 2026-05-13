import { request, uploadRequest } from '@/utils'
export function fetchFilePage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/file/page', {
    params,
  })
}
export function fetchFileDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/file/detail', { params })
}
export function fetchFileRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/file/remove', data)
}
export function fetchFileRemoveAbsolute(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/file/remove-absolute', data)
}

/** 上传文件，使用 XHR adapter 以支持上传进度 */
export function uploadFile(file: File, engine?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (engine) formData.append('engine', engine)
  return uploadRequest.Post<Service.ResponseResult>('/api/v1/sys/file/upload', formData)
}

/** 下载文件，携带认证信息请求，返回 blob */
export function fetchFileDownload(downloadPath: string): Promise<Blob> {
  const url = new URL(downloadPath)
  return request.Get(url.pathname + url.search, {
    meta: { isBlob: true },
  }) as unknown as Promise<Blob>
}
