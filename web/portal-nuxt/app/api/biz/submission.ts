import { http } from '~/composables/useHttp'
import type { PageData } from './problem'

const prefix = '/api/v1/portal/biz/submission'

export type PortalSubmissionListItem = {
  id: string
  user_id: string
  user_nickname?: string | null
  user_avatar?: string | null
  problem_id: string
  problem_code?: string | null
  problem_name?: string | null
  language_key: string
  kind: string
  status: string
  result?: string | null
  score: number
  time_ms: number
  memory_kb: number
  contest_id?: string | null
  created_at: string
}

export type PortalSubmissionDetail = PortalSubmissionListItem & {
  compile_output?: string | null
  error?: string | null
  source?: string | null
  cases?: Array<{
    case_no: number
    result?: string | null
    score: number
    time_ms: number
    memory_kb: number
  }>
}

export async function fetchSubmissionPage(params: {
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
}) {
  const { data } = await http.get<PageData<PortalSubmissionListItem>>(`${prefix}/page`, { params })
  return data
}

export async function fetchSubmissionDetail(id: string) {
  const { data } = await http.get<PortalSubmissionDetail>(`${prefix}/detail`, { params: { id } })
  return data
}
