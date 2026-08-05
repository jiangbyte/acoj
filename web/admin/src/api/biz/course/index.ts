import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/course'

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${prefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function remove(data: { ids: string[] }) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function publish(params: { id: string }) {
  return http.post<any>(`${prefix}/publish`, {}, { params })
}

export function archive(params: { id: string }) {
  return http.post<any>(`${prefix}/archive`, {}, { params })
}

export function announcementList(params: { course_id: string }) {
  return http.get<any>(`${prefix}/announcement/list`, { params })
}

export function announcementCreate(data: any) {
  return http.post<any>(`${prefix}/announcement/create`, data)
}

export function announcementUpdate(data: any) {
  return http.post<any>(`${prefix}/announcement/update`, data)
}

export function announcementDelete(params: { id: string }) {
  return http.post<any>(`${prefix}/announcement/delete`, {}, { params })
}

export function taskList(params: { course_id: string }) {
  return http.get<any>(`${prefix}/task/list`, { params })
}

export function taskDetail(params: { id: string }) {
  return http.get<any>(`${prefix}/task/detail`, { params })
}

export function taskCreate(data: any) {
  return http.post<any>(`${prefix}/task/create`, data)
}

export function taskUpdate(data: any) {
  return http.post<any>(`${prefix}/task/update`, data)
}

export function taskDelete(params: { id: string }) {
  return http.post<any>(`${prefix}/task/delete`, {}, { params })
}

export function taskPublish(params: { id: string }) {
  return http.post<any>(`${prefix}/task/publish`, {}, { params })
}

export function taskClose(params: { id: string }) {
  return http.post<any>(`${prefix}/task/close`, {}, { params })
}

export function taskSetProblems(data: { task_id: string, problem_ids: string[], scores?: Record<string, number> }) {
  return http.post<any>(`${prefix}/task/set-problems`, data)
}

export function taskProgressBoard(params: { task_id: string }) {
  return http.get<any>(`${prefix}/task/progress-board`, { params })
}
