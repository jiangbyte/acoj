import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/contest/clarification'

export function page(contestId: string, params: any = {}) {
  return http.get<any>(`${prefix}/page`, { params: { ...params, contest_id: contestId } })
}

export function detail(contestId: string, params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params: { ...params, contest_id: contestId } })
}

export function create(contestId: string, data: any) {
  return http.post<any>(`${prefix}/create`, { ...data, contest_id: contestId })
}

export function update(contestId: string, data: any) {
  return http.post<any>(`${prefix}/update`, { ...data, contest_id: contestId })
}

export function remove(contestId: string, data: any) {
  return http.post<any>(`${prefix}/delete`, { ...data, contest_id: contestId })
}

export function threadPage(contestId: string, params: any = {}) {
  return http.get<any>(`${prefix}/thread/page`, { params: { ...params, contest_id: contestId } })
}

export function threadDetail(contestId: string, params: { id: string }) {
  return http.get<any>(`${prefix}/thread/detail`, { params: { ...params, contest_id: contestId } })
}

export function threadReply(contestId: string, data: { thread_id: string, body: string, set_answered?: boolean }) {
  return http.post<any>(`${prefix}/thread/reply`, { ...data, contest_id: contestId })
}

export function threadStatus(contestId: string, data: { thread_id: string, status: string }) {
  return http.post<any>(`${prefix}/thread/status`, { ...data, contest_id: contestId })
}

export function threadPromote(contestId: string, data: { thread_id: string, title?: string, body?: string }) {
  return http.post<any>(`${prefix}/thread/promote`, { ...data, contest_id: contestId })
}

export function threadRemove(contestId: string, data: any) {
  return http.post<any>(`${prefix}/thread/delete`, { ...data, contest_id: contestId })
}
