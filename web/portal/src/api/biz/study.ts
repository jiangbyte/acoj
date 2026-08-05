import { http } from '@/utils'

const prefix = '/api/v1/portal'

export const problemListApi = {
  mine: () => http.get<any>(`${prefix}/biz/problem-list/mine`),
  page: (params?: any) =>
    http.get<any>(`${prefix}/biz/problem-list/page`, { params, addToken: false }),
  detail: (id: string) => http.get<any>(`${prefix}/biz/problem-list/detail`, { params: { id } }),
  create: (data: any) =>
    http.post<any>(`${prefix}/biz/problem-list/create`, data),
  update: (data: any) => http.post<any>(`${prefix}/biz/problem-list/update`, data),
  remove: (ids: string[]) => http.post<any>(`${prefix}/biz/problem-list/delete`, { ids }),
  addItem: (list_id: string, problem_id: string) =>
    http.post<any>(`${prefix}/biz/problem-list/item/add`, { list_id, problem_id }),
  removeItem: (list_id: string, problem_id: string) =>
    http.post<any>(`${prefix}/biz/problem-list/item/remove`, { list_id, problem_id }),
  favoriteStatus: (problem_id: string) =>
    http.get<any>(`${prefix}/biz/problem-list/favorite/status`, { params: { problem_id } }),
  addFavorite: (problem_id: string) =>
    http.post<any>(`${prefix}/biz/problem-list/favorite/add`, { problem_id }),
  removeFavorite: (problem_id: string) =>
    http.post<any>(`${prefix}/biz/problem-list/favorite/remove`, { problem_id }),
}

export const learningPlanApi = {
  page: (params?: any) =>
    http.get<any>(`${prefix}/biz/learning-plan/page`, { params, addToken: false }),
  detail: (id: string) => http.get<any>(`${prefix}/biz/learning-plan/detail`, { params: { id } }),
}

export const dailyApi = {
  today: () => http.get<any>(`${prefix}/biz/daily/today`),
  calendar: (year: number, month: number) =>
    http.get<any>(`${prefix}/biz/daily/calendar`, { params: { year, month } }),
}

export const userStatsApi = {
  stats: (account_id?: string) =>
    http.get<any>(`${prefix}/biz/user/stats`, { params: account_id ? { account_id } : undefined }),
  heatmap: (year: number, account_id?: string) =>
    http.get<any>(`${prefix}/biz/user/heatmap`, {
      params: { year, ...(account_id ? { account_id } : {}) },
    }),
  recentSolved: (params?: any) =>
    http.get<any>(`${prefix}/biz/user/recent-solved`, { params }),
}
