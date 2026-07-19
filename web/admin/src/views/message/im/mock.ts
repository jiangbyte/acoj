type MessageSide = 'me' | 'other'
type ThreadKind = 'direct' | 'group'

export interface MockThread {
  id: string
  kind: ThreadKind
  title: string
  subtitle: string
  avatarText: string
  lastMessage: string
  lastMessageAt: string
  unreadCount: number
  pinned: boolean
  muted: boolean
  contactId?: string
  groupId?: string
}

export interface MockFriend {
  id: string
  name: string
  title: string
  department: string
  avatarText: string
  statusText: string
  signature: string
  threadId: string
}

export interface MockGroup {
  id: string
  name: string
  description: string
  memberCount: number
  avatarText: string
  statusText: string
  threadId: string
}

export interface MockDirectoryUser {
  id: string
  name: string
  title: string
  department: string
  avatarText: string
  statusText: string
  signature: string
}

export interface MockDirectoryGroup {
  id: string
  name: string
  description: string
  memberCount: number
  avatarText: string
  statusText: string
}

export interface MockAttachment {
  name: string
  size: number
  type: string
}

export interface MockSystemNotice {
  id: string
  title: string
  content: string
  severity: 'info' | 'warning' | 'error'
  read: boolean
  createdAt: string
}

export interface MockTodoItem {
  id: string
  title: string
  content: string
  priority: 'low' | 'medium' | 'high' | 'urgent'
  status: 'pending' | 'done' | 'cancelled'
  dueAt: string
  createdAt: string
}

export interface MockMessage {
  id: string
  threadId: string
  senderName: string
  senderSide: MessageSide
  content: string
  createdAt: string
  attachments?: MockAttachment[]
}
export type RequestStatus = "pending" | "approved" | "rejected"

export interface MockApplicationRequest {
  id: string
  mode: 'friend' | 'group'
  name: string
  avatarText: string
  subtitle: string
  detail: string
  status: RequestStatus
  createdAt: string
}



export interface MockImData {
  profile: any
  threads: MockThread[]
  friends: MockFriend[]
  groups: MockGroup[]
  directoryUsers: MockDirectoryUser[]
  directoryGroups: MockDirectoryGroup[]
  messagesByThread: Record<string, MockMessage[]>
  notices: MockSystemNotice[]
  requests: MockApplicationRequest[]
  todos: MockTodoItem[]
}

const baseTime = Date.now()

function minutesAgo(offset: number) {
  return new Date(baseTime - offset * 60 * 1000).toISOString()
}

function buildMessages(
  threadId: string,
  lines: Array<{ senderName: string; senderSide: MessageSide; content: string }>,
  startOffset = 220,
  step = 12,
): MockMessage[] {
  return lines.map((item, index) => ({
    id: `${threadId}-${index + 1}`,
    threadId,
    senderName: item.senderName,
    senderSide: item.senderSide,
    content: item.content,
    createdAt: minutesAgo(startOffset - index * step),
  }))
}

