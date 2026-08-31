/** Author: Charlie */

export * as authApi from './auth'
export * as publicApi from './public'
export * as myNoticeApi from './sys/my-notice'
export * as sysNoticeApi from './sys/notice'
export * as sysFeedbackApi from './sys/feedback'
export * as auditApi from './sys/audit'
export * as bannerApi from './sys/banner'
export * as dictApi from './sys/dict'
export * as fileApi from './sys/file'
export * as realNameApi from './realName'
export * as ojMetaApi from './oj/meta'
export * as ojProblemApi from './oj/problem'
export * as ojSubmissionApi from './oj/submission'
export * as ojTagApi from './oj/tag'
export * as ojUserApi from './oj/user'
export type { OjTagOption, OjPortalTagOptions } from './oj/tag'
export type {
  OjUserHomepage,
  OjSolveProgress,
  OjLanguageStat,
  OjHeatmapStat,
  OjRecentAccepted,
} from './oj/user'
