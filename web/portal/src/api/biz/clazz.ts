import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function clazzPage(params?: any) {
  return http.get<any>(`${prefix}/biz/clazz/page`, {
    params,
  })
}

export function clazzMy() {
  return http.get<any>(`${prefix}/biz/clazz/my`)
}

export function clazzJoin(data: any) {
  return http.post<any>(`${prefix}/biz/clazz/join`, {
    invite_code: data.invite_code.trim().toUpperCase(),
  })
}

export function clazzDetail(id: string) {
  return http.get<any>(`${prefix}/biz/clazz/detail`, { params: { id } })
}

export function clazzMembers(classId: string) {
  return http.get<any>(`${prefix}/biz/clazz/members`, {
    params: { class_id: classId },
  })
}
