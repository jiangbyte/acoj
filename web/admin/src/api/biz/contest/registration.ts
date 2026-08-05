import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/contest/registration'

export function page(contestId: string, params: Record<string, unknown>) {
  return http.get<any>(`${prefix}/page`, { params: { ...params, contest_id: contestId } })
}

export function add(contestId: string, data: { account_id: string; remark?: string }) {
  return http.post<string>(`${prefix}/add`, data, { params: { contest_id: contestId } })
}

export function approve(contestId: string, data: { ids: string[]; remark?: string }) {
  return http.post<null>(`${prefix}/approve`, data, { params: { contest_id: contestId } })
}

export function reject(contestId: string, data: { ids: string[]; remark?: string }) {
  return http.post<null>(`${prefix}/reject`, data, { params: { contest_id: contestId } })
}

export function cancel(contestId: string, data: { ids: string[] }) {
  return http.post<null>(`${prefix}/cancel`, data, { params: { contest_id: contestId } })
}
