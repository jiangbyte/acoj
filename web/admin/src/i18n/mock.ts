import { i18n } from '@/i18n'

const enTextMap: Record<string, string> = {
  系统管理员: 'System Administrator',
  平台管理员: 'Platform Administrator',
  活跃账号: 'Active Accounts',
  待处理授权: 'Pending Grants',
  文件存储: 'File Storage',
  接口健康度: 'API Health',
  稳定: 'Stable',
  IAM权限中心: 'IAM Permission Center',
  'IAM 权限中心': 'IAM Permission Center',
  文件服务治理: 'File Service Governance',
  可观测性接入: 'Observability Integration',
  门户账号体系: 'Portal Account System',
  任务调度面板: 'Task Scheduling Panel',
  审计报表: 'Audit Reports',
  平台管理组: 'Platform Management',
  基础设施组: 'Infrastructure',
  合规审计组: 'Compliance Audit',
  任务平台组: 'Task Platform',
  用户增长组: 'User Growth',
  可观测性组: 'Observability',
  核心权限: 'Core Permissions',
  存储与队列: 'Storage and Queues',
  审计治理: 'Audit Governance',
  异步任务: 'Async Tasks',
  门户用户: 'Portal Users',
  监控链路: 'Monitoring Traces',
  待办事项: 'Todos',
  未读消息: 'Unread Messages',
  协作事项: 'Collaboration',
  风险提醒: 'Risk Alerts',
  '4 项需要今天处理': '4 items need handling today',
  '含 6 条权限提醒': 'Includes 6 permission alerts',
  跨部门流程进行中: 'Cross-department workflows in progress',
  账号与文件审计异常: 'Account and file audit anomalies',
  审批运营部门账号开通申请: 'Approve account requests from Operations',
  复核审计只读角色的数据范围: 'Review data scope for audit read-only role',
  确认文件服务治理巡检结果: 'Confirm file service governance check result',
  补充资源管理接口权限说明: 'Complete resource API permission notes',
  账号管理: 'Accounts',
  角色授权: 'Role Grants',
  文件管理: 'Files',
  资源管理: 'Resources',
  运营负责人: 'Operations Owner',
  '今天 11:30': 'Today 11:30',
  '今天 16:00': 'Today 16:00',
  '明天 10:00': 'Tomorrow 10:00',
  '已超时 2 小时': 'Overdue by 2 hours',
  新建锁定授权账号: 'Create, lock, and grant accounts',
  '新建、锁定、授权账号': 'Create, lock, and grant accounts',
  维护角色与数据范围: 'Maintain roles and data scopes',
  维护菜单与权限点: 'Maintain menus and permission points',
  查看上传与存储状态: 'View uploads and storage status',
  个人中心: 'Profile',
  查看个人资料与岗位: 'View profile and job information',
  分析页: 'Analysis',
  查看访问趋势与热度: 'View access trends and popularity',
  权限资源同步完成: 'Permission resources synced',
  '身份权限模块已完成最新资源树同步，请关注新增接口的授权范围。':
    'Identity permissions have synced the latest resource tree. Review grant scopes for new APIs.',
  账号安全提醒: 'Account Security Alert',
  '检测到2个账号连续登录失败，请及时复核账号状态。': 'Detected repeated login failures on 2 accounts. Review account status promptly.',
  '检测到 2 个账号连续登录失败，请及时复核账号状态。':
    'Detected repeated login failures on 2 accounts. Review account status promptly.',
  文件审计报表已生成: 'File audit report generated',
  '昨日文件访问审计报表已生成，可在文件管理中查看。': 'Yesterday file access audit report is ready in File Management.',
  '10 分钟前': '10 minutes ago',
  '38 分钟前': '38 minutes ago',
  '1 小时前': '1 hour ago',
  权限变更评审: 'Permission Change Review',
  账号治理周会: 'Account Governance Weekly',
  文件审计结果确认: 'File Audit Result Confirmation',
  已完成: 'Done',
  待开始: 'Not Started',
  身份权限: 'Identity & Access',
  文件服务: 'File Service',
  登录认证: 'Login Auth',
  部门管理: 'Departments',
  审计只读角色存在跨部门数据范围: 'Audit read-only role has cross-department data scope',
  '影响 2 个部门': 'Impacts 2 departments',
  文件服务高频下载缺少二次确认: 'High-frequency file downloads lack second confirmation',
  '近 24 小时 18 次': '18 times in the last 24 hours',
  资源管理新增接口未绑定权限点: 'New resource APIs are not bound to permission points',
  '3 个接口': '3 APIs',
  锁定账号未完成归因复核: 'Locked accounts still need root-cause review',
  '2 个账号': '2 accounts',
}

function localizeString(value: string) {
  return i18n.global.locale.value === 'en-US' ? enTextMap[value] || value : value
}

export function localizeMockData<T>(data: T): T {
  if (Array.isArray(data)) {
    return data.map((item) => localizeMockData(item)) as T
  }
  if (data && typeof data === 'object') {
    return Object.fromEntries(
      Object.entries(data).map(([key, value]) => [key, localizeMockData(value)]),
    ) as T
  }
  return typeof data === 'string' ? (localizeString(data) as T) : data
}
