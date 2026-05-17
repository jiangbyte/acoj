import { request } from '@/utils'

export function fetchLogPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/log/page', {
    params,
  })
}

export function fetchLogDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/log/detail', { params })
}

export function fetchLogRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/log/remove', data)
}

export function fetchLogDeleteByCategory(data: { category: string }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/log/delete-by-category', data)
}

// ---- Chart / Statistics ----

export interface LogBarChartData {
  days: string[]
  series: { name: string; data: number[] }[]
}

export interface LogPieChartData {
  data: { category: string; total: number }[]
}

/** Login/Logout daily trend (last 7 days) */
export function fetchVisLineChartData() {
  return request.Get<Service.ResponseResult<LogBarChartData>>('/api/v1/sys/log/vis/line-chart-data')
}

/** Login vs Logout total proportion */
export function fetchVisPieChartData() {
  return request.Get<Service.ResponseResult<LogPieChartData>>('/api/v1/sys/log/vis/pie-chart-data')
}

/** Operation/Exception daily trend (last 7 days) */
export function fetchOpBarChartData() {
  return request.Get<Service.ResponseResult<LogBarChartData>>('/api/v1/sys/log/op/bar-chart-data')
}

/** Operation vs Exception total proportion */
export function fetchOpPieChartData() {
  return request.Get<Service.ResponseResult<LogPieChartData>>('/api/v1/sys/log/op/pie-chart-data')
}

