import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export interface PortalCourseClassBrief {
  id: string
  code: string
  name: string
}

export interface PortalCourseBrief {
  id: string
  class_id: string | null
  class_ids: string[]
  classes: PortalCourseClassBrief[]
  name: string
  summary: string | null
  cover_url: string | null
  status: string
  visibility: string
  access_scope: string
  binding_mode: string
  sort: number
  can_participate?: boolean
  extra: Record<string, unknown>
  created_at: string
  created_by: string | null
  updated_at: string
  updated_by: string | null
}

export interface PortalCourseAnnouncement {
  id: string
  course_id: string
  title: string
  content: string | null
  status: string
  published_at: string | null
  created_at: string
  updated_at: string
}

export interface PortalCourseTaskProblem {
  id: string
  task_id: string
  problem_id: string
  sort: number
  score: number | null
}

export interface PortalCourseTaskProgress {
  id: string
  task_id: string
  account_id: string
  solved_count: number
  total_count: number
  status: string
  finished_at: string | null
}

export interface PortalCourseTask {
  id: string
  course_id: string
  title: string
  description: string | null
  mode: string
  status: string
  open_at: string | null
  close_at: string | null
  due_at: string | null
  sort: number
  extra: Record<string, unknown>
  created_at: string
  updated_at: string
  problems: PortalCourseTaskProblem[]
  my_progress: PortalCourseTaskProgress | null
}

export function coursePage(params?: { current?: number; size?: number; keyword?: string }) {
  return http.get<PageData<PortalCourseBrief>>(`${prefix}/biz/course/page`, {
    params,
  })
}

export function courseList(classId: string) {
  return http.get<PortalCourseBrief[]>(`${prefix}/biz/course/list`, {
    params: { class_id: classId },
  })
}

export function courseDetail(id: string) {
  return http.get<PortalCourseBrief>(`${prefix}/biz/course/detail`, { params: { id } })
}

export function courseAnnouncementList(courseId: string) {
  return http.get<PortalCourseAnnouncement[]>(`${prefix}/biz/course/announcement/list`, {
    params: { course_id: courseId },
  })
}

export function courseTaskList(courseId: string) {
  return http.get<PortalCourseTask[]>(`${prefix}/biz/course/task/list`, {
    params: { course_id: courseId },
  })
}

export function courseTaskDetail(id: string) {
  return http.get<PortalCourseTask>(`${prefix}/biz/course/task/detail`, { params: { id } })
}

export function courseTaskCanSubmit(taskId: string) {
  return http.get<{ allowed: boolean }>(`${prefix}/biz/course/task/can-submit`, {
    params: { task_id: taskId },
  })
}

export function courseTaskRecordSubmission(data: {
  task_id: string
  problem_id: string
  submission_id: string
}) {
  return http.post<null>(`${prefix}/biz/course/task/record-submission`, data)
}
