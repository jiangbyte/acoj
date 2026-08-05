import { http } from '@/utils'

const listPrefix = '/api/v1/admin/biz/problem-list'
const planPrefix = '/api/v1/admin/biz/learning-plan'
const dailyPrefix = '/api/v1/admin/biz/daily'

export const ojProblemListApi = {
  page: (params: any) => http.get<any>(`${listPrefix}/page`, { params }),
  detail: (params: any) => http.get<any>(`${listPrefix}/detail`, { params }),
  create: (data: any) => http.post<any>(`${listPrefix}/create`, data),
  update: (data: any) => http.post<any>(`${listPrefix}/update`, data),
  remove: (data: any) => http.post<any>(`${listPrefix}/delete`, data),
}

export const ojLearningPlanApi = {
  page: (params: any) => http.get<any>(`${planPrefix}/page`, { params }),
  detail: (params: any) => http.get<any>(`${planPrefix}/detail`, { params }),
  create: (data: any) => http.post<any>(`${planPrefix}/create`, data),
  update: (data: any) => http.post<any>(`${planPrefix}/update`, data),
  remove: (data: any) => http.post<any>(`${planPrefix}/delete`, data),
}

export const ojDailyApi = {
  page: (params: any) => http.get<any>(`${dailyPrefix}/page`, { params }),
  upsert: (data: any) => http.post<any>(`${dailyPrefix}/upsert`, data),
  remove: (data: any) => http.post<any>(`${dailyPrefix}/delete`, data),
}
