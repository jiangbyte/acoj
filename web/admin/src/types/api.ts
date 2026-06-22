import type { RecordStatus, SysStatus } from '@hei/shared'

export type { PageData, PageQuery, RecordStatus, SysStatus } from '@hei/shared'

export interface SysBannerItem {
  id: string
  title: string
  image: string
  url?: string | null
  link_type: 'URL' | 'ROUTE' | 'NONE'
  summary?: string | null
  description?: string | null
  category: 'HOME' | 'LOGIN' | 'WORKPLACE' | 'NOTICE' | 'ADMIN_DASHBOARD' | 'SYSTEM_UPGRADE'
  type: 'CAROUSEL' | 'HERO' | 'NOTICE' | 'CARD' | 'POPUP' | 'SIDEBAR'
  position:
    | 'HOME_TOP'
    | 'HOME_MIDDLE'
    | 'HOME_BOTTOM'
    | 'LOGIN_SIDE'
    | 'WORKPLACE_TOP'
    | 'NOTICE_AREA'
    | 'ADMIN_TOP'
    | 'ADMIN_SIDEBAR'
  display_scope: 'PORTAL' | 'ADMIN' | 'APP'
  sort: number
  interaction_count: number
  status: SysStatus | string
  start_at?: string | null
  end_at?: string | null
  created_at: string
  created_by?: string | null
  updated_at: string
  updated_by?: string | null
}

export interface SysDictItem {
  id: string
  code: string
  label?: string | null
  value?: string | null
  color?: string | null
  category?: 'SYS' | 'BIZ' | null
  parent_id?: string | null
  status: SysStatus | string
  sort: number
  created_at: string
  created_by?: string | null
  updated_at: string
  updated_by?: string | null
}

export interface SysDictTreeNode extends SysDictItem {
  children: SysDictTreeNode[]
}

export interface AccountItem {
  id: string
  account: string
  account_type: 'admin' | 'portal'
  account_status: RecordStatus
  name: string
  nickname: string
  phone: string
  email: string
  dept_name: string
  role_names: string[]
  created_at: string
  updated_at: string
}

export interface DeptNode {
  id: string
  name: string
  code: string
  category: string
  parent_id?: string
  manager?: string
  children?: DeptNode[]
}

export interface RoleItem {
  id: string
  code: string
  name: string
  category: string
  scope_type: string
  owner_dept_id?: string
  user_count: number
  created_at: string
}

export interface FileItem {
  id: string
  filename: string
  content_type: string
  size: number
  storage_provider: 'local' | 's3'
  uploader: string
  created_at: string
}

export interface MetricItem {
  title: string
  value: string
  change: string
  trend: 'up' | 'down' | 'flat'
}

export interface WorkplaceProject {
  title: string
  description: string
  group: string
  updated_at: string
}

export interface WorkplaceActivity {
  user: string
  action: string
  target: string
  time: string
}

export interface WorkplaceTeam {
  name: string
  title: string
}

export type WorkplacePriority = 'high' | 'medium' | 'low'

export interface WorkplaceOverviewItem {
  title: string
  value: string
  description: string
  color: 'blue' | 'green' | 'orange' | 'red'
}

export interface WorkplaceTodoItem {
  id: string
  title: string
  module: string
  priority: WorkplacePriority
  status: 'pending' | 'processing' | 'overdue'
  due_time: string
  owner: string
  path: string
}

export interface WorkplaceShortcut {
  title: string
  description: string
  path: string
  icon: string
  color: string
}

export interface WorkplaceNotice {
  id: string
  title: string
  content: string
  level: 'info' | 'warning' | 'success'
  time: string
}

export interface WorkplaceSchedule {
  id: string
  title: string
  time: string
  participant: string
  status: 'todo' | 'done'
}

export interface AnalysisBar {
  label: string
  value: number
}

export interface AnalysisRankItem {
  name: string
  total: number
}

export interface AnalysisSearchItem {
  index: number
  keyword: string
  count: number
  range: number
  status: 'up' | 'down'
}

export interface GovernanceTrendPoint {
  date: string
  visits: number
  grants: number
  audits: number
}

export interface GovernanceModuleHealth {
  module: string
  score: number
  owner: string
  status: 'healthy' | 'warning' | 'risk'
}

export interface GovernanceRiskSlice {
  name: string
  value: number
  level: 'low' | 'medium' | 'high'
}

export interface GovernanceIssue {
  id: string
  title: string
  module: string
  impact: string
  owner: string
  trend: 'up' | 'down' | 'flat'
  status: 'open' | 'processing' | 'closed'
}
