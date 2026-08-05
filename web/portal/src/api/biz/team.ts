import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function teamPage(params?: any) {
  return http.get<any>(`${prefix}/biz/team/page`, {
    params,
  })
}

export function teamMy() {
  return http.get<any>(`${prefix}/biz/team/my`)
}

export function teamCreate(data: any) {
  return http.post<any>(`${prefix}/biz/team/create`, data)
}

export function teamJoin(data: any) {
  return http.post<any>(`${prefix}/biz/team/join`, {
    invite_code: data.invite_code.trim().toUpperCase(),
  })
}

export function teamLeave(id: string) {
  return http.post<any>(`${prefix}/biz/team/leave`, null, { params: { id } })
}

export function teamDissolve(id: string) {
  return http.post<any>(`${prefix}/biz/team/dissolve`, null, { params: { id } })
}

export function teamDetail(id: string) {
  return http.get<any>(`${prefix}/biz/team/detail`, { params: { id } })
}

export function teamMembers(teamId: string) {
  return http.get<any>(`${prefix}/biz/team/members`, {
    params: { team_id: teamId },
  })
}

export function teamCourseList(courseId: string) {
  return http.get<any>(`${prefix}/biz/team/course/list`, {
    params: { course_id: courseId },
  })
}

export function teamUpdate(data: any) {
  return http.post<any>(`${prefix}/biz/team/update`, data)
}

export function teamMemberAdd(data: any) {
  return http.post<any>(`${prefix}/biz/team/member/add`, data)
}

export function teamMemberRemove(data: any) {
  return http.post<any>(`${prefix}/biz/team/member/remove`, data)
}

export function teamInviteRefresh(teamId: string) {
  return http.post<any>(`${prefix}/biz/team/invite/refresh`, {
    team_id: teamId,
  })
}

export function teamUserSearch(keyword: string, size = 20) {
  return http.get<any>(`${prefix}/biz/team/user/search`, {
    params: { keyword, size },
  })
}
