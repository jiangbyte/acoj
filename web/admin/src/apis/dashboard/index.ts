import type {
  AnalysisBar,
  AnalysisRankItem,
  AnalysisSearchItem,
  GovernanceIssue,
  GovernanceModuleHealth,
  GovernanceRiskSlice,
  GovernanceTrendPoint,
  MetricItem,
  WorkplaceActivity,
  WorkplaceNotice,
  WorkplaceOverviewItem,
  WorkplaceProject,
  WorkplaceSchedule,
  WorkplaceShortcut,
  WorkplaceTeam,
  WorkplaceTodoItem,
} from '@/types/api'
import { getAnalysisData as getMockAnalysisData, getWorkplaceData as getMockWorkplaceData } from '@mock/modules/dashboard'

export interface WorkplaceData {
  overview: WorkplaceOverviewItem[]
  todos: WorkplaceTodoItem[]
  shortcuts: WorkplaceShortcut[]
  notices: WorkplaceNotice[]
  schedules: WorkplaceSchedule[]
  projects: WorkplaceProject[]
  activities: WorkplaceActivity[]
  teams: WorkplaceTeam[]
}

export interface AnalysisData {
  metrics: MetricItem[]
  bars: AnalysisBar[]
  ranks: AnalysisRankItem[]
  searches: AnalysisSearchItem[]
  trends: GovernanceTrendPoint[]
  moduleHealth: GovernanceModuleHealth[]
  riskSlices: GovernanceRiskSlice[]
  issues: GovernanceIssue[]
}

export async function getWorkplaceData(): Promise<WorkplaceData> {
  return getMockWorkplaceData()
}

export async function getAnalysisData(): Promise<AnalysisData> {
  return getMockAnalysisData()
}
