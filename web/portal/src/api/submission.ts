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
