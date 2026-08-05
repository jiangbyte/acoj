import { http } from '@/utils'
import type { PageData } from '@/typing/api'
import type { SubmissionSnapshot } from './problem'

const prefix = '/api/v1/portal'

export interface PortalContestBrief {
  id: string
  key: string
  name: string
  summary: string | null
  description: string | null
  /** 竞赛封面图；未下发时可由 extra.cover_url 兜底 */
  cover_url?: string | null
  start_time: string
  end_time: string
  format_name: string
  lifecycle_status: string
  is_rated: boolean
  is_private: boolean
  use_clarifications: boolean
  scoreboard_visibility: string
  freeze_seconds: number | null
  user_count: number
  joined: boolean
  register_start?: string | null
  register_end?: string | null
  registration_mode?: string
  list_visibility?: string
  registration_status?: string | null
  registration_remark?: string | null
  can_register?: boolean
  can_enter?: boolean
  requires_access_code?: boolean
  extra?: Record<string, unknown> | null
}

export interface PortalContestParticipation {
  id: string
  contest_id: string
  account_id: string
  real_start: string
  score: number
  cumtime: number
  virtual: number
  is_disqualified: boolean
}

export interface PortalContestProblemMeta {
  id: string
  problem_id: string
  label: string
  points: number
  partial: boolean
  sort: number
  max_submissions: number | null
  problem_code: string | null
  problem_name: string | null
}

export interface PortalContestProblemDetail {
  id: string
  problem_id: string
  label: string
  points: number
  partial: boolean
  sort: number
  max_submissions: number | null
  problem_code: string
  problem_name: string
  description: string
  time_limit_ms: number
  memory_limit_kb: number
  languages: {
    language_key: string
    label: string | null
    extension: string | null
    time_limit_ms: number | null
    memory_limit_kb: number | null
  }[]
}

export interface PortalClarification {
  id: string
  contest_id: string
  problem_id: string | null
  title: string
  body: string
  published_at: string
}

export interface PortalClarificationMessage {
  id: string
  thread_id: string
  account_id: string
  body: string
  is_staff: boolean
  created_at: string
}

export interface PortalClarificationThread {
  id: string
  contest_id: string
  problem_id: string | null
  account_id: string
  title: string
  status: string
  messages: PortalClarificationMessage[]
}

export interface PortalContestSubmission {
  submission_id: string
  problem_id: string
  language_key: string
  status: string
  result: string | null
  score: number
  contest_points: number
  is_pretest: boolean
  created_at: string | null
}

export interface ContestScoreboard {
  contest_id: string
  format_name: string
  lifecycle_status: string
  is_frozen: boolean
  problems: {
    id: string
    problem_id: string
    label: string
    points: number
    partial: boolean
    sort: number
  }[]
  rows: {
    participation_id: string
    account_id: string
    score: number
    cumtime: number
    tiebreaker: number
    is_disqualified: boolean
    format_data: Record<string, unknown>
    real_start: string | null
    rank: number
    frozen?: boolean
    freeze_at?: string | null
  }[]
}

export function contestPage(params: { current?: number; size?: number; keyword?: string }) {
  return http.get<PageData<PortalContestBrief>>(`${prefix}/biz/contest/page`, {
    params,
  })
}

export function contestMine(params: { current?: number; size?: number }) {
  return http.get<PageData<PortalContestBrief>>(`${prefix}/biz/contest/mine`, { params })
}

export function contestDetail(id: string) {
  return http.get<PortalContestBrief>(`${prefix}/biz/contest/detail`, {
    params: { id },
  })
}

export function contestRegister(contestId: string, data: { access_code?: string | null } = {}) {
  return http.post<PortalContestBrief>(`${prefix}/biz/contest/register`, data, {
    params: { contest_id: contestId },
  })
}

export function contestUnregister(contestId: string) {
  return http.post<null>(`${prefix}/biz/contest/unregister`, null, {
    params: { contest_id: contestId },
  })
}

export function contestEnter(contestId: string) {
  return http.post<{ participation: PortalContestParticipation; first_problem_id: string | null }>(
    `${prefix}/biz/contest/enter`,
    null,
    { params: { contest_id: contestId } },
  )
}

/** @deprecated 使用 contestRegister / contestEnter */
export function contestJoin(contestId: string, data: { access_code?: string | null; spectate?: boolean }) {
  return http.post<PortalContestParticipation>(`${prefix}/biz/contest/join`, data, {
    params: { contest_id: contestId },
  })
}

export function contestLeave(contestId: string) {
  return http.post<null>(`${prefix}/biz/contest/leave`, null, {
    params: { contest_id: contestId },
  })
}

export function contestSubmit(contestId: string, data: { problem_id: string; language_key: string; source: string; wait?: boolean; wait_timeout_sec?: number }) {
  return http.post<SubmissionSnapshot>(`${prefix}/biz/contest/submit`, data, {
    params: { contest_id: contestId },
  })
}

export function contestProblems(contestId: string) {
  return http.get<PortalContestProblemMeta[]>(`${prefix}/biz/contest/problems`, {
    params: { contest_id: contestId },
  })
}

export function contestProblemDetail(contestId: string, problemId: string) {
  return http.get<PortalContestProblemDetail>(`${prefix}/biz/contest/problem/detail`, {
    params: { contest_id: contestId, problem_id: problemId },
  })
}

export function contestScoreboard(contestId: string) {
  return http.get<ContestScoreboard>(`${prefix}/biz/contest/scoreboard`, {
    params: { contest_id: contestId },
  })
}

export function contestMySubmissions(contestId: string) {
  return http.get<PortalContestSubmission[]>(`${prefix}/biz/contest/my-submissions`, {
    params: { contest_id: contestId },
  })
}

export function contestClarifications(contestId: string) {
  return http.get<PortalClarification[]>(`${prefix}/biz/contest/clarifications`, {
    params: { contest_id: contestId },
  })
}

export function contestMyThreads(contestId: string) {
  return http.get<PortalClarificationThread[]>(`${prefix}/biz/contest/clarification-threads/mine`, {
    params: { contest_id: contestId },
  })
}

export function contestCreateThread(contestId: string, data: { title: string; body: string; problem_id?: string | null }) {
  return http.post<PortalClarificationThread>(`${prefix}/biz/contest/clarification-threads`, data, {
    params: { contest_id: contestId },
  })
}

export function contestAddThreadMessage(contestId: string, threadId: string, data: { body: string }) {
  return http.post<PortalClarificationMessage>(
    `${prefix}/biz/contest/clarification-threads/messages`,
    data,
    { params: { contest_id: contestId, thread_id: threadId } },
  )
}
