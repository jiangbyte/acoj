import { http } from '@/utils'

export { watchSubmissionEvents, pollSubmissionUntilDone } from './watch'
export type { SubmissionWatchSnapshot } from './watch'

const prefix = '/api/v1/admin/biz/submission/submission'

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function remove(data: any) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function rejudge(data: { ids: string[] }) {
  return http.post<any>(`${prefix}/rejudge`, data)
}
