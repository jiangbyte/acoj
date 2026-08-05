import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/team'

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function createCourseTeam(data: any) {
  return http.post<any>(`${prefix}/course/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function disable(params: { id: string }) {
  return http.post<any>(`${prefix}/disable`, {}, { params })
}

export function dissolve(params: { id: string }) {
  return http.post<any>(`${prefix}/dissolve`, {}, { params })
}

export function members(params: { team_id: string }) {
  return http.get<any>(`${prefix}/members`, { params })
}

export function memberAdd(data: { team_id: string, account_ids: string[] }) {
  return http.post<any>(`${prefix}/member/add`, data)
}

export function memberRemove(data: { team_id: string, account_id: string }) {
  return http.post<any>(`${prefix}/member/remove`, data)
}
