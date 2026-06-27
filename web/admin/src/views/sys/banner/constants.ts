export const statusOptions = [
  { labelKey: 'common.often.enable', value: 'ENABLED' },
  { labelKey: 'common.often.disable', value: 'DISABLED' },
]

export const displayScopeOptions = [
  { labelKey: 'pages.sys.banner.displayScopes.portal', value: 'PORTAL' },
  { labelKey: 'pages.sys.banner.displayScopes.admin', value: 'ADMIN' },
  { labelKey: 'pages.sys.banner.displayScopes.app', value: 'APP' },
]

export const categoryOptions = [
  { labelKey: 'pages.sys.banner.categories.home', value: 'HOME' },
  { labelKey: 'pages.sys.banner.categories.login', value: 'LOGIN' },
  { labelKey: 'pages.sys.banner.categories.workplace', value: 'WORKPLACE' },
  { labelKey: 'pages.sys.banner.categories.notice', value: 'NOTICE' },
  { labelKey: 'pages.sys.banner.categories.adminDashboard', value: 'ADMIN_DASHBOARD' },
  { labelKey: 'pages.sys.banner.categories.systemUpgrade', value: 'SYSTEM_UPGRADE' },
]

export const typeOptions = [
  { labelKey: 'pages.sys.banner.types.carousel', value: 'CAROUSEL' },
  { labelKey: 'pages.sys.banner.types.hero', value: 'HERO' },
  { labelKey: 'pages.sys.banner.types.notice', value: 'NOTICE' },
  { labelKey: 'pages.sys.banner.types.card', value: 'CARD' },
  { labelKey: 'pages.sys.banner.types.popup', value: 'POPUP' },
  { labelKey: 'pages.sys.banner.types.sidebar', value: 'SIDEBAR' },
]

export const positionOptions = [
  { labelKey: 'pages.sys.banner.positions.homeTop', value: 'HOME_TOP' },
  { labelKey: 'pages.sys.banner.positions.homeMiddle', value: 'HOME_MIDDLE' },
  { labelKey: 'pages.sys.banner.positions.homeBottom', value: 'HOME_BOTTOM' },
  { labelKey: 'pages.sys.banner.positions.loginSide', value: 'LOGIN_SIDE' },
  { labelKey: 'pages.sys.banner.positions.workplaceTop', value: 'WORKPLACE_TOP' },
  { labelKey: 'pages.sys.banner.positions.noticeArea', value: 'NOTICE_AREA' },
  { labelKey: 'pages.sys.banner.positions.adminTop', value: 'ADMIN_TOP' },
  { labelKey: 'pages.sys.banner.positions.adminSidebar', value: 'ADMIN_SIDEBAR' },
]

export const linkTypeOptions = [
  { labelKey: 'pages.sys.banner.linkTypes.url', value: 'URL' },
  { labelKey: 'pages.sys.banner.linkTypes.route', value: 'ROUTE' },
  { labelKey: 'pages.sys.banner.linkTypes.none', value: 'NONE' },
]

export const statusLabelKeyMap: Record<string, string> = Object.fromEntries(
  statusOptions.map((item) => [item.value, item.labelKey]),
)

export const displayScopeLabelKeyMap: Record<string, string> = Object.fromEntries(
  displayScopeOptions.map((item) => [item.value, item.labelKey]),
)

export const categoryLabelKeyMap: Record<string, string> = Object.fromEntries(
  categoryOptions.map((item) => [item.value, item.labelKey]),
)

export const typeLabelKeyMap: Record<string, string> = Object.fromEntries(
  typeOptions.map((item) => [item.value, item.labelKey]),
)

export const positionLabelKeyMap: Record<string, string> = Object.fromEntries(
  positionOptions.map((item) => [item.value, item.labelKey]),
)

export const linkTypeLabelKeyMap: Record<string, string> = Object.fromEntries(
  linkTypeOptions.map((item) => [item.value, item.labelKey]),
)

export const statusTagTypeMap: Record<string, 'success' | 'error' | 'default'> = {
  ENABLED: 'success',
  DISABLED: 'error',
}

export const displayScopeTagTypeMap: Record<string, 'success' | 'info' | 'warning'> = {
  PORTAL: 'success',
  ADMIN: 'info',
  APP: 'warning',
}

export function createEmptyBannerForm() {
  return {
    title: '',
    image: '',
    url: '',
    link_type: 'URL',
    summary: '',
    description: '',
    category: 'HOME',
    type: 'CAROUSEL',
    position: 'HOME_TOP',
    display_scope: 'PORTAL',
    sort: 0,
    status: 'ENABLED',
    start_at: '',
    end_at: '',
  }
}
