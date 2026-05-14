import { request } from '@/utils'

export interface DashboardStats {
  total_users: number
  active_users: number
  total_roles: number
  total_orgs: number
  total_configs: number
  total_notices: number
}

export interface TrendItem {
  month: string
  count: number
}

export interface OrgUserDistribution {
  name: string
  count: number
}

export interface CategoryDistribution {
  category: string
  count: number
}

export interface SysInfo {
  python_version: string
  os_name: string
  server_ip: string
  run_time: string
}

export interface ClientStats {
  total_users: number
  active_users: number
}

export interface DashboardData {
  stats: DashboardStats
  client_stats: ClientStats
  user_trend: TrendItem[]
  client_trend: TrendItem[]
  org_user_distribution: OrgUserDistribution[]
  role_category_distribution: CategoryDistribution[]
  sys_info: SysInfo
}

export function fetchDashboard() {
  return request.Get<Service.ResponseResult<DashboardData>>('/api/v1/sys/analyze/dashboard')
}
