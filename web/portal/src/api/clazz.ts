import { http } from '@/utils'
import type { PageData } from '@/typing/api'
import type { PortalCourseBrief } from './course'

const prefix = '/api/v1/portal'

export interface PortalClassPublic {
  id: string
  code: string
  name: string
  summary: string | null
  status: string
  visibility: string
  member_count: number
  created_at: string
  joined: boolean
}

export interface PortalClassBrief {
  id: string
  code: string
  name: string
  summary: string | null
  invite_code: string | null
  status: string
  visibility: string
  im_group_id: string | null
  member_count: number
  extra: Record<string, unknown>
  created_at: string
  created_by: string | null
  updated_at: string
  updated_by: string | null
  conversation_id: string | null
  joined?: boolean
}

export interface PortalClassMember {
  id: string
  class_id: string
  account_id: string
  role: string
  joined_at: string
  left_at: string | null
}

export function clazzPage(params?: { current?: number; size?: number; keyword?: string }) {
  return http.get<PageData<PortalClassPublic>>(`${prefix}/biz/clazz/page`, {
    params,
  })
}

export function clazzMy() {
  return http.get<PortalClassBrief[]>(`${prefix}/biz/clazz/my`)
}

export function clazzJoin(data: { invite_code: string }) {
  return http.post<{ id: string }>(`${prefix}/biz/clazz/join`, {
    invite_code: data.invite_code.trim().toUpperCase(),
  })
}

export function clazzDetail(id: string) {
  return http.get<PortalClassBrief>(`${prefix}/biz/clazz/detail`, { params: { id } })
}

export function clazzMembers(classId: string) {
  return http.get<PortalClassMember[]>(`${prefix}/biz/clazz/members`, {
    params: { class_id: classId },
  })
}

export function clazzCourses(classId: string) {
  return http.get<PortalCourseBrief[]>(`${prefix}/biz/clazz/courses`, {
    params: { class_id: classId },
  })
}
