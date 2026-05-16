import { request } from '@/utils'

export function fetchNoticePage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/notice/page', {
    params,
  })
}
export function fetchNoticeCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/notice/create', data)
}
export function fetchNoticeModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/notice/modify', data)
}
export function fetchNoticeRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/notice/remove', data)
}
export function fetchNoticeDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/notice/detail', { params })
}