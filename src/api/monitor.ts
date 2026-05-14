import { request } from '@/utils'

export function fetchSessionAnalysis() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/session/analysis')
}

export function fetchSessionPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/session/page', { params })
}

export function fetchSessionExit(userId: string) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/session/exit', { user_id: userId })
}

export function fetchClientSessionAnalysis() {
  return request.Get<Service.ResponseResult>('/api/v1/client/session/analysis')
}

export function fetchClientSessionPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/client/session/page', { params })
}

export function fetchClientSessionExit(userId: string) {
  return request.Post<Service.ResponseResult>('/api/v1/client/session/exit', { user_id: userId })
}

export function fetchSessionChartData() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/session/chart-data')
}
