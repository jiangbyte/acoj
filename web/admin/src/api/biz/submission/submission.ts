import { http } from '@/utils'

export { watchSubmissionEvents, pollSubmissionUntilDone } from './watch'
export type { SubmissionWatchSnapshot } from './watch'

const prefix = '/api/v1/admin/biz/submission/submission'

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function remove(data: any) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function rejudge(data: { ids: string[] }) {
  return http.post<any>(`${prefix}/rejudge`, data)
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

export function performance(params: { id: string }) {
  return http.get<SubmissionPerformanceData>(`${prefix}/performance`, { params })
}

export function similar(params: { id: string, size?: number }) {
  return http.get<SimilarSubmissionListData>(`${prefix}/similar`, { params })
}
