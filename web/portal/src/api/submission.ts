import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export interface OjSubmissionListItem {
  id: string
  user_id: string
  user_nickname: string | null
  user_avatar: string | null
  user_account_type: string | null
  problem_id: string
  problem_code: string | null
  problem_name: string | null
  language_key: string
  kind: string
  status: string
  result: string | null
  score: number
  time_ms: number
  memory_kb: number
  contest_id: string | null
  contest_key: string | null
  contest_name: string | null
  case_points: number
  case_total: number
  locked_at: string | null
  created_at: string
  updated_at: string
}

export interface OjSubmissionCase {
  id: string
  submission_id: string
  case_no: number
  test_case_id: string | null
  result: string | null
  score: number
  time_ms: number
  memory_kb: number
  stdout_preview: string | null
  stderr_preview: string | null
  feedback: string | null
}

export interface OjContestSubmission {
  id: string
  submission_id: string
  contest_problem_id: string
  participation_id: string
  points: number
  is_pretest: boolean
}

export interface OjSubmissionDetail extends OjSubmissionListItem {
  compile_output: string | null
  error: string | null
  source: string | null
  cases: OjSubmissionCase[]
  contest_submission: OjContestSubmission | null
}

export interface SubmissionPageParams {
  current?: number
  size?: number
  problem_id?: string
  problem_code?: string
  contest_id?: string
  user_id?: string
  kind?: string
  status?: string
  result?: string
  language_key?: string
}

export function submissionPage(params: SubmissionPageParams) {
  return http.get<PageData<OjSubmissionListItem>>(`${prefix}/biz/submission/page`, {
    params,
    addToken: false,
  })
}

export function submissionDetail(id: string) {
  return http.get<OjSubmissionDetail>(`${prefix}/biz/submission/detail`, {
    params: { id },
    addToken: false,
  })
}

export interface PerformanceBucket {
  start: number
  end: number
  count: number
  is_current: boolean
}

export interface SubmissionPerformanceData {
  available: boolean
  reason?: string | null
  scope?: 'practice' | 'contest' | null
  problem_id?: string | null
  language_key?: string | null
  contest_id?: string | null
  time_ms?: number | null
  memory_kb?: number | null
  sample_size?: number | null
  insufficient_sample?: boolean | null
  beats_time_pct?: number | null
  beats_memory_pct?: number | null
  runtime_buckets?: PerformanceBucket[] | null
  memory_buckets?: PerformanceBucket[] | null
}

export interface SimilarSubmissionItem {
  id: string
  user_id: string
  nickname?: string | null
  avatar?: string | null
  language_key: string
  time_ms: number
  memory_kb: number
  created_at: string
  source?: string | null
}

export interface SimilarSubmissionListData {
  available: boolean
  reason?: string | null
  items: SimilarSubmissionItem[]
}

export interface MyLatestPracticeAcData {
  submission_id: string | null
}

export interface MySubmissionStatsData {
  submission_total: number
  ac_total: number
  fail_total: number
  judging_total: number
  ac_rate: number
  solved_problem_total: number
}

export function submissionPerformance(id: string) {
  return http.get<SubmissionPerformanceData>(`${prefix}/biz/submission/performance`, {
    params: { id },
  })
}

export function submissionSimilar(id: string, size = 10) {
  return http.get<SimilarSubmissionListData>(`${prefix}/biz/submission/similar`, {
    params: { id, size },
  })
}

export function myLatestPracticeAc(problemId: string) {
  return http.get<MyLatestPracticeAcData>(`${prefix}/biz/submission/my-latest-ac`, {
    params: { problem_id: problemId },
  })
}

export function mySubmissionStats() {
  return http.get<MySubmissionStatsData>(`${prefix}/biz/submission/my-stats`)
}
