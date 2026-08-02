import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/contest/rating'

export function rate(contestId: string) {
  return http.post<any>(`${prefix}/rate`, {}, { params: { contest_id: contestId } })
}

export function undo(contestId: string) {
  return http.post<any>(`${prefix}/undo`, {}, { params: { contest_id: contestId } })
}

export function list(contestId: string) {
  return http.get<any>(`${prefix}/list`, { params: { contest_id: contestId } })
}
