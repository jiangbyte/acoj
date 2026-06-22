export type RecordStatus = 'enabled' | 'disabled' | 'locked' | 'ENABLED' | 'DISABLED' | 'LOCKED'

export interface PageQuery {
  page?: number
  page_size?: number
  keyword?: string
  [key: string]: unknown
}

export interface PageData<T> {
  items: T[]
  total: number
  page: number
  page_size: number
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
