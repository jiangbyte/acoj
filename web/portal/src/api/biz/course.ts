import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function coursePage(params?: any) {
  return http.get<any>(`${prefix}/biz/course/page`, {
    params,
  })
}

export function courseList(classId: string) {
  return http.get<any>(`${prefix}/biz/course/list`, {
    params: { class_id: classId },
  })
}

export function courseDetail(id: string) {
  return http.get<any>(`${prefix}/biz/course/detail`, { params: { id } })
}

export function courseAnnouncementList(courseId: string) {
  return http.get<any>(`${prefix}/biz/course/announcement/list`, {
    params: { course_id: courseId },
  })
}

export function courseTaskList(courseId: string) {
  return http.get<any>(`${prefix}/biz/course/task/list`, {
    params: { course_id: courseId },
  })
}

export function courseTaskDetail(id: string) {
  return http.get<any>(`${prefix}/biz/course/task/detail`, { params: { id } })
}

export function courseTaskCanSubmit(taskId: string) {
  return http.get<any>(`${prefix}/biz/course/task/can-submit`, {
    params: { task_id: taskId },
  })
}

export function courseTaskRecordSubmission(data: any) {
  return http.post<any>(`${prefix}/biz/course/task/record-submission`, data)
}
