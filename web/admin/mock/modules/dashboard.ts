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
import { delay } from '@mock/utils'
import {
  analysisBars,
  analysisRankList,
  analysisSearchData,
  governanceIssues,
  governanceModuleHealth,
  governanceRiskSlices,
  governanceTrends,
  metrics,
  workplaceActivities,
  workplaceNotices,
  workplaceOverview,
  workplaceProjects,
  workplaceSchedules,
  workplaceShortcuts,
  workplaceTeams,
  workplaceTodos,
} from '@mock/data'

export async function getDashboardMetrics(): Promise<MetricItem[]> {
  return delay(metrics)
}

export async function getWorkplaceData(): Promise<{
  overview: WorkplaceOverviewItem[]
  todos: WorkplaceTodoItem[]
  shortcuts: WorkplaceShortcut[]
  notices: WorkplaceNotice[]
  schedules: WorkplaceSchedule[]
  projects: WorkplaceProject[]
  activities: WorkplaceActivity[]
  teams: WorkplaceTeam[]
}> {
  return delay({
    overview: workplaceOverview,
    todos: workplaceTodos,
    shortcuts: workplaceShortcuts,
    notices: workplaceNotices,
    schedules: workplaceSchedules,
    projects: workplaceProjects,
    activities: workplaceActivities,
    teams: workplaceTeams,
  })
}

export async function getAnalysisData(): Promise<{
  metrics: MetricItem[]
  bars: AnalysisBar[]
  ranks: AnalysisRankItem[]
  searches: AnalysisSearchItem[]
  trends: GovernanceTrendPoint[]
  moduleHealth: GovernanceModuleHealth[]
  riskSlices: GovernanceRiskSlice[]
  issues: GovernanceIssue[]
}> {
  return delay({
    metrics,
    bars: analysisBars,
    ranks: analysisRankList,
    searches: analysisSearchData,
    trends: governanceTrends,
    moduleHealth: governanceModuleHealth,
    riskSlices: governanceRiskSlices,
    issues: governanceIssues,
  })
}
