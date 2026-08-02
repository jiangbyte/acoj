import { http } from '~/composables/useHttp'
import type { PageData } from './problem'

const prefix = '/api/v1/portal/biz/contest'

export type PortalContestBrief = {
  id: string
  key: string
  name: string
  summary?: string | null
  description?: string | null
  start_time: string
  end_time: string
  format_name: string
  lifecycle_status: string
  is_rated: boolean
  is_private?: boolean
  use_clarifications: boolean
  scoreboard_visibility: string
  freeze_seconds?: number | null
  user_count: number
  joined?: boolean
}

export type PortalContestProblemMeta = {
  id: string
  problem_id: string
  label: string
  points: number
  partial: boolean
  sort: number
  max_submissions?: number | null
  problem_code?: string | null
  problem_name?: string | null
}

export type PortalContestProblemDetail = PortalContestProblemMeta & {
  description: string
  time_limit_ms: number
  memory_limit_kb: number
  languages: Array<{
    language_key: string
    label?: string | null
    extension?: string | null
  }>
}

export async function fetchContestPage(params: { current?: number, size?: number, keyword?: string }) {
  const { data } = await http.get<PageData<PortalContestBrief>>(`${prefix}/page`, { params })
  return data
}

export async function fetchContestDetail(id: string) {
  const { data } = await http.get<PortalContestBrief>(`${prefix}/detail`, { params: { id } })
  return data
}

export async function joinContest(contestId: string, body: { access_code?: string, spectate?: boolean }) {
  const { data } = await http.post(`${prefix}/join`, body, { params: { contest_id: contestId } })
  return data
}

export async function leaveContest(contestId: string) {
  const { data } = await http.post(`${prefix}/leave`, {}, { params: { contest_id: contestId } })
  return data
}

export async function fetchContestProblems(contestId: string) {
  const { data } = await http.get<PortalContestProblemMeta[]>(`${prefix}/problems`, {
    params: { contest_id: contestId },
  })
  return data
}

export async function fetchContestProblemDetail(contestId: string, problemId: string) {
  const { data } = await http.get<PortalContestProblemDetail>(`${prefix}/problem/detail`, {
    params: { contest_id: contestId, problem_id: problemId },
  })
  return data
}

export async function submitContest(
  contestId: string,
  body: {
    problem_id: string
    language_key: string
    source: string
    wait?: boolean
    wait_timeout_sec?: number
  },
) {
  const { data } = await http.post<{ submission_id: string }>(`${prefix}/submit`, body, {
    params: { contest_id: contestId },
  })
  return data
}

export async function fetchContestScoreboard(contestId: string) {
  const { data } = await http.get<Record<string, unknown>>(`${prefix}/scoreboard`, {
    params: { contest_id: contestId },
  })
  return data
}

export async function fetchMyContestSubmissions(contestId: string) {
  const { data } = await http.get<Array<Record<string, unknown>>>(`${prefix}/my-submissions`, {
    params: { contest_id: contestId },
  })
  return data
}

export async function fetchClarifications(contestId: string) {
  const { data } = await http.get<Array<Record<string, unknown>>>(`${prefix}/clarifications`, {
    params: { contest_id: contestId },
  })
  return data
}

export async function fetchMyClarificationThreads(contestId: string) {
  const { data } = await http.get<Array<Record<string, unknown>>>(`${prefix}/clarification-threads/mine`, {
    params: { contest_id: contestId },
  })
  return data
}

export async function createClarificationThread(
  contestId: string,
  body: { title: string, body: string, problem_id?: string },
) {
  const { data } = await http.post(`${prefix}/clarification-threads`, body, {
    params: { contest_id: contestId },
  })
  return data
}

export async function addClarificationMessage(
  contestId: string,
  threadId: string,
  body: { body: string },
) {
  const { data } = await http.post(`${prefix}/clarification-threads/messages`, body, {
    params: { contest_id: contestId, thread_id: threadId },
  })
  return data
}
