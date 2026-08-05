import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export interface PortalTeamPublic {
  id: string
  name: string
  description: string | null
  status: string
  visibility: string
  max_members: number
  member_count: number
  created_at: string
  is_member: boolean
}

export interface PortalTeamBrief {
  id: string
  scope: string
  course_id: string | null
  class_id: string | null
  name: string
  description: string | null
  owner_id: string
  invite_code: string | null
  im_group_id: string | null
  status: string
  visibility: string
  max_members: number
  member_count: number
  extra: Record<string, unknown>
  created_at: string
  updated_at: string
  conversation_id: string | null
  is_member: boolean
}

export interface PortalTeamMember {
  id: string
  team_id: string
  account_id: string
  role: string
  joined_at: string
}

export function teamPage(params?: { current?: number; size?: number; keyword?: string }) {
  return http.get<PageData<PortalTeamPublic>>(`${prefix}/biz/team/page`, {
    params,
  })
}

export function teamMy() {
  return http.get<PortalTeamBrief[]>(`${prefix}/biz/team/my`)
}

export function teamCreate(data: { name: string; description?: string; max_members?: number }) {
  return http.post<{ id: string }>(`${prefix}/biz/team/create`, data)
}

export function teamJoin(data: { invite_code: string }) {
  return http.post<{ id: string }>(`${prefix}/biz/team/join`, {
    invite_code: data.invite_code.trim().toUpperCase(),
  })
}

export function teamLeave(id: string) {
  return http.post<null>(`${prefix}/biz/team/leave`, null, { params: { id } })
}

export function teamDissolve(id: string) {
  return http.post<null>(`${prefix}/biz/team/dissolve`, null, { params: { id } })
}

export function teamDetail(id: string) {
  return http.get<PortalTeamBrief>(`${prefix}/biz/team/detail`, { params: { id } })
}

export function teamMembers(teamId: string) {
  return http.get<PortalTeamMember[]>(`${prefix}/biz/team/members`, {
    params: { team_id: teamId },
  })
}

export function teamCourseList(courseId: string) {
  return http.get<PortalTeamBrief[]>(`${prefix}/biz/team/course/list`, {
    params: { course_id: courseId },
  })
}

export function teamUpdate(data: {
  id: string
  name?: string
  description?: string | null
  max_members?: number
  visibility?: 'PUBLIC' | 'PRIVATE'
}) {
  return http.post<null>(`${prefix}/biz/team/update`, data)
}

export function teamMemberAdd(data: { team_id: string; account_ids: string[] }) {
  return http.post<null>(`${prefix}/biz/team/member/add`, data)
}

export function teamMemberRemove(data: { team_id: string; account_id: string }) {
  return http.post<null>(`${prefix}/biz/team/member/remove`, data)
}

export function teamInviteRefresh(teamId: string) {
  return http.post<{ invite_code: string }>(`${prefix}/biz/team/invite/refresh`, {
    team_id: teamId,
  })
}

export interface PortalTeamUserSearchItem {
  account_id: string
  username: string | null
  nickname: string | null
  avatar: string | null
}

export function teamUserSearch(keyword: string, size = 20) {
  return http.get<PortalTeamUserSearchItem[]>(`${prefix}/biz/team/user/search`, {
    params: { keyword, size },
  })
}