export function createMockImData(): MockImData {
  const profile = {}

  const friends: MockFriend[] = [
    {
      id: 'friend-li',
      name: '李雷',
      title: '前端工程师',
      department: '研发中心',
      avatarText: '李',
      statusText: '在线',
      signature: '先把界面做扎实，再补细节。',
      threadId: 'thread-li',
    },
    {
      id: 'friend-chen',
      name: '陈静',
      title: '产品经理',
      department: '产品部',
      avatarText: '陈',
      statusText: '刚刚活跃',
      signature: '需求先收口，再拆页面。',
      threadId: 'thread-chen',
    },
    {
      id: 'friend-zhao',
      name: '赵强',
      title: '测试工程师',
      department: '质量保障部',
      avatarText: '赵',
      statusText: '忙碌',
      signature: '把回归路径先列出来。',
      threadId: 'thread-zhao',
    },
    {
      id: 'friend-wang',
      name: '王敏',
      title: '设计师',
      department: '体验设计部',
      avatarText: '王',
      statusText: '离线',
      signature: '版式和节奏要先对齐。',
      threadId: 'thread-wang',
    },
  ]

  const groups: MockGroup[] = [
    {
      id: 'group-ops',
      name: '运营协同群',
      description: '日常排期、活动和文案同步',
      memberCount: 18,
      avatarText: '运',
      statusText: '活跃',
      threadId: 'thread-ops',
    },
    {
      id: 'group-dev',
      name: '研发协作群',
      description: '版本节奏、接口和联调',
      memberCount: 32,
      avatarText: '研',
      statusText: '活跃',
      threadId: 'thread-dev',
    },
    {
      id: 'group-all',
      name: '全员通知群',
      description: '跨部门通知与公告',
      memberCount: 126,
      avatarText: '全',
      statusText: '只读',
      threadId: 'thread-all',
    },
  ]

  const directoryUsers: MockDirectoryUser[] = [
    {
      id: 'user-xu',
      name: '许航',
      title: '运维工程师',
      department: '平台部',
      avatarText: '许',
      statusText: '在线',
      signature: '可以先从这里发起新会话。',
    },
    {
      id: 'user-sun',
      name: '孙悦',
      title: '客服主管',
      department: '客服部',
      avatarText: '孙',
      statusText: '刚刚活跃',
      signature: '我会先回一下消息。',
    },
    {
      id: 'user-zhou',
      name: '周晨',
      title: '数据分析师',
      department: '数据部',
      avatarText: '周',
      statusText: '忙碌',
      signature: '先把数据口径对齐。',
    },
    {
      id: 'user-zheng',
      name: '郑菲',
      title: '培训负责人',
      department: '人力资源部',
      avatarText: '郑',
      statusText: '离线',
      signature: '有空再一起对页面。',
    },
  ]

  const directoryGroups: MockDirectoryGroup[] = [
    {
      id: 'group-design',
      name: '设计评审群',
      description: '视觉稿、交互稿和评审结论同步',
      memberCount: 14,
      avatarText: '设',
      statusText: '活跃',
    },
    {
      id: 'group-service',
      name: '客户支持群',
      description: '工单、反馈和处理结果',
      memberCount: 58,
      avatarText: '服',
      statusText: '活跃',
    },
    {
      id: 'group-data',
      name: '数据讨论群',
      description: '指标、报表和分析结论',
      memberCount: 24,
      avatarText: '数',
      statusText: '只读',
    },
  ]

  const messagesByThread: Record<string, MockMessage[]> = {
    'thread-li': buildMessages('thread-li', [
      { senderName: '李雷', senderSide: 'other', content: '我把 IM 页面骨架先铺出来了，今晚再收一版。' },
      { senderName: '周岚', senderSide: 'me', content: '好，先把三栏和移动端切换跑通。' },
      { senderName: '李雷', senderSide: 'other', content: '中栏我会做成列表式，消息区单独滚动。' },
      { senderName: '周岚', senderSide: 'me', content: '通讯录先用 mock，后面再接账号接口。' },
      { senderName: '李雷', senderSide: 'other', content: '明白，头像点开个人信息也一并补上。' },
      { senderName: '周岚', senderSide: 'me', content: '移动端要铺满，别保留后台那套窄卡片。' },
      { senderName: '李雷', senderSide: 'other', content: '收到，我会把页面壳子独立出去。' },
      { senderName: '周岚', senderSide: 'me', content: '消息列表需要支持上滑看更早记录。' },
      { senderName: '李雷', senderSide: 'other', content: '我会用本地分页模拟这个交互。' },
      { senderName: '周岚', senderSide: 'me', content: '可以，先把体验做顺，再补后端。' },
    ]),
    'thread-chen': buildMessages('thread-chen', [
      { senderName: '陈静', senderSide: 'other', content: '今日重点是把视觉密度提上去。' },
      { senderName: '周岚', senderSide: 'me', content: '会把右侧消息区做得更像即时通讯。' },
      { senderName: '陈静', senderSide: 'other', content: '中栏列表要能一眼扫出最近会话。' },
      { senderName: '周岚', senderSide: 'me', content: '头像、昵称、摘要、时间都会保留。' },
      { senderName: '陈静', senderSide: 'other', content: '通讯录也别做成后台表格，像联系人列表就行。' },
      { senderName: '周岚', senderSide: 'me', content: '会按好友和群组两块拆开。' },
      { senderName: '陈静', senderSide: 'other', content: '移动端先看单手操作是否顺手。' },
      { senderName: '周岚', senderSide: 'me', content: '会把返回和切换路径做短一些。' },
      { senderName: '陈静', senderSide: 'other', content: '好，先把页面做好。' },
      { senderName: '周岚', senderSide: 'me', content: '收到。' },
    ]),
    'thread-zhao': buildMessages('thread-zhao', [
      { senderName: '赵强', senderSide: 'other', content: '我先帮你看下滚动加载的边界。' },
      { senderName: '周岚', senderSide: 'me', content: '重点是顶部加载时不要把视图顶乱。' },
      { senderName: '赵强', senderSide: 'other', content: '可以把旧消息从上方补进来再回填 scrollTop。' },
      { senderName: '周岚', senderSide: 'me', content: '对，消息不能跳。' },
      { senderName: '赵强', senderSide: 'other', content: '输入框也要固定在底部，别跟着列表滚。' },
      { senderName: '周岚', senderSide: 'me', content: '右侧区域会做成上下分层。' },
      { senderName: '赵强', senderSide: 'other', content: '行，我再看一下小屏交互。' },
      { senderName: '周岚', senderSide: 'me', content: '移动端会做成单页切换，不走后台布局。' },
      { senderName: '赵强', senderSide: 'other', content: '这个方向对。' },
      { senderName: '周岚', senderSide: 'me', content: '那就先按这个版本落。' },
    ]),
    'thread-wang': buildMessages('thread-wang', [
      { senderName: '王敏', senderSide: 'other', content: '我把头像和状态样式整理了一版。' },
      { senderName: '周岚', senderSide: 'me', content: '好，侧栏上只保留必要信息。' },
      { senderName: '王敏', senderSide: 'other', content: '消息气泡颜色不要太跳。' },
      { senderName: '周岚', senderSide: 'me', content: '会用白底和浅灰做主色。' },
      { senderName: '王敏', senderSide: 'other', content: '联系人列表可以加个小签名。' },
      { senderName: '周岚', senderSide: 'me', content: '会加。' },
      { senderName: '王敏', senderSide: 'other', content: '看起来就接近真正的 IM 了。' },
      { senderName: '周岚', senderSide: 'me', content: '先把结构做好，后面再补细节。' },
      { senderName: '王敏', senderSide: 'other', content: '可以。' },
      { senderName: '周岚', senderSide: 'me', content: '我这边继续收口。' },
    ]),
    'thread-ops': buildMessages('thread-ops', [
      { senderName: '运营协同群', senderSide: 'other', content: '今晚 18:00 前同步活动稿件。' },
      { senderName: '周岚', senderSide: 'me', content: '我先确认视觉稿和时间点。' },
      { senderName: '运营协同群', senderSide: 'other', content: '文案版本会放在群文件里。' },
      { senderName: '周岚', senderSide: 'me', content: '收到，IM 页我也会一起看一下群组入口。' },
      { senderName: '运营协同群', senderSide: 'other', content: '中午前再确认一次排期。' },
      { senderName: '周岚', senderSide: 'me', content: '好的。' },
      { senderName: '运营协同群', senderSide: 'other', content: '联系人信息先按 mock 展示即可。' },
      { senderName: '周岚', senderSide: 'me', content: '会先做 UI。' },
      { senderName: '运营协同群', senderSide: 'other', content: '消息区注意留白和密度。' },
      { senderName: '周岚', senderSide: 'me', content: '明白。' },
    ]),
    'thread-dev': buildMessages('thread-dev', [
      { senderName: '研发协作群', senderSide: 'other', content: '接口清单已经同步到群里。' },
      { senderName: '周岚', senderSide: 'me', content: '我先对照 IM 页面字段。' },
      { senderName: '研发协作群', senderSide: 'other', content: '群组列表里可以显示成员数。' },
      { senderName: '周岚', senderSide: 'me', content: '会加在中栏说明里。' },
      { senderName: '研发协作群', senderSide: 'other', content: '消息流顶部要有群标题和状态。' },
      { senderName: '周岚', senderSide: 'me', content: '右侧头部会保留这些信息。' },
      { senderName: '研发协作群', senderSide: 'other', content: '小屏下别把信息塞得太满。' },
      { senderName: '周岚', senderSide: 'me', content: '会做成分步切换。' },
      { senderName: '研发协作群', senderSide: 'other', content: '好。' },
      { senderName: '周岚', senderSide: 'me', content: '先这样。' },
    ]),
    'thread-all': buildMessages('thread-all', [
      { senderName: '全员通知群', senderSide: 'other', content: '今晚平台会做一次小版本发布。' },
      { senderName: '周岚', senderSide: 'me', content: '我会先检查消息中心首页展示。' },
      { senderName: '全员通知群', senderSide: 'other', content: '相关公告会保留在通知页。' },
      { senderName: '周岚', senderSide: 'me', content: 'IM 页会和通知做视觉区分。' },
      { senderName: '全员通知群', senderSide: 'other', content: '如果有问题请直接回复。' },
      { senderName: '周岚', senderSide: 'me', content: '收到。' },
      { senderName: '全员通知群', senderSide: 'other', content: '谢谢配合。' },
      { senderName: '周岚', senderSide: 'me', content: '好的。' },
      { senderName: '全员通知群', senderSide: 'other', content: '群消息会保留在这里。' },
      { senderName: '周岚', senderSide: 'me', content: '明白。' },
    ]),
  }

  const threads: MockThread[] = [
    {
      id: 'thread-li',
      kind: 'direct',
      title: '李雷',
      subtitle: '前端工程师 · 研发中心',
      avatarText: '李',
      lastMessage: '可以，先把体验做顺，再补后端。',
      lastMessageAt: messagesByThread['thread-li'][9].createdAt,
      unreadCount: 2,
      pinned: true,
      muted: false,
      contactId: 'friend-li',
    },
    {
      id: 'thread-chen',
      kind: 'direct',
      title: '陈静',
      subtitle: '产品经理 · 产品部',
      avatarText: '陈',
      lastMessage: '收到。',
      lastMessageAt: messagesByThread['thread-chen'][9].createdAt,
      unreadCount: 1,
      pinned: true,
      muted: false,
      contactId: 'friend-chen',
    },
    {
      id: 'thread-ops',
      kind: 'group',
      title: '运营协同群',
      subtitle: '18 人 · 活跃',
      avatarText: '运',
      lastMessage: '消息区注意留白和密度。',
      lastMessageAt: messagesByThread['thread-ops'][9].createdAt,
      unreadCount: 5,
      pinned: false,
      muted: false,
      groupId: 'group-ops',
    },
    {
      id: 'thread-dev',
      kind: 'group',
      title: '研发协作群',
      subtitle: '32 人 · 活跃',
      avatarText: '研',
      lastMessage: '先这样。',
      lastMessageAt: messagesByThread['thread-dev'][9].createdAt,
      unreadCount: 0,
      pinned: false,
      muted: true,
      groupId: 'group-dev',
    },
    {
      id: 'thread-zhao',
      kind: 'direct',
      title: '赵强',
      subtitle: '测试工程师 · 质量保障部',
      avatarText: '赵',
      lastMessage: '先这样。',
      lastMessageAt: messagesByThread['thread-zhao'][9].createdAt,
      unreadCount: 0,
      pinned: false,
      muted: false,
      contactId: 'friend-zhao',
    },
    {
      id: 'thread-all',
      kind: 'group',
      title: '全员通知群',
      subtitle: '126 人 · 只读',
      avatarText: '全',
      lastMessage: '群消息会保留在这里。',
      lastMessageAt: messagesByThread['thread-all'][9].createdAt,
      unreadCount: 9,
      pinned: false,
      muted: false,
      groupId: 'group-all',
    },
  ]

  const notices: MockSystemNotice[] = [
    {
      id: 'notice-1',
      title: '系统维护通知',
      content: '系统将于今晚 23:00-02:00 进行例行维护，期间部分功能可能暂不可用，请提前保存工作进度。',
      severity: 'warning',
      read: false,
      createdAt: minutesAgo(30),
    },
    {
      id: 'notice-2',
      title: '版本更新 v3.2.0',
      content: '新版本已发布，新增 IM 站内信模块、优化数据看板性能、修复已知问题。',
      severity: 'info',
      read: false,
      createdAt: minutesAgo(180),
    },
    {
      id: 'notice-3',
      title: '安全提醒',
      content: '检测到多次异常登录尝试，请及时修改密码并开启二次验证。',
      severity: 'error',
      read: true,
      createdAt: minutesAgo(1440),
    },
    {
      id: 'notice-4',
      title: '季度总结提交提醒',
      content: '各部门请于本周五前提交 Q2 工作总结至运营中心。',
      severity: 'info',
      read: true,
      createdAt: minutesAgo(2880),
    },
  ]
  const requests: MockApplicationRequest[] = [
    {
      id: 'req-1',
      mode: 'friend',
      name: '周杰伦',
      avatarText: '周',
      subtitle: '市场部 · 渠道经理',
      detail: '你好，我是市场部的周杰伦，想加你为好友方便沟通。',
      status: 'pending',
      createdAt: minutesAgo(60),
    },
    {
      id: 'req-2',
      mode: 'friend',
      name: '林志玲',
      avatarText: '林',
      subtitle: '运营部 · 活动策划',
      detail: '你好，我是运营部的林志玲，想和你学习一下产品设计。',
      status: 'pending',
      createdAt: minutesAgo(120),
    },
  ]


  const todos: MockTodoItem[] = [
    {
      id: 'todo-1',

      title: '审核内容发布申请',
      content: '市场部提交了新品发布文案，需在 48 小时内完成审核。',
      priority: 'high',
      status: 'pending',
      dueAt: minutesAgo(480),
      createdAt: minutesAgo(1440),
    },
    {
      id: 'todo-2',
      title: '确认活动预算',
      content: 'Q3 线下推广活动预算表已提交，请确认后流转财务。',
      priority: 'medium',
      status: 'pending',
      dueAt: minutesAgo(960),
      createdAt: minutesAgo(2880),
    },
    {
      id: 'todo-3',
      title: '完成员工权限复核',
      content: '按安全规范，需每月复核一次核心系统权限分配，本月尚未完成。',
      priority: 'urgent',
      status: 'pending',
      dueAt: minutesAgo(120),
      createdAt: minutesAgo(4320),
    },
    {
      id: 'todo-4',
      title: '回复客户咨询工单',
      content: '客户 #10234 关于 API 接入的技术咨询工单待回复。',
      priority: 'low',
      status: 'pending',
      dueAt: minutesAgo(720),
      createdAt: minutesAgo(5760),
    },
    {
      id: 'todo-5',
      title: '更新操作手册',
      content: 'IM 模块上线后需更新内部操作手册，分配至运营组。',
      priority: 'medium',
      status: 'done',
      dueAt: minutesAgo(0),
      createdAt: minutesAgo(10080),
    },
  ]

  return {
    profile,
    threads,
    friends,
    groups,
    directoryUsers,
    directoryGroups,
    messagesByThread,
    notices,
    todos,
    requests,
  }
}
