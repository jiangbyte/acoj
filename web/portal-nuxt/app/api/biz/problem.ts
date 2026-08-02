import { http } from '~/composables/useHttp'

const prefix = '/api/v1/portal/biz/problem'

export type PortalProblemListItem = {
  id: string
  code: string
  name: string
  summary?: string | null
  group_name?: string | null
  time_limit_ms: number
  memory_limit_kb: number
  points: number
  partial: boolean
  user_count: number
  ac_rate: number
  type_names: string[]
}

export type PortalProblemDetail = PortalProblemListItem & {
  description: string
  submission_source_visibility: string
  published_at?: string | null
}

export type PortalProblemLanguage = {
  language_key: string
  label?: string | null
  extension?: string | null
  time_limit_ms?: number | null
  memory_limit_kb?: number | null
}

export type PageData<T> = {
  current: number
  size: number
  total: number
  pages?: number
  records: T[]
}

export type PortalSubmitSnapshot = {
  submission_id: string
  status: string
  result?: string | null
  score?: number
  time_ms?: number
  memory_kb?: number
  compile_output?: string | null
  cases?: unknown[]
}

export async function fetchProblemPage(params: {
  current?: number
  size?: number
  keyword?: string
  code?: string
  name?: string
  group_id?: string
  type_id?: string
}) {
  const { data } = await http.get<PageData<PortalProblemListItem>>(`${prefix}/page`, { params })
  return data
}

export async function fetchProblemDetail(id: string) {
  const { data } = await http.get<PortalProblemDetail>(`${prefix}/detail`, { params: { id } })
  return data
}

export async function fetchProblemLanguages(problemId: string) {
  const { data } = await http.get<PortalProblemLanguage[]>(`${prefix}/languages`, {
    params: { problem_id: problemId },
  })
  return data
}

export async function submitProblem(
  problemId: string,
  body: { language_key: string, source: string, wait?: boolean, wait_timeout_sec?: number },
) {
  const { data } = await http.post<PortalSubmitSnapshot>(`${prefix}/submit`, body, {
    params: { problem_id: problemId },
  })
  return data
}
