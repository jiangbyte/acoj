import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export type ProblemDifficulty = 'Easy' | 'Medium' | 'Hard'

export interface PortalProblemListItem {
  id: string
  code: string
  name: string
  summary: string | null
  group_id: string | null
  group_name: string | null
  time_limit_ms: number
  memory_limit_kb: number
  points: number
  partial: boolean
  difficulty: ProblemDifficulty
  user_count: number
  ac_rate: number
  solved: boolean
  type_ids: string[]
  type_names: string[]
}

export interface PortalProblemPageData extends PageData<PortalProblemListItem> {
  solved_count: number
}

export interface PortalProblemRecommendItem extends PortalProblemListItem {
  reason: string
  score: number
}

export interface PortalProblemRecommendData {
  records: PortalProblemRecommendItem[]
  strategy: string
  target_difficulty: string | null
}

export interface PortalProblemGroupItem {
  id: string
  code: string
  name: string
  sort: number
  problem_count: number
}

export interface PortalProblemTypeItem {
  id: string
  code: string
  name: string
  sort: number
  problem_count: number
}

export interface PortalProblemDetail extends PortalProblemListItem {
  description: string
  submission_source_visibility: string
  published_at: string | null
  extra?: Record<string, unknown> | null
}

export interface PortalProblemLanguage {
  language_key: string
  label: string | null
  extension: string | null
  time_limit_ms: number | null
  memory_limit_kb: number | null
}

export interface PortalProblemSubmitRequest {
  language_key: string
  source: string
  wait?: boolean
  wait_timeout_sec?: number
}

export interface SubmissionCaseSnapshot {
  case_no: number
  result: string | null
  points: number
  score: number
  time_ms: number
  memory_kb: number
  stdout_preview?: string
  stderr_preview?: string
}

export interface SubmissionSnapshot {
  submission_id: string
  status: string
  result: string | null
  score: number
  time_ms: number
  memory_kb: number
  compile_output: string | null
  compile_error: boolean
  cases: SubmissionCaseSnapshot[]
  error: string | null
  wall_time_ms: number
}

export interface ProblemPageParams {
  current?: number
  size?: number
  keyword?: string
  code?: string
  name?: string
  group_id?: string
  type_id?: string
}

export function problemPage(params: ProblemPageParams) {
  return http.get<PortalProblemPageData>(`${prefix}/biz/problem/page`, {
    params,
  })
}

/** 个性化题目推荐（登录按做题画像；游客按热门入门） */
export function problemRecommend(params?: { size?: number }) {
  return http.get<PortalProblemRecommendData>(`${prefix}/biz/problem/recommend`, {
    params,
  })
}

export function problemGroups() {
  return http.get<PortalProblemGroupItem[]>(`${prefix}/biz/problem/group/list`, {
    addToken: false,
  })
}

export function problemTypes() {
  return http.get<PortalProblemTypeItem[]>(`${prefix}/biz/problem/type/list`, {
    addToken: false,
  })
}

export function problemDetail(id: string) {
  return http.get<PortalProblemDetail>(`${prefix}/biz/problem/detail`, {
    params: { id },
  })
}

export function problemLanguages(problemId: string) {
  return http.get<PortalProblemLanguage[]>(`${prefix}/biz/problem/languages`, {
    params: { problem_id: problemId },
    addToken: false,
  })
}

export function problemSubmit(problemId: string, data: PortalProblemSubmitRequest) {
  return http.post<SubmissionSnapshot>(`${prefix}/biz/problem/submit`, data, {
    params: { problem_id: problemId },
  })
}
