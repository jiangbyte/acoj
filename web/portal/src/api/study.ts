import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export interface ProblemListProgress {
  solved: number
  attempted: number
  total: number
  easy_solved: number
  easy_total: number
  medium_solved: number
  medium_total: number
  hard_solved: number
  hard_total: number
}

export interface ProblemListProblemBrief {
  id: string
  code: string
  name: string
  difficulty: string
  ac_rate: number
  user_count: number
  solved: boolean
  attempted: boolean
  sort: number
}

export interface ProblemListItem {
  id: string
  kind: string
  owner_id: string | null
  code: string | null
  title: string
  summary: string | null
  cover_url: string | null
  visibility: string
  is_system: boolean
  status: string
  sort: number
  problem_count: number
  progress?: ProblemListProgress | null
  problems?: ProblemListProblemBrief[]
}

export interface LearningPlanSection {
  id: string
  title: string
  sort: number
  problems: ProblemListProblemBrief[]
}

export interface LearningPlanItem {
  id: string
  code: string
  title: string
  subtitle: string | null
  overview: string | null
  cover_url: string | null
  category: string
  status: string
  sort: number
  problem_count: number
  progress?: ProblemListProgress | null
  sections?: LearningPlanSection[]
  related?: LearningPlanItem[]
}

export interface DailyToday {
  day_date: string
  problem: {
    id: string
    day_date: string
    problem_id: string
    problem_code: string | null
    problem_name: string | null
    difficulty: string | null
    ac_rate: number
    checked_in: boolean
  } | null
  checked_in: boolean
  streak: number
  month_done: number
  month_total: number
}

export interface DailyCalendar {
  year: number
  month: number
  days: { day_date: string; has_problem: boolean; checked_in: boolean; problem_id: string | null }[]
  streak: number
  month_done: number
  month_total: number
}

export interface UserStats {
  account_id: string
  solved_total: number
  problem_total: number
  submission_total: number
  ac_submission_total: number
  ac_rate: number
  streak: number
  by_difficulty: { difficulty: string; solved: number; total: number }[]
}

export interface HeatmapDay {
  day_date: string
  count: number
}

export interface UserHeatmap {
  year: number
  days: HeatmapDay[]
  total_submissions: number
  active_days: number
}

export interface RecentSolvedItem {
  problem_id: string
  problem_code: string
  problem_name: string
  difficulty: string
  solved_at: string
}

export const problemListApi = {
  mine: () => http.get<ProblemListItem[]>(`${prefix}/biz/problem-list/mine`),
  page: (params?: { current?: number; size?: number }) =>
    http.get<PageData<ProblemListItem>>(`${prefix}/biz/problem-list/page`, { params, addToken: false }),
  detail: (id: string) => http.get<ProblemListItem>(`${prefix}/biz/problem-list/detail`, { params: { id } }),
  create: (data: { title: string; summary?: string; visibility?: string; problem_ids?: string[] }) =>
    http.post<{ id: string }>(`${prefix}/biz/problem-list/create`, data),
  update: (data: Record<string, unknown>) => http.post(`${prefix}/biz/problem-list/update`, data),
  remove: (ids: string[]) => http.post(`${prefix}/biz/problem-list/delete`, { ids }),
  addItem: (list_id: string, problem_id: string) =>
    http.post(`${prefix}/biz/problem-list/item/add`, { list_id, problem_id }),
  removeItem: (list_id: string, problem_id: string) =>
    http.post(`${prefix}/biz/problem-list/item/remove`, { list_id, problem_id }),
  favoriteStatus: (problem_id: string) =>
    http.get<{ favorited: boolean }>(`${prefix}/biz/problem-list/favorite/status`, { params: { problem_id } }),
  addFavorite: (problem_id: string) =>
    http.post(`${prefix}/biz/problem-list/favorite/add`, null, { params: { problem_id } }),
  removeFavorite: (problem_id: string) =>
    http.post(`${prefix}/biz/problem-list/favorite/remove`, null, { params: { problem_id } }),
}

export const learningPlanApi = {
  page: (params?: { current?: number; size?: number; category?: string }) =>
    http.get<PageData<LearningPlanItem>>(`${prefix}/biz/learning-plan/page`, { params, addToken: false }),
  detail: (id: string) => http.get<LearningPlanItem>(`${prefix}/biz/learning-plan/detail`, { params: { id } }),
}

export const dailyApi = {
  today: () => http.get<DailyToday>(`${prefix}/biz/daily/today`),
  calendar: (year: number, month: number) =>
    http.get<DailyCalendar>(`${prefix}/biz/daily/calendar`, { params: { year, month } }),
}

export const userStatsApi = {
  stats: (account_id?: string) =>
    http.get<UserStats>(`${prefix}/biz/user/stats`, { params: account_id ? { account_id } : undefined }),
  heatmap: (year: number, account_id?: string) =>
    http.get<UserHeatmap>(`${prefix}/biz/user/heatmap`, {
      params: { year, ...(account_id ? { account_id } : {}) },
    }),
  recentSolved: (params?: { account_id?: string; current?: number; size?: number }) =>
    http.get<PageData<RecentSolvedItem>>(`${prefix}/biz/user/recent-solved`, { params }),
}
