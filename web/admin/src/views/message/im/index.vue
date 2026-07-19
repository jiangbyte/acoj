<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useThemeVars } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useAuthStore } from '@/stores'
import { formatDateTime } from '@/utils'
import { renderIcon } from '@/utils/icon'
import {
  createMockImData,
  type MockDirectoryGroup,
  type MockDirectoryUser,
  type MockAttachment,
  type MockFriend,
  type MockGroup,
  type MockMessage,
  type MockThread,
  type MockSystemNotice,
  type MockTodoItem,
} from './mock'

type AddMode = 'chat' | 'friend' | 'group'
type RequestStatus = 'pending' | 'accepted' | 'rejected'
type RequestOrigin = 'incoming' | 'outgoing'

interface MockApplicationRequest {
  id: string
  mode: AddMode
  origin: RequestOrigin
  status: RequestStatus
  targetKind: 'user' | 'group'
  targetId: string
  name: string
  subtitle: string
  avatarText: string
  detail: string
  createdAt: string
}

const appStore = useAppStore()
const themeVars = useThemeVars()
const router = useRouter()
const route = useRoute()
const homePath = import.meta.env.VITE_HOME_PATH || '/dashboard'
const data = reactive(createMockImData())

const activeSection = ref<'chat' | 'contacts' | 'notice' | 'todos' | 'profile'>('chat')
const contactTab = ref<'friends' | 'groups'>('friends')
const noticeTab = ref<'requests' | 'notices'>('notices')
const todoTab = ref<'pending' | 'done'>('pending')
const searchScope = ref<'threads' | 'users' | 'groups'>('threads')
const mobileView = ref<'list' | 'chat' | 'detail'>('list')
const searchText = ref('')
const skipDefaultThread = ref(false)
const showProfileModal = ref(false)
const showThreadDrawer = ref(false)
const selectedPendingRequest = ref<MockApplicationRequest | null>(null)
const selectedNotice = ref<MockSystemNotice | null>(null)
const selectedTodo = ref<MockTodoItem | null>(null)
const showPendingDetail = ref(false)
const showAddModal = ref(false)
const showCreateGroupModal = ref(false)
const addMode = ref<AddMode>('friend')
const addSearchText = ref('')
const selectedAttachments = ref<MockAttachment[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const contactActionHint = ref('')
const createGroupName = ref('')
const createGroupDesc = ref('')
const createGroupPublic = ref(true)
const createGroupAvatarText = ref('群')
const createGroupInvitees = ref<string[]>([])
const showInviteFriendModal = ref(false)
const inviteSearchText = ref('')

const filteredInviteFriends = computed(() => {
  const keyword = inviteSearchText.value.trim().toLowerCase()
  if (!keyword) return data.friends
  return data.friends.filter(f => f.name.toLowerCase().includes(keyword) || f.title?.toLowerCase().includes(keyword))
})

function handleGroupAvatarClick() {
  const chars = data.friends.map(f => f.avatarText).filter(Boolean)
  const all = [...new Set(['群', ...chars, '组', '聊', '天', '新'])]
  const current = createGroupAvatarText.value
  const idx = all.indexOf(current)
  createGroupAvatarText.value = all[(idx + 1) % all.length]
}

function toggleGroupInvitee(friendId: string) {
  const idx = createGroupInvitees.value.indexOf(friendId)
  if (idx >= 0) {
    createGroupInvitees.value.splice(idx, 1)
  } else {
    createGroupInvitees.value.push(friendId)
  }
}

function removeGroupInvitee(friendId: string) {
  const idx = createGroupInvitees.value.indexOf(friendId)
  if (idx >= 0) {
    createGroupInvitees.value.splice(idx, 1)
  }
}

function closeCreateGroup() {
  showCreateGroupModal.value = false
  createGroupName.value = ''
  createGroupDesc.value = ''
  createGroupInvitees.value = []
  createGroupAvatarText.value = '群'
  inviteSearchText.value = ''
}
const composerText = ref('')
const selectedThreadId = ref('')
const selectedContact = ref<{ kind: 'friend' | 'group'; id: string } | null>(null)
const messageListRef = ref<HTMLElement | null>(null)
const requestList = reactive<MockApplicationRequest[]>([
  {
    id: 'req-in-friend-1',
    mode: 'friend',
    origin: 'incoming',
    status: 'pending',
    targetKind: 'user',
    targetId: 'incoming-user-1',
    name: '黄珊',
    subtitle: '活动策划 · 品牌部',
    avatarText: '黄',
    detail: '想先和你建立好友关系，方便后续对接。',
    createdAt: new Date(Date.now() - 1000 * 60 * 42).toISOString(),
  },
  {
    id: 'req-in-group-1',
    mode: 'group',
    origin: 'incoming',
    status: 'pending',
    targetKind: 'group',
    targetId: 'incoming-group-1',
    name: '增长讨论群',
    subtitle: '12 人 · 活跃',
    avatarText: '增',
    detail: '群主已发起邀请，等待你确认入群。',
    createdAt: new Date(Date.now() - 1000 * 60 * 18).toISOString(),
  },
])
const messageState = reactive({
  visibleStart: 0,
  visibleMessages: [] as MockMessage[],
  loadingOlder: false,
})

const isMobile = computed(() => appStore.isMobile)
const selectedThread = computed(() =>
  data.threads.find((thread) => thread.id === selectedThreadId.value) ?? null,
)
const selectedFriend = computed(() => {
  const contactId = selectedThread.value?.contactId
  return contactId ? data.friends.find((friend) => friend.id === contactId) ?? null : null
})
const selectedGroup = computed(() => {
  const groupId = selectedThread.value?.groupId
  return groupId ? data.groups.find((group) => group.id === groupId) ?? null : null
})
const normalizedSearch = computed(() => searchText.value.trim().toLowerCase())
const hasSearchKeyword = computed(() => normalizedSearch.value.length > 0)

const profile = data.profile

const sortedThreads = computed(() =>
  [...data.threads].sort((left, right) => {
    if (left.pinned !== right.pinned) {
      return Number(right.pinned) - Number(left.pinned)
    }
    return new Date(right.lastMessageAt).getTime() - new Date(left.lastMessageAt).getTime()
  }),
)

const filteredThreads = computed(() => {
  const keyword = normalizedSearch.value
  if (!keyword) {
    return sortedThreads.value
  }
  return sortedThreads.value.filter((thread) =>
    [thread.title, thread.subtitle, thread.lastMessage]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
})

const filteredFriends = computed(() => {
  const keyword = normalizedSearch.value
  const friends = [...data.friends]
  if (!keyword) {
    return friends
  }
  return friends.filter((friend) =>
    [friend.name, friend.title, friend.department, friend.signature]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
})

const filteredGroups = computed(() => {
  const keyword = normalizedSearch.value
  const groups = [...data.groups]
  if (!keyword) {
    return groups
  }
  return groups.filter((group) =>
    [group.name, group.description, group.statusText]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
})

const filteredSearchThreads = computed(() => filteredThreads.value)
const filteredSearchFriends = computed(() => filteredFriends.value)
const filteredSearchGroups = computed(() => filteredGroups.value)

const visibleMessages = computed(() => messageState.visibleMessages)
const hasMoreOlder = computed(() => messageState.visibleStart > 0)
const totalUnreadCount = computed(() =>
  data.threads.reduce((sum, thread) => sum + (thread.unreadCount || 0), 0),
)

const currentContactEmpty = computed(() =>
  contactTab.value === 'friends' ? '暂无好友' : '暂无群组',
)
const selectedFriendContact = computed(() => {
  if (selectedContact.value?.kind !== 'friend') {
    return null
  }
  return data.friends.find((friend) => friend.id === selectedContact.value?.id) ?? null
})
const selectedGroupContact = computed(() => {
  if (selectedContact.value?.kind !== 'group') {
    return null
  }
  return data.groups.find((group) => group.id === selectedContact.value?.id) ?? null
})
const selectedContactTitle = computed(() => selectedFriendContact.value?.name ?? selectedGroupContact.value?.name ?? '')
const selectedContactSubtitle = computed(() =>
  selectedFriendContact.value
    ? `${selectedFriendContact.value.title} · ${selectedFriendContact.value.department}`
    : selectedGroupContact.value
      ? `${selectedGroupContact.value.memberCount} 人 · ${selectedGroupContact.value.statusText}`
      : '',
)
const selectedContactEmpty = computed(() =>
  activeSection.value === 'contacts' ? '请选择一个好友或群组查看详情' : '请选择联系人',
)
const normalizedAddSearch = computed(() => addSearchText.value.trim().toLowerCase())
const addSearchLabel = computed(() => {
  if (addMode.value === 'chat') return '搜索用户或群组'
  if (addMode.value === 'friend') return '搜索用户'
  return '搜索群组'
})
const addModalTitle = computed(() => {
  if (addMode.value === 'chat') return '发起对话'
  if (addMode.value === 'friend') return '添加好友'
  return '申请入群'
})
const showListPane = computed(() => activeSection.value !== 'profile' && (!isMobile.value || mobileView.value === 'list'))
const showChatPane = computed(() => activeSection.value === 'chat' && (!isMobile.value || mobileView.value === 'chat'))
const showNoticeDetailPane = computed(
  () => (activeSection.value === 'notice' || activeSection.value === 'todos') && showPendingDetail.value,
)

const showContactDetailPane = computed(
  () => activeSection.value === 'contacts' && (!isMobile.value || mobileView.value === 'detail'),
)
const showProfilePane = computed(
  () => isMobile.value && activeSection.value === 'profile',
)
const showTodosPane = computed(
  () => activeSection.value === 'todos',
)
const addActions = [
  { label: '添加好友', key: 'friend', icon: renderIcon('icon-park-outline:people') },
  { label: '添加群聊', key: 'group', icon: renderIcon('icon-park-outline:group') },
  { label: '创建群聊', key: 'create-group', icon: renderIcon('icon-park-outline:add-one') },
]

const mobileActions = [
  { label: '添加好友', key: 'friend', icon: renderIcon('icon-park-outline:people') },
  { label: '添加群聊', key: 'group', icon: renderIcon('icon-park-outline:group') },
  { label: '创建群聊', key: 'create-group', icon: renderIcon('icon-park-outline:add-one') },
  { type: 'divider' },
  { label: '返回工作台', key: 'home', icon: renderIcon('icon-park-outline:arrow-left') },
]

const addChatUsers = computed(() => filterDirectoryUsers())
const addChatGroups = computed(() => filterDirectoryGroups())
const addFriendUsers = computed(() => filterDirectoryUsers())
const addGroupResults = computed(() => filterDirectoryGroups())
const pendingRequests = computed(() => requestList.filter((item) => item.status === 'pending'))
const handledRequests = computed(() => requestList.filter((item) => item.status !== 'pending'))
const requestBadgeCount = computed(() => pendingRequests.value.length)
const unreadNoticeCount = computed(() => data.notices.filter((n) => !n.read).length)
const pendingTodoCount = computed(() => data.todos.filter((t) => t.status === 'pending').length)

const filteredTodos = computed(() => {
  if (todoTab.value === 'pending') {
    return data.todos.filter((t) => t.status === 'pending')
  }
  return data.todos.filter((t) => t.status !== 'pending')
})
const noticeBadgeTotal = computed(() => requestBadgeCount.value + unreadNoticeCount.value)
const addModeCount = computed(() => {
  if (addMode.value === 'chat') {
    return addChatUsers.value.length + addChatGroups.value.length
  }
  if (addMode.value === 'friend') {
    return addFriendUsers.value.length
  }
  return addGroupResults.value.length
})

const threadDrawerWidth = computed(() => (isMobile.value ? '100vw' : 360))
const rootBackgroundColor = computed(() => themeVars.value.bodyColor)
const pageStyle = computed(() => ({
  backgroundColor: rootBackgroundColor.value,
  color: themeVars.value.textColorBase,
  height: '100dvh',
  minHeight: '100vh',
}))
const previousHtmlBackgroundColor = document.documentElement.style.backgroundColor
const previousBodyBackgroundColor = document.body.style.backgroundColor

watch(
  () => route.query.thread_id,
  (threadId) => {
    const id = typeof threadId === 'string' ? threadId : ''
    if (id) {
      if (id !== selectedThreadId.value && data.threads.some((thread) => thread.id === id)) {
        openThread(id, false, true)
      }
      return
    }

    if (!selectedThreadId.value && data.threads[0] && !skipDefaultThread.value) {
      openThread(data.threads[0].id, true, !isMobile.value)
    }
    skipDefaultThread.value = false
  },
  { immediate: true },
)

onMounted(() => {
  if (isMobile.value) {
    mobileView.value =
      activeSection.value === 'contacts' && selectedContact.value
        ? 'detail'
        : selectedThreadId.value
          ? 'chat'
          : 'list'
  }
})

onBeforeUnmount(() => {
  document.documentElement.style.backgroundColor = previousHtmlBackgroundColor
  document.body.style.backgroundColor = previousBodyBackgroundColor
})

watch(
  rootBackgroundColor,
  (backgroundColor) => {
    document.documentElement.style.backgroundColor = backgroundColor
    document.body.style.backgroundColor = backgroundColor
  },
  { immediate: true },
)

watch(isMobile, (value) => {
  mobileView.value = value
    ? activeSection.value === 'contacts' && selectedContact.value
      ? 'detail'
      : selectedThreadId.value
        ? 'chat'
        : 'list'
    : activeSection.value === 'chat' && selectedThreadId.value
      ? 'chat'
      : 'list'
})

function goHome() {
  router.push(homePath)
}

function openProfileModal() {
  showProfileModal.value = true
}

function goProfileCenter() {
  showProfileModal.value = false
  router.push('/usercenter')
}

function handleLogout() {
  const authStore = useAuthStore()
  authStore.logout()
}

function openThreadDrawer() {
  showThreadDrawer.value = true
}

function openChatSection() {
  activeSection.value = 'chat'
  searchScope.value = 'threads'
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function openContactsSection() {
  activeSection.value = 'contacts'
  searchScope.value = 'users'
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function openNoticeSection() {
  activeSection.value = 'notice'
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function openProfileSection() {
  activeSection.value = 'profile'
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function openTodosSection() {
  activeSection.value = 'todos'
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function openFriend(friend: MockFriend) {
  contactActionHint.value = ''
  selectedContact.value = { kind: 'friend', id: friend.id }
  activeSection.value = 'contacts'
  if (isMobile.value) {
    mobileView.value = 'detail'
  }
}

function openGroup(group: MockGroup) {
  contactActionHint.value = ''
  selectedContact.value = { kind: 'group', id: group.id }
  activeSection.value = 'contacts'
  if (isMobile.value) {
    mobileView.value = 'detail'
  }
}

function openThread(threadId: string, syncRoute = true, openChat = true) {
  const thread = data.threads.find((item) => item.id === threadId)
  if (!thread) {
    return
  }

  selectedThreadId.value = threadId
  activeSection.value = 'chat'
  searchScope.value = 'threads'
  selectedContact.value = null
  contactActionHint.value = ''
  if (isMobile.value && openChat) {
    mobileView.value = 'chat'
  }

  syncVisibleMessages(threadId)
  markThreadRead(threadId)

  if (syncRoute && route.query.thread_id !== threadId) {
    router.replace({
      path: route.path,
      query: {
        ...route.query,
        thread_id: threadId,
      },
    })
  }

  void nextTick(() => scrollMessagesToBottom())
}

function syncVisibleMessages(threadId: string) {
  const history = data.messagesByThread[threadId] ?? []
  messageState.visibleStart = Math.max(0, history.length - 6)
  messageState.visibleMessages = history.slice(messageState.visibleStart)
}

function markThreadRead(threadId: string) {
  const thread = data.threads.find((item) => item.id === threadId)
  if (thread) {
    thread.unreadCount = 0
  }
}

async function loadOlderMessages() {
  const threadId = selectedThreadId.value
  if (!threadId || messageState.loadingOlder || !hasMoreOlder.value) {
    return
  }

  const history = data.messagesByThread[threadId] ?? []
  const currentStart = messageState.visibleStart
  const nextStart = Math.max(0, currentStart - 6)
  const previousNode = messageListRef.value
  const previousHeight = previousNode?.scrollHeight ?? 0
  const previousTop = previousNode?.scrollTop ?? 0

  messageState.loadingOlder = true
  messageState.visibleStart = nextStart
  messageState.visibleMessages = [
    ...history.slice(nextStart, currentStart),
    ...messageState.visibleMessages,
  ]

  await nextTick()
  if (previousNode) {
    previousNode.scrollTop = previousNode.scrollHeight - previousHeight + previousTop
  }
  messageState.loadingOlder = false
}

function handleMessageScroll(event: Event) {
  const target = event.currentTarget as HTMLElement
  if (target.scrollTop <= 24) {
    void loadOlderMessages()
  }
}

function scrollMessagesToBottom() {
  const target = messageListRef.value
  if (target) {
    target.scrollTop = target.scrollHeight
  }
}

function backToListPane() {
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function handleAddAction(key: string | number) {
  if (key === 'chat') {
    openAddModal('chat')
    return
  }

  if (key === 'friend') {
    openAddModal('friend')
    return
  }

  if (key === 'group') {
    openAddModal('group')
    return
  }

  if (key === 'create-group') {
    showCreateGroupModal.value = true
  }
}

function handleMobileAction(key: string | number) {
  if (key === 'home') {
    goHome()
    return
  }
  handleAddAction(key)
}

function openAddModal(mode: AddMode) {
  addMode.value = mode
  addSearchText.value = ''
  showAddModal.value = true
}

function closeAddModal() {
  showAddModal.value = false
}

function handleCreateGroup() {
  const name = createGroupName.value.trim()
  if (!name) return

  const groupId = `group-created-${Date.now()}`
  const threadId = `thread-${groupId}`
  const avatarText = createGroupAvatarText.value || name.charAt(0)
  const memberCount = 1 + createGroupInvitees.value.length
  const invitedNames = createGroupInvitees.value
    .map((id) => data.friends.find((f) => f.id === id)?.name)
    .filter(Boolean)

  data.groups.unshift({
    id: groupId,
    name,
    description: createGroupDesc.value.trim() || '新创建的群组',
    memberCount,
    avatarText,
    statusText: '活跃',
    threadId,
  })

  data.threads.unshift({
    id: threadId,
    kind: 'group',
    title: name,
    subtitle: `${memberCount} 人 · 活跃`,
    avatarText,
    lastMessage: '群聊已创建',
    lastMessageAt: new Date().toISOString(),
    unreadCount: 0,
    pinned: false,
    muted: false,
    groupId,
  })

  data.messagesByThread[threadId] = []

  showCreateGroupModal.value = false
  showInviteFriendModal.value = false
  createGroupName.value = ''
  createGroupDesc.value = ''
  createGroupInvitees.value = []
  createGroupAvatarText.value = '群'
  inviteSearchText.value = ''

  if (invitedNames.length) {
    const msg: MockMessage = {
      id: `${threadId}-sys-${Date.now()}`,
      threadId,
      senderName: '系统',
      senderSide: 'other',
      content: `邀请 ${invitedNames.join('、')} 加入了群聊`,
      createdAt: new Date().toISOString(),
    }
    data.messagesByThread[threadId].push(msg)
  }

  openThread(threadId)
}

function filterDirectoryUsers() {
  const keyword = normalizedAddSearch.value
  const users = [...data.directoryUsers]
  if (!keyword) {
    return users
  }
  return users.filter((user) =>
    [user.name, user.title, user.department, user.signature]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
}

function filterDirectoryGroups() {
  const keyword = normalizedAddSearch.value
  const groups = [...data.directoryGroups]
  if (!keyword) {
    return groups
  }
  return groups.filter((group) =>
    [group.name, group.description, group.statusText]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
}

function getFriendApplicationStatus(user: MockDirectoryUser) {
  if (data.friends.some((friend) => friend.name === user.name)) {
    return 'accepted'
  }
  if (
    requestList.some(
      (item) =>
        item.origin === 'outgoing' &&
        item.targetKind === 'user' &&
        item.targetId === user.id &&
        item.status === 'pending',
    )
  ) {
    return 'pending'
  }
  return 'idle'
}

function getGroupApplicationStatus(group: MockDirectoryGroup) {
  if (data.groups.some((item) => item.name === group.name)) {
    return 'accepted'
  }
  if (
    requestList.some(
      (item) =>
        item.origin === 'outgoing' &&
        item.targetKind === 'group' &&
        item.targetId === group.id &&
        item.status === 'pending',
    )
  ) {
    return 'pending'
  }
  return 'idle'
}

function createThreadForUser(user: MockDirectoryUser) {
  const threadId = `thread-${user.id}`
  const existing = data.threads.find((thread) => thread.id === threadId)
  if (existing) {
    return existing
  }

  const createdAt = new Date().toISOString()
  const thread: MockThread = {
    id: threadId,
    kind: 'direct',
    title: user.name,
    subtitle: `${user.title} · ${user.department}`,
    avatarText: user.avatarText,
    lastMessage: '已发起对话',
    lastMessageAt: createdAt,
    unreadCount: 0,
    pinned: false,
    muted: false,
  }

  data.threads.unshift(thread)
  data.messagesByThread[threadId] = []
  return thread
}

function createThreadForGroup(group: MockDirectoryGroup) {
  const threadId = `thread-${group.id}`
  const existing = data.threads.find((thread) => thread.id === threadId)
  if (existing) {
    return existing
  }

  const createdAt = new Date().toISOString()
  const thread: MockThread = {
    id: threadId,
    kind: 'group',
    title: group.name,
    subtitle: `${group.memberCount} 人 · ${group.statusText}`,
    avatarText: group.avatarText,
    lastMessage: '已发起对话',
    lastMessageAt: createdAt,
    unreadCount: 0,
    pinned: false,
    muted: false,
  }

  data.threads.unshift(thread)
  data.messagesByThread[threadId] = []
  return thread
}

function applyForFriend(user: MockDirectoryUser) {
  const exists = requestList.some(
    (item) => item.targetKind === 'user' && item.targetId === user.id && item.origin === 'outgoing' && item.status === 'pending',
  )
  if (exists) {
    contactActionHint.value = `已提交好友申请：${user.name}`
    return
  }

  requestList.unshift({
    id: `req-out-friend-${user.id}-${Date.now()}`,
    mode: 'friend',
    origin: 'outgoing',
    status: 'pending',
    targetKind: 'user',
    targetId: user.id,
    name: user.name,
    subtitle: `${user.title} · ${user.department}`,
    avatarText: user.avatarText,
    detail: '好友申请已发送，等待对方确认。',
    createdAt: new Date().toISOString(),
  })
  contactActionHint.value = `已提交好友申请：${user.name}`
}

function applyForGroup(group: MockDirectoryGroup) {
  const exists = requestList.some(
    (item) => item.targetKind === 'group' && item.targetId === group.id && item.origin === 'outgoing' && item.status === 'pending',
  )
  if (exists) {
    contactActionHint.value = `已提交入群申请：${group.name}`
    return
  }

  requestList.unshift({
    id: `req-out-group-${group.id}-${Date.now()}`,
    mode: 'group',
    origin: 'outgoing',
    status: 'pending',
    targetKind: 'group',
    targetId: group.id,
    name: group.name,
    subtitle: `${group.memberCount} 人 · ${group.statusText}`,
    avatarText: group.avatarText,
    detail: '入群申请已发送，等待管理员确认。',
    createdAt: new Date().toISOString(),
  })
  contactActionHint.value = `已提交入群申请：${group.name}`
}

function startConversationFromUser(user: MockDirectoryUser) {
  const thread = createThreadForUser(user)
  closeAddModal()
  openThread(thread.id)
}

function startConversationFromGroup(group: MockDirectoryGroup) {
  const thread = createThreadForGroup(group)
  closeAddModal()
  openThread(thread.id)
}

function handleAddFileButtonClick() {
  fileInputRef.value?.click()
}

function handleFileInputChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (!files.length) {
    return
  }

  selectedAttachments.value = [
    ...selectedAttachments.value,
    ...files.map((file) => ({
      name: file.name,
      size: file.size,
      type: file.type || 'application/octet-stream',
    })),
  ]

  input.value = ''
}

function removeAttachment(index: number) {
  selectedAttachments.value = selectedAttachments.value.filter((_, currentIndex) => currentIndex !== index)
}

function formatFileSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function sendSelectedAttachments() {
  const threadId = selectedThreadId.value
  if (!threadId || !selectedAttachments.value.length) {
    return
  }

  const history = data.messagesByThread[threadId] ?? []
  const attachments = selectedAttachments.value.map((item) => ({ ...item }))
  const message: MockMessage = {
    id: `${threadId}-file-${Date.now()}`,
    threadId,
    senderName: profile.nickname,
    senderSide: 'me',
    content: `发送了 ${attachments.length} 个文件`,
    createdAt: new Date().toISOString(),
    attachments,
  }

  history.push(message)
  data.messagesByThread[threadId] = history

  const thread = data.threads.find((item) => item.id === threadId)
  if (thread) {
    thread.lastMessage = message.content
    thread.lastMessageAt = message.createdAt
  }

  selectedAttachments.value = []
  syncVisibleMessages(threadId)
  void nextTick(() => scrollMessagesToBottom())
}

function acceptRequest(request: MockApplicationRequest) {
  request.status = 'accepted'
  if (request.targetKind === 'user') {
    const existingFriend = data.friends.find((friend) => friend.name === request.name)
    if (!existingFriend) {
      const friendId = `friend-${request.targetId}`
      data.friends.unshift({
        id: friendId,
        name: request.name,
        title: request.subtitle.split(' · ')[0] || '',
        department: request.subtitle.split(' · ')[1] || '',
        avatarText: request.avatarText,
        statusText: '在线',
        signature: request.detail,
        threadId: `thread-${request.targetId}`,
      })
      const threadId = `thread-${request.targetId}`
      data.threads.unshift({
        id: threadId,
        kind: 'direct',
        title: request.name,
        subtitle: request.subtitle,
        avatarText: request.avatarText,
        lastMessage: '好友申请已通过',
        lastMessageAt: new Date().toISOString(),
        unreadCount: 0,
        pinned: false,
        muted: false,
        contactId: friendId,
      })
      data.messagesByThread[threadId] = []
    }
  } else {
    const existingGroup = data.groups.find((group) => group.name === request.name)
    if (!existingGroup) {
      const groupId = `group-${request.targetId}`
      const memberCount = Number(request.subtitle.split(' 人')[0]) || 0
      data.groups.unshift({
        id: groupId,
        name: request.name,
        description: request.detail,
        memberCount,
        avatarText: request.avatarText,
        statusText: '活跃',
        threadId: `thread-${request.targetId}`,
      })
      const threadId = `thread-${request.targetId}`
      data.threads.unshift({
        id: threadId,
        kind: 'group',
        title: request.name,
        subtitle: request.subtitle,
        avatarText: request.avatarText,
        lastMessage: '入群申请已通过',
        lastMessageAt: new Date().toISOString(),
        unreadCount: 0,
        pinned: false,
        muted: false,
        groupId,
      })
      data.messagesByThread[threadId] = []
    }
  }

  contactActionHint.value = `已接受申请：${request.name}`
}

function rejectRequest(request: MockApplicationRequest) {
  request.status = 'rejected'
  contactActionHint.value = `已拒绝申请：${request.name}`
  closePendingDetail()
}

function openNoticeDetail(notice: MockSystemNotice) {
  notice.read = true
  selectedNotice.value = notice
  selectedTodo.value = null
  selectedPendingRequest.value = null
  showPendingDetail.value = true
  if (isMobile.value) {
    mobileView.value = 'detail'
  }
}

function openTodoDetail(todo: MockTodoItem) {
  selectedTodo.value = todo
  selectedNotice.value = null
  selectedPendingRequest.value = null
  showPendingDetail.value = true
  if (isMobile.value) {
    mobileView.value = 'detail'
  }
}

function markTodoDone(todo: MockTodoItem) {
  todo.status = 'done'
  closePendingDetail()
}

function markTodoCancelled(todo: MockTodoItem) {
  todo.status = 'cancelled'
  closePendingDetail()
}

function getSeverityType(severity: string) {
  if (severity === 'error') return 'error'
  if (severity === 'warning') return 'warning'
  return 'info'
}

function getPriorityType(priority: string) {
  if (priority === 'urgent') return 'error'
  if (priority === 'high') return 'warning'
  if (priority === 'medium') return 'info'
  return 'default'
}

function getPriorityLabel(priority: string) {
  if (priority === 'urgent') return '紧急'
  if (priority === 'high') return '高'
  if (priority === 'medium') return '中'
  return '低'
}

function getTodoStatusLabel(status: string) {
  if (status === 'pending') return '待处理'
  if (status === 'done') return '已完成'
  return '已取消'
}

function openPendingDetail(request: MockApplicationRequest) {
  selectedPendingRequest.value = request
  showPendingDetail.value = true
  activeSection.value = 'notice'
  if (isMobile.value) {
    mobileView.value = 'detail'
  }
}

function closePendingDetail() {
  showPendingDetail.value = false
  selectedPendingRequest.value = null
  if (isMobile.value) {
    mobileView.value = 'list'
  }
}

function acceptPendingRequest() {
  if (selectedPendingRequest.value) {
    acceptRequest(selectedPendingRequest.value)
    closePendingDetail()
  }
}

function rejectPendingRequest() {
  if (selectedPendingRequest.value) {
    rejectRequest(selectedPendingRequest.value)
    closePendingDetail()
  }
}

function continueChatFromContact() {
  if (selectedFriendContact.value) {
    openThread(selectedFriendContact.value.threadId)
    return
  }

  if (selectedGroupContact.value) {
    openThread(selectedGroupContact.value.threadId)
  }
}

function removeFriendMock() {
  if (!selectedFriendContact.value) {
    return
  }

  contactActionHint.value = `已模拟删除好友：${selectedFriendContact.value.name}`
}

function leaveGroupMock() {
  if (!selectedGroupContact.value) {
    return
  }

  contactActionHint.value = `已模拟退出群聊：${selectedGroupContact.value.name}`
}

function getSectionButtonStyle(active: boolean) {
  return active
    ? {
        backgroundColor: themeVars.value.buttonColor2Hover,
        color: themeVars.value.textColorBase,
      }
    : undefined
}

function sendMessage() {
  const threadId = selectedThreadId.value
  const content = composerText.value.trim()
  const attachments = selectedAttachments.value.map((item) => ({ ...item }))
  if (!threadId || (!content && !attachments.length)) {
    return
  }

  const history = data.messagesByThread[threadId] ?? []
  const message: MockMessage = {
    id: `${threadId}-local-${Date.now()}`,
    threadId,
    senderName: profile.nickname,
    senderSide: 'me',
    content,
    createdAt: new Date().toISOString(),
    attachments: attachments.length ? attachments : undefined,
  }

  history.push(message)
  data.messagesByThread[threadId] = history

  const thread = data.threads.find((item) => item.id === threadId)
  if (thread) {
    thread.lastMessage = content || `发送了 ${attachments.length} 个文件`
    thread.lastMessageAt = message.createdAt
  }

  composerText.value = ''
  selectedAttachments.value = []
  syncVisibleMessages(threadId)
  void nextTick(() => scrollMessagesToBottom())
}

function isActiveThread(threadId: string) {
  return selectedThreadId.value === threadId
}

function isActiveContact(kind: 'friend' | 'group', id: string) {
  return selectedContact.value?.kind === kind && selectedContact.value?.id === id
}

function selectThreadFromList(threadId: string) {
  openThread(threadId)
}

function closeCurrentThread() {
  skipDefaultThread.value = true
  selectedThreadId.value = ''
  selectedContact.value = null
  if (isMobile.value) {
    mobileView.value = 'list'
  }
  void router.replace({
    path: route.path,
    query: { ...route.query, thread_id: undefined },
  })
}

function getThreadLatestMessageText(threadId: string) {
  const history = data.messagesByThread[threadId] ?? []
  const latestMessage = history[history.length - 1]
  if (!latestMessage) {
    const thread = data.threads.find((item) => item.id === threadId)
    return thread?.lastMessage ?? ''
  }
  return `${latestMessage.senderName}：${latestMessage.content}`
}

function getThreadItemStyle(isActive: boolean) {
  return isActive
    ? {
        backgroundColor: themeVars.value.buttonColor2Hover,
      }
    : undefined
}

function getContactItemStyle(isActive: boolean) {
  return isActive
    ? {
        backgroundColor: themeVars.value.buttonColor2Hover,
      }
    : undefined
}

function getMessageBubbleStyle(isMine: boolean) {
  return isMine
    ? {
        backgroundColor: themeVars.value.primaryColor,
        border: `1px solid ${themeVars.value.primaryColor}`,
        color: '#ffffff',
      }
    : {
        backgroundColor: themeVars.value.cardColor,
        border: `1px solid ${themeVars.value.borderColor}`,
        color: themeVars.value.textColor1,
      }
}

</script>

<template>
  <n-el tag="main" class="fixed inset-0 flex flex-col overflow-hidden" :style="pageStyle">



    <div class="grid min-h-0 flex-1 gap-0 md:grid-cols-[60px_minmax(280px,360px)_minmax(0,1fr)]">
      <aside
        class="hidden h-full min-h-0 flex-col items-center bg-[#2b2b2b] py-3 md:flex dark:bg-[#1a1a1a]"
      >
        <NAvatar
          round
          :size="40"
          class="shrink-0 cursor-pointer"
          @click="openProfileModal"
        >
          {{ profile.avatarText }}
        </NAvatar>

        <div class="mt-6 flex flex-1 flex-col items-center gap-6">
          <NTooltip placement="right">
            <template #trigger>
              <NBadge :value="totalUnreadCount" :max="99" :show-zero="false">
                <NButton
                  text
                  :class="activeSection === 'chat' ? 'text-[var(--primary-color)]' : 'text-white'"
                  aria-label="聊天"
                  @click="openChatSection"
                >
                  <template #icon>
                    <NovaIcon icon="icon-park-outline:message" :size="22" />
                  </template>
                </NButton>
              </NBadge>
            </template>
            聊天
          </NTooltip>

          <NTooltip placement="right">
            <template #trigger>
              <NButton
                text
                :class="activeSection === 'contacts' ? 'text-[var(--primary-color)]' : 'text-white'"
                aria-label="通讯录"
                @click="openContactsSection"
              >
                <template #icon>
                  <NovaIcon icon="icon-park-outline:people" :size="22" />
                </template>
              </NButton>
            </template>
            通讯录
          </NTooltip>

          <NTooltip placement="right">
            <template #trigger>
              <NBadge :value="pendingTodoCount" :max="99" :show-zero="false">
                <NButton
                  text
                  :class="activeSection === 'todos' ? 'text-[var(--primary-color)]' : 'text-white'"
                  aria-label="待办"
                  @click="openTodosSection"
                >
                  <template #icon>
                    <NovaIcon icon="icon-park-outline:checklist" :size="22" />
                  </template>
                </NButton>
              </NBadge>
            </template>
            待办
          </NTooltip>

          <NTooltip placement="right">
            <template #trigger>
              <NBadge :value="noticeBadgeTotal" :max="99" :show-zero="false">
                <NButton
                  text
                  :class="activeSection === 'notice' ? 'text-[var(--primary-color)]' : 'text-white'"
                  aria-label="通知"
                  @click="openNoticeSection"
                >
                  <template #icon>
                    <NovaIcon icon="icon-park-outline:alarm" :size="22" />
                  </template>
                </NButton>
              </NBadge>
            </template>
            通知
          </NTooltip>
        </div>

        <div class="mt-auto">
          <NTooltip placement="right">
            <template #trigger>
              <NButton text class="text-white" aria-label="返回工作台" @click="goHome">
                <template #icon>
                  <NovaIcon icon="icon-park-outline:arrow-left" :size="20" />
                </template>
              </NButton>
            </template>
            返回工作台
          </NTooltip>
        </div>
      </aside>

      <NCard
        v-show="showListPane"
        :bordered="false"
        class="h-full min-h-0 overflow-hidden shadow-sm"
        :content-style="{ height: '100%', padding: '0' }"
      >
        <div class="flex h-full min-h-0 flex-col">
          <div v-show="activeSection === 'chat' || activeSection === 'contacts'" class="border-b border-[var(--border-color)] px-4 py-3">
            <NInputGroup>
              <NInputGroupLabel class="text-[var(--text-color-3)]">
                <NovaIcon icon="icon-park-outline:search" :size="16" />
              </NInputGroupLabel>
              <NInput
                v-model:value="searchText"
                clearable
                placeholder="搜索群组 / 用户 / 对话"
              />
              <NDropdown trigger="click" :options="addActions" @select="handleAddAction">
                <NButton quaternary aria-label="添加">
                  <template #icon>
                    <NovaIcon icon="icon-park-outline:add" :size="16" />
                  </template>
                </NButton>
              </NDropdown>
            </NInputGroup>

            <NTabs
              v-if="hasSearchKeyword"
              v-model:value="searchScope"
              type="segment"
              size="small"
              class="mt-3"
            >
              <NTabPane name="threads" tab="对话" />
              <NTabPane name="users" tab="用户" />
              <NTabPane name="groups" tab="群组" />
            </NTabs>
          </div>

          <div v-if="hasSearchKeyword" class="flex min-h-0 flex-1 flex-col">
            <NScrollbar class="h-full">
              <NList v-if="searchScope === 'threads' && filteredSearchThreads.length" hoverable clickable>
                <NListItem
                  v-for="thread in filteredSearchThreads"
                  :key="thread.id"
                  class="im-list-item cursor-pointer"
                  :style="getThreadItemStyle(isActiveThread(thread.id))"
                  @click="selectThreadFromList(thread.id)"
                >
                  <div class="im-list-row flex items-start gap-3 px-4 py-3">
                    <NBadge class="shrink-0" :value="thread.unreadCount" :max="99" :show-zero="false">
                      <NAvatar round :size="40" class="shrink-0">
                        {{ thread.avatarText }}
                      </NAvatar>
                    </NBadge>

                    <div class="im-list-body">
                      <div class="im-list-main-line flex items-center justify-between gap-3">
                        <span class="im-ellipsis flex-1 text-sm font-600">
                          {{ thread.title }}
                        </span>
                        <span class="shrink-0 text-xs text-[var(--text-color-3)]">
                          {{ formatDateTime(thread.lastMessageAt) }}
                        </span>
                      </div>
                      <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">
                        {{ getThreadLatestMessageText(thread.id) }}
                      </span>
                    </div>
                  </div>
                </NListItem>
              </NList>
              <NEmpty v-else-if="searchScope === 'threads'" class="py-12" description="暂无对话结果" />

              <NList v-else-if="searchScope === 'users' && filteredSearchFriends.length" hoverable clickable>
                <NListItem
                  v-for="friend in filteredSearchFriends"
                  :key="friend.id"
                  class="im-list-item cursor-pointer"
                  :style="getContactItemStyle(isActiveContact('friend', friend.id))"
                  @click="openFriend(friend)"
                >
                  <div class="im-list-row flex items-start gap-3 px-4 py-3">
                    <NAvatar round :size="40" class="shrink-0">
                      {{ friend.avatarText }}
                    </NAvatar>
                    <div class="im-list-body">
                      <div class="im-list-main-line flex items-center justify-between gap-3">
                        <span class="im-ellipsis flex-1 text-sm font-600">
                          {{ friend.name }}
                        </span>
                        <span class="shrink-0 text-xs text-[var(--text-color-3)]">
                          {{ friend.statusText }}
                        </span>
                      </div>
                      <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">
                        {{ friend.title }} · {{ friend.department }}
                      </span>
                    </div>
                  </div>
                </NListItem>
              </NList>
              <NEmpty v-else-if="searchScope === 'users'" class="py-12" description="暂无用户结果" />

              <NList v-else-if="searchScope === 'groups' && filteredSearchGroups.length" hoverable clickable>
                <NListItem
                  v-for="group in filteredSearchGroups"
                  :key="group.id"
                  class="im-list-item cursor-pointer"
                  :style="getContactItemStyle(isActiveContact('group', group.id))"
                  @click="openGroup(group)"
                >
                  <div class="im-list-row flex items-start gap-3 px-4 py-3">
                    <NAvatar round :size="40" class="shrink-0">
                      {{ group.avatarText }}
                    </NAvatar>
                    <div class="im-list-body">
                      <div class="im-list-main-line flex items-center justify-between gap-3">
                        <span class="im-ellipsis flex-1 text-sm font-600">
                          {{ group.name }}
                        </span>
                        <span class="shrink-0 text-xs text-[var(--text-color-3)]">
                          {{ group.statusText }}
                        </span>
                      </div>
                      <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">
                        {{ group.memberCount }} 人 · {{ group.description }}
                      </span>
                    </div>
                  </div>
                </NListItem>
              </NList>
              <NEmpty v-else class="py-12" description="暂无群组结果" />
            </NScrollbar>
          </div>

          <div v-else-if="activeSection === 'chat'" class="flex min-h-0 flex-1 flex-col">
            <NScrollbar class="h-full">
              <NList v-if="filteredThreads.length" hoverable clickable>
                <NListItem
                  v-for="thread in filteredThreads"
                  :key="thread.id"
                  class="im-list-item cursor-pointer"
                  :style="getThreadItemStyle(isActiveThread(thread.id))"
                  @click="selectThreadFromList(thread.id)"
                >
                  <div class="im-list-row flex items-start gap-3 px-4 py-3">
                    <NBadge class="shrink-0" :value="thread.unreadCount" :max="99" :show-zero="false">
                      <NAvatar round :size="40" class="shrink-0">
                        {{ thread.avatarText }}
                      </NAvatar>
                    </NBadge>

                    <div class="im-list-body">
                      <div class="im-list-main-line flex items-center justify-between gap-3">
                        <span class="im-ellipsis flex-1 text-sm font-600">
                          {{ thread.title }}
                        </span>
                        <span class="shrink-0 text-xs text-[var(--text-color-3)]">
                          {{ formatDateTime(thread.lastMessageAt) }}
                        </span>
                      </div>
                      <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">
                        {{ getThreadLatestMessageText(thread.id) }}
                      </span>
                    </div>
                  </div>
                </NListItem>
              </NList>
              <NEmpty v-else class="py-12" description="暂无会话" />
            </NScrollbar>
          </div>

          <div v-else-if="activeSection === 'notice'" class="flex min-h-0 flex-1 flex-col">
            <NTabs v-model:value="noticeTab" type="segment" size="small" class="px-4 pt-3">
              <NTabPane name="notices" :tab="`通知 ${unreadNoticeCount ? `(${unreadNoticeCount})` : ''}`">
                <NScrollbar class="h-full">
                  <NList v-if="data.notices.length" hoverable>
                    <NListItem
                      v-for="notice in data.notices"
                      :key="notice.id"
                      class="im-list-item cursor-pointer"
                      @click="openNoticeDetail(notice)"
                    >
                      <div class="flex items-start gap-3 px-4 py-3">
                        <NAvatar round :size="40" class="shrink-0" :style="{ backgroundColor: notice.severity === 'error' ? 'var(--error-color)' : notice.severity === 'warning' ? 'var(--warning-color)' : 'var(--info-color)' }">
                          {{ notice.severity === 'error' ? '!' : notice.severity === 'warning' ? '!' : 'i' }}
                        </NAvatar>
                        <div class="min-w-0 flex-1">
                          <div class="flex items-start justify-between gap-3">
                            <div class="min-w-0 flex items-center gap-2">
                              <span class="im-ellipsis text-sm font-600" :class="{ 'font-700': !notice.read }">{{ notice.title }}</span>
                              <NTag v-if="!notice.read" :bordered="false" size="tiny" type="primary">新</NTag>
                            </div>
                            <span class="shrink-0 text-xs text-[var(--text-color-3)]">{{ formatDateTime(notice.createdAt) }}</span>
                          </div>
                          <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ notice.content }}</div>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" description="暂无通知" />
                </NScrollbar>
              </NTabPane>
                          <NTabPane name="requests" :tab="`申请 ${requestBadgeCount ? `(${requestBadgeCount})` : ''}`">
                <NScrollbar class="h-full">
                  <NList v-if="pendingRequests.length" hoverable>
                    <NListItem
                      v-for="request in pendingRequests"
                      :key="request.id"
                      class="im-list-item cursor-pointer"
                      @click="openPendingDetail(request)"
                    >
                      <div class="flex items-start gap-3 px-4 py-3">
                        <NAvatar round :size="40" class="shrink-0">
                          {{ request.avatarText }}
                        </NAvatar>
                        <div class="min-w-0 flex-1">
                          <div class="flex items-start justify-between gap-3">
                            <div class="min-w-0">
                              <div class="im-ellipsis text-sm font-600">{{ request.name }}</div>
                              <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ request.subtitle }}</div>
                            </div>
                            <NTag :bordered="false" size="small" type="warning">
                              {{ request.mode === 'friend' ? '好友申请' : '入群申请' }}
                            </NTag>
                          </div>
                          <div class="mt-2 text-xs text-[var(--text-color-3)]">{{ request.detail }}</div>
                          <div class="mt-2 text-xs text-[var(--text-color-3)]">{{ formatDateTime(request.createdAt) }}</div>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" description="暂无待处理申请" />
                </NScrollbar>
              </NTabPane>
</NTabs>
          </div>

          <div v-else-if="activeSection === 'todos'" class="flex min-h-0 flex-1 flex-col">
            <NTabs v-model:value="todoTab" type="segment" size="small" class="px-4 pt-3">
              <NTabPane name="pending" :tab="`待处理 ${pendingTodoCount ? `(${pendingTodoCount})` : ''}`">
                <NScrollbar class="h-full">
                  <NList v-if="filteredTodos.filter(t => t.status === 'pending').length" hoverable>
                    <NListItem
                      v-for="todo in filteredTodos"
                      :key="todo.id"
                      class="im-list-item cursor-pointer"
                      @click="openTodoDetail(todo)"
                    >
                      <div class="flex items-start gap-3 px-4 py-3">
                        <div class="shrink-0 mt-1">
                          <NSwitch :value="false" size="small" @click.stop="markTodoDone(todo)" />
                        </div>
                        <div class="min-w-0 flex-1">
                          <div class="flex items-start justify-between gap-3">
                            <div class="min-w-0 flex items-center gap-2">
                              <span class="im-ellipsis text-sm font-600">{{ todo.title }}</span>
                              <NTag :bordered="false" size="tiny" :type="getPriorityType(todo.priority) as any">{{ getPriorityLabel(todo.priority) }}</NTag>
                            </div>
                            <span class="shrink-0 text-xs text-[var(--text-color-3)]">{{ formatDateTime(todo.createdAt) }}</span>
                          </div>
                          <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ todo.content }}</div>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" description="暂无待处理待办" />
                </NScrollbar>
              </NTabPane>
              <NTabPane name="done" :tab="`已处理`">
                <NScrollbar class="h-full">
                  <NList v-if="filteredTodos.filter(t => t.status !== 'pending').length" hoverable>
                    <NListItem
                      v-for="todo in filteredTodos"
                      :key="todo.id"
                      class="im-list-item cursor-pointer"
                      :style="{ opacity: 0.6 }"
                      @click="openTodoDetail(todo)"
                    >
                      <div class="flex items-start gap-3 px-4 py-3">
                        <div class="shrink-0 mt-1">
                          <NCheckbox :checked="todo.status === 'done'" disabled size="small" />
                        </div>
                        <div class="min-w-0 flex-1">
                          <div class="flex items-start justify-between gap-3">
                            <div class="min-w-0 flex items-center gap-2">
                              <span class="im-ellipsis text-sm line-through">{{ todo.title }}</span>
                              <NTag :bordered="false" size="tiny" :type="todo.status === 'done' ? 'success' : 'default'">{{ getTodoStatusLabel(todo.status) }}</NTag>
                            </div>
                            <span class="shrink-0 text-xs text-[var(--text-color-3)]">{{ formatDateTime(todo.createdAt) }}</span>
                          </div>
                          <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ todo.content }}</div>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" description="暂无已处理待办" />
                </NScrollbar>
              </NTabPane>
            </NTabs>
          </div>

          <div v-else-if="activeSection === 'contacts'" class="flex min-h-0 flex-1 flex-col">
            <NTabs v-model:value="contactTab" type="segment" size="small" class="px-4 pt-3">
              <NTabPane name="friends" tab="好友">
                <NScrollbar class="h-full">
                  <NList v-if="filteredFriends.length" hoverable clickable>
                    <NListItem v-for="friend in filteredFriends" :key="friend.id" class="im-list-item cursor-pointer"
                      :style="getContactItemStyle(isActiveContact('friend', friend.id))" @click="openFriend(friend)">
                      <div class="im-list-row flex items-start gap-3 px-4 py-3">
                        <NAvatar round :size="40" class="shrink-0">{{ friend.avatarText }}</NAvatar>
                        <div class="im-list-body">
                          <div class="im-list-main-line flex items-center justify-between gap-3">
                            <span class="im-ellipsis flex-1 text-sm font-600">{{ friend.name }}</span>
                            <span class="shrink-0 text-xs text-[var(--text-color-3)]">{{ friend.statusText }}</span>
                          </div>
                          <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ friend.title }} · {{ friend.department }}</span>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" :description="currentContactEmpty" />
                </NScrollbar>
              </NTabPane>
              <NTabPane name="groups" tab="群组">
                <NScrollbar class="h-full">
                  <NList v-if="filteredGroups.length" hoverable clickable>
                    <NListItem v-for="group in filteredGroups" :key="group.id" class="im-list-item cursor-pointer"
                      :style="getContactItemStyle(isActiveContact('group', group.id))" @click="openGroup(group)">
                      <div class="im-list-row flex items-start gap-3 px-4 py-3">
                        <NAvatar round :size="40" class="shrink-0">{{ group.avatarText }}</NAvatar>
                        <div class="im-list-body">
                          <div class="im-list-main-line flex items-center justify-between gap-3">
                            <span class="im-ellipsis flex-1 text-sm font-600">{{ group.name }}</span>
                            <span class="shrink-0 text-xs text-[var(--text-color-3)]">{{ group.statusText }}</span>
                          </div>
                          <span class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ group.memberCount }} 人 · {{ group.description }}</span>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                  <NEmpty v-else class="py-12" :description="currentContactEmpty" />
                </NScrollbar>
              </NTabPane>
            </NTabs>
          </div>
        </div>
      </NCard>

      <NCard v-show="showChatPane" :bordered="false" class="h-full min-h-0 overflow-hidden shadow-sm" :content-style="{ height: '100%', padding: '0' }">
        <template v-if="selectedThread">
          <div class="flex h-full min-h-0 flex-col">
            <div class="flex items-center justify-between gap-3 border-b border-[var(--border-color)] px-4 py-3">
              <div class="flex min-w-0 items-center gap-3 overflow-hidden">
                <NButton v-if="isMobile" text size="small" @click="backToListPane">
                  <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
                </NButton>
                <NAvatar round :size="42" class="shrink-0">{{ selectedThread.avatarText }}</NAvatar>
                <NThing :title="selectedThread.title" :description="selectedThread.subtitle" />
              </div>
              <NFlex :size="4">
                <NButton text size="small" aria-label="更多" @click="openThreadDrawer">
                  <template #icon><NovaIcon icon="icon-park-outline:more" :size="18" /></template>
                </NButton>
                <NButton text size="small" aria-label="关闭会话" @click="closeCurrentThread">
                  <template #icon><NovaIcon icon="icon-park-outline:close" :size="18" /></template>
                </NButton>
              </NFlex>
            </div>

            <div class="flex min-h-0 flex-1 flex-col">
              <div v-if="hasMoreOlder" class="border-b border-[var(--border-color)] px-4 py-2 text-center">
                <NButton text size="small" :loading="messageState.loadingOlder" @click="loadOlderMessages">上滑加载更早消息</NButton>
              </div>
              <div ref="messageListRef" class="flex min-h-0 flex-1 flex-col gap-3 overflow-auto px-4 py-4" @scroll.passive="handleMessageScroll">
                <div v-if="visibleMessages.length" class="flex flex-col gap-3">
                  <div v-for="message in visibleMessages" :key="message.id" class="flex items-start gap-2"
                    :class="message.senderSide === 'me' ? 'flex-row-reverse' : ''">
                    <NAvatar round :size="28" class="shrink-0">
                      {{ message.senderSide === 'me' ? profile.avatarText : selectedThread.avatarText }}
                    </NAvatar>
                    <div class="min-w-0 max-w-[min(68%,640px)]">
                      <div class="mb-1 flex gap-2 text-xs text-[var(--text-color-3)]"
                        :class="message.senderSide === 'me' ? 'justify-end' : 'justify-start'">
                        <span>{{ message.senderName }}</span>
                        <span>{{ formatDateTime(message.createdAt) }}</span>
                      </div>
                      <div class="rounded-2 px-3 py-2 text-sm leading-6" :style="getMessageBubbleStyle(message.senderSide === 'me')">
                        <div class="break-words">{{ message.content }}</div>
                        <div v-if="message.attachments?.length" class="mt-2 flex flex-col gap-2">
                          <div v-for="attachment in message.attachments" :key="attachment.name"
                            class="flex items-center gap-3 rounded-1 border px-3 py-2"
                            :class="message.senderSide === 'me' ? 'border-white/20 bg-white/10' : 'border-[var(--border-color)] bg-[var(--body-color)]'">
                            <NovaIcon icon="icon-park-outline:file" :size="16" />
                            <div class="min-w-0 flex-1">
                              <div class="im-ellipsis text-xs font-600">{{ attachment.name }}</div>
                              <div class="mt-0.5 text-[10px] opacity-80">{{ formatFileSize(attachment.size) }}</div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <NEmpty v-else class="py-12" description="暂无消息" />
              </div>

              <div class="border-t border-[var(--border-color)] p-4">
                <input ref="fileInputRef" type="file" multiple class="hidden" @change="handleFileInputChange" />
                <div v-if="selectedAttachments.length" class="mb-3 flex flex-wrap gap-2">
                  <NTag v-for="(attachment, index) in selectedAttachments" :key="`${attachment.name}-${index}`"
                    closable :bordered="false" @close="removeAttachment(index)">
                    <template #icon><NovaIcon icon="icon-park-outline:file" :size="14" /></template>
                    {{ attachment.name }}
                  </NTag>
                </div>
                <NInput v-model:value="composerText" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"
                  placeholder="输入消息，Enter 发送，Shift + Enter 换行" @keydown.enter.exact.prevent="sendMessage" />
                <div class="mt-3 flex items-center justify-between gap-3 text-xs text-[var(--text-color-3)]">
                  <div class="flex items-center gap-2">
                    <NButton quaternary size="small" aria-label="发送文件" @click="handleAddFileButtonClick">
                      <template #icon><NovaIcon icon="icon-park-outline:folder-upload" :size="16" /></template>
                    </NButton>
                  </div>
                  <NButton type="primary" :disabled="!composerText.trim() && !selectedAttachments.length" @click="sendMessage">发送</NButton>
                </div>
              </div>
            </div>
          </div>
        </template>
        <NEmpty v-else class=" h-full flex justify-center items-center" description="请选择会话"  />
      </NCard>

      <NCard v-show="showNoticeDetailPane" :bordered="false" class="h-full min-h-0 overflow-hidden shadow-sm" :content-style="{ height: '100%', padding: '0' }">
        <!-- 好友/入群申请详情 -->
        <template v-if="selectedPendingRequest">
          <div class="flex h-full min-h-0 flex-col">
            <NScrollbar class="h-full">
              <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
                <div v-if="isMobile" class="flex justify-start">
                  <NButton text size="small" @click="closePendingDetail">
                    <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
                  </NButton>
                </div>
                <div class="flex items-center gap-3">
                  <NAvatar round :size="64" class="shrink-0">{{ selectedPendingRequest.avatarText }}</NAvatar>
                  <div class="min-w-0 text-left">
                    <div class="truncate text-lg font-600">{{ selectedPendingRequest.name }}</div>
                    <div class="truncate text-xs text-[var(--text-color-3)]">{{ selectedPendingRequest.subtitle }}</div>
                  </div>
                </div>
                <NDescriptions :column="1" label-placement="left" size="small">
                  <NDescriptionsItem label="类型">
                    {{ selectedPendingRequest.mode === 'friend' ? '好友申请' : '入群申请' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="说明">
                    {{ selectedPendingRequest.detail }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="时间">
                    {{ formatDateTime(selectedPendingRequest.createdAt) }}
                  </NDescriptionsItem>
                </NDescriptions>
                <NFlex justify="center" :wrap="true" :size="12">
                  <NButton type="primary" @click="acceptPendingRequest">通过</NButton>
                  <NButton tertiary type="error" @click="rejectPendingRequest">拒绝</NButton>
                </NFlex>
              </div>
            </NScrollbar>
          </div>
        </template>
        <!-- 系统通知详情 -->
        <template v-else-if="selectedNotice">
          <div class="flex h-full min-h-0 flex-col">
            <NScrollbar class="h-full">
              <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
                <div v-if="isMobile" class="flex justify-start">
                  <NButton text size="small" @click="closePendingDetail">
                    <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
                  </NButton>
                </div>
                <div class="flex items-center gap-3">
                  <NAvatar round :size="64" class="shrink-0" :style="{ backgroundColor: selectedNotice.severity === 'error' ? 'var(--error-color)' : selectedNotice.severity === 'warning' ? 'var(--warning-color)' : 'var(--primary-color)' }">
                    {{ selectedNotice.severity === 'error' ? '!' : selectedNotice.severity === 'warning' ? '!' : 'i' }}
                  </NAvatar>
                  <div class="min-w-0 text-left">
                    <div class="truncate text-lg font-600">{{ selectedNotice.title }}</div>
                    <div class="truncate text-xs text-[var(--text-color-3)]">{{ formatDateTime(selectedNotice.createdAt) }}</div>
                  </div>
                </div>
                <NAlert v-if="selectedNotice.severity === 'error'" type="error" :bordered="false">严重通知</NAlert>
                <NAlert v-else-if="selectedNotice.severity === 'warning'" type="warning" :bordered="false">重要通知</NAlert>
                <div class="rounded-1 border border-[var(--border-color)] bg-[var(--card-color)] px-4 py-4 text-sm leading-7 whitespace-pre-wrap">
                  {{ selectedNotice.content }}
                </div>
                <NFlex justify="center" :wrap="true" :size="12">
                  <NButton @click="closePendingDetail">关闭</NButton>
                </NFlex>
              </div>
            </NScrollbar>
          </div>
        </template>
        <!-- 待办详情 -->
        <template v-else-if="selectedTodo">
          <div class="flex h-full min-h-0 flex-col">
            <NScrollbar class="h-full">
              <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
                <div v-if="isMobile" class="flex justify-start">
                  <NButton text size="small" @click="closePendingDetail">
                    <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
                  </NButton>
                </div>
                <div class="flex items-center gap-3">
                  <div class="shrink-0 flex items-center justify-center w-[64px] h-[64px] rounded-full" :style="{ backgroundColor: selectedTodo.priority === 'urgent' ? 'var(--error-color)' : selectedTodo.priority === 'high' ? 'var(--warning-color)' : 'var(--primary-color)', color: '#fff', fontSize: '20px' }">
                    <NovaIcon :icon="selectedTodo.status === 'done' ? 'icon-park-outline:check-one' : 'icon-park-outline:file-text'" :size="28" />
                  </div>
                  <div class="min-w-0 text-left">
                    <div class="truncate text-lg font-600">{{ selectedTodo.title }}</div>
                    <div class="truncate text-xs text-[var(--text-color-3)]">{{ getTodoStatusLabel(selectedTodo.status) }} · {{ formatDateTime(selectedTodo.createdAt) }}</div>
                  </div>
                </div>
                <NDescriptions :column="1" label-placement="left" size="small">
                  <NDescriptionsItem label="优先级">
                    <NTag :bordered="false" size="small" :type="getPriorityType(selectedTodo.priority) as any">{{ getPriorityLabel(selectedTodo.priority) }}</NTag>
                  </NDescriptionsItem>
                  <NDescriptionsItem label="截止时间">{{ formatDateTime(selectedTodo.dueAt) }}</NDescriptionsItem>
                  <NDescriptionsItem label="状态">
                    <NTag :bordered="false" size="small" :type="selectedTodo.status === 'done' ? 'success' : selectedTodo.status === 'cancelled' ? 'default' : 'warning'">{{ getTodoStatusLabel(selectedTodo.status) }}</NTag>
                  </NDescriptionsItem>
                </NDescriptions>
                <div class="rounded-1 border border-[var(--border-color)] bg-[var(--card-color)] px-4 py-4 text-sm leading-7 whitespace-pre-wrap">
                  {{ selectedTodo.content }}
                </div>
                <NFlex v-if="selectedTodo.status === 'pending'" justify="center" :wrap="true" :size="12">
                  <NButton type="primary" @click="markTodoDone(selectedTodo)">标记完成</NButton>
                  <NButton tertiary @click="markTodoCancelled(selectedTodo)">取消</NButton>
                </NFlex>
                <NFlex v-else justify="center" :wrap="true" :size="12">
                  <NButton @click="closePendingDetail">关闭</NButton>
                </NFlex>
              </div>
            </NScrollbar>
          </div>
        </template>
        <NEmpty v-else class="grid h-full place-items-center" description="请选择通知项查看" />
      </NCard>

      <NCard v-show="showContactDetailPane" :bordered="false" class="h-full min-h-0 overflow-hidden shadow-sm" :content-style="{ height: '100%', padding: '0' }">
        <template v-if="selectedFriendContact || selectedGroupContact">
          <div class="flex h-full min-h-0 flex-col">
            <NScrollbar class="h-full">
              <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
                <div v-if="isMobile" class="flex justify-start">
                  <NButton text size="small" @click="backToListPane">
                    <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
                  </NButton>
                </div>
                <NAlert v-if="contactActionHint" type="success" :bordered="false">{{ contactActionHint }}</NAlert>
                <div class="flex items-center gap-3">
                  <NAvatar round :size="64" class="shrink-0">{{ selectedFriendContact?.avatarText ?? selectedGroupContact?.avatarText }}</NAvatar>
                  <div class="min-w-0 text-left">
                    <div class="truncate text-lg font-600">{{ selectedContactTitle }}</div>
                    <div class="truncate text-xs text-[var(--text-color-3)]">{{ selectedContactSubtitle }}</div>
                  </div>
                </div>
                <NDescriptions :column="1" label-placement="left" size="small">
                  <template v-if="selectedFriendContact">
                    <NDescriptionsItem label="职位">{{ selectedFriendContact.title }}</NDescriptionsItem>
                    <NDescriptionsItem label="部门">{{ selectedFriendContact.department }}</NDescriptionsItem>
                    <NDescriptionsItem label="签名">{{ selectedFriendContact.signature }}</NDescriptionsItem>
                  </template>
                  <template v-else-if="selectedGroupContact">
                    <NDescriptionsItem label="成员">{{ selectedGroupContact.memberCount }} 人</NDescriptionsItem>
                    <NDescriptionsItem label="说明">{{ selectedGroupContact.description }}</NDescriptionsItem>
                    <NDescriptionsItem label="状态">{{ selectedGroupContact.statusText }}</NDescriptionsItem>
                  </template>
                </NDescriptions>
                <div class="rounded-1 border border-[var(--border-color)] bg-[var(--card-color)] px-3 py-3 text-sm leading-6">
                  <template v-if="selectedFriendContact">{{ selectedFriendContact.signature }}</template>
                  <template v-else>{{ selectedGroupContact?.description }}</template>
                </div>
                <NFlex justify="center" :wrap="true" :size="12">
                  <NButton type="primary" @click="continueChatFromContact">继续聊天</NButton>
                  <NButton v-if="selectedFriendContact" tertiary type="error" @click="removeFriendMock">删除好友</NButton>
                  <NButton v-else tertiary type="error" @click="leaveGroupMock">退出群聊</NButton>
                </NFlex>
              </div>
            </NScrollbar>
          </div>
        </template>
        <NEmpty v-else class=" h-full flex items-center justify-center" :description="selectedContactEmpty" />
      </NCard>

      <div v-show="showProfilePane" class="flex h-full min-h-0 flex-col md:hidden">
        <NScrollbar class="h-full">
          <div class="flex flex-col">
            <div class="flex flex-col items-center bg-[var(--primary-color)] px-4 pb-8 pt-10 text-white">
              <NAvatar round :size="80" class="shrink-0 border-2 border-white/30 shadow-lg">{{ profile.avatarText }}</NAvatar>
              <div class="mt-3 text-lg font-600">{{ profile.nickname }}</div>
              <div class="mt-0.5 text-sm text-white/70">{{ profile.title }} · {{ profile.department }}</div>
            </div>
            <div class="flex flex-col gap-0 bg-[var(--card-color)] px-4 py-3">
              <div class="flex items-center justify-between border-b border-[var(--border-color)] py-4">
                <span class="text-sm text-[var(--text-color-3)]">手机</span>
                <span class="text-sm">{{ profile.phone }}</span>
              </div>
              <div class="flex items-center justify-between border-b border-[var(--border-color)] py-4">
                <span class="text-sm text-[var(--text-color-3)]">邮箱</span>
                <span class="text-sm">{{ profile.email }}</span>
              </div>
              <div class="flex items-center justify-between border-b border-[var(--border-color)] py-4">
                <span class="text-sm text-[var(--text-color-3)]">角色</span>
                <span class="text-sm">{{ profile.role }}</span>
              </div>
              <div class="flex items-start justify-between gap-4 py-4">
                <span class="shrink-0 text-sm text-[var(--text-color-3)]">签名</span>
                <span class="text-right text-sm">{{ profile.signature }}</span>
              </div>
            </div>
            <div class="flex flex-col gap-3 px-4 pt-6 pb-8">
              <NButton type="primary" block @click="goProfileCenter">个人中心</NButton>
              <NButton quaternary block type="error" @click="handleLogout">退出登录</NButton>
            </div>
          </div>
        </NScrollbar>
      </div>
    </div>

    <NModal v-model:show="showProfileModal" preset="card" :bordered="false"
      title="个人信息"
      style="width: min(460px, calc(100vw - 32px));"
    >
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-3">
          <NAvatar round :size="56" class="shrink-0">{{ profile.avatarText }}</NAvatar>
          <div class="min-w-0">
            <div class="text-base font-600">{{ profile.nickname }}</div>
            <div class="mt-1 text-xs text-[var(--text-color-3)]">{{ profile.statusText }}</div>
          </div>
        </div>
        <NDescriptions :column="1" label-placement="left" size="small">
          <NDescriptionsItem label="账号">{{ profile.account }}</NDescriptionsItem>
          <NDescriptionsItem label="标题">{{ profile.title }}</NDescriptionsItem>
          <NDescriptionsItem label="部门">{{ profile.department }}</NDescriptionsItem>
          <NDescriptionsItem label="角色">{{ profile.role }}</NDescriptionsItem>
          <NDescriptionsItem label="联系方式">{{ profile.phone }} / {{ profile.email }}</NDescriptionsItem>
        </NDescriptions>
        <NDivider class="!my-0" />
        <div class="rounded-1 border border-[var(--border-color)] bg-[var(--card-color)] px-3 py-3 text-sm leading-6">
          {{ profile.signature }}
        </div>
        <NDivider class="!my-0" />
        <NFlex justify="center" :wrap="true" :size="12">
          <NButton type="primary" @click="goProfileCenter">个人中心</NButton>
          <NButton tertiary type="error" @click="handleLogout">退出登录</NButton>
        </NFlex>
      </div>
    </NModal>

    <NModal v-model:show="showAddModal" preset="card" :bordered="false" draggable
      title="添加好友 / 群聊" :mask-closable="false"
      style="width: min(700px, calc(100vw - 24px)); height: 75vh;"
      content-style="display: flex; flex-direction: column; height: 65vh; padding: 0 20px 20px">
      <div class="flex min-h-0 flex-1 flex-col gap-4">
        <NTabs v-model:value="addMode" type="segment" size="small">
          <NTabPane name="friend" tab="添加好友" />
          <NTabPane name="group" tab="添加群聊" />
        </NTabs>
        <NInputGroup>
          <NInputGroupLabel class="text-[var(--text-color-3)]">
            <NovaIcon icon="icon-park-outline:search" :size="16" />
          </NInputGroupLabel>
          <NInput v-model:value="addSearchText" clearable :placeholder="addSearchLabel" />
        </NInputGroup>
        <NScrollbar class="flex-1" style="max-height: calc(70vh - 140px);">
          <div v-if="addMode === 'friend'" class="pr-1">
            <NList v-if="addFriendUsers.length" hoverable>
              <NListItem v-for="user in addFriendUsers" :key="user.id" class="im-list-item">
                <div class="flex items-center gap-3 px-4 py-3">
                  <NAvatar round :size="40" class="shrink-0">{{ user.avatarText }}</NAvatar>
                  <div class="min-w-0 flex-1">
                    <div class="im-ellipsis text-sm font-600">{{ user.name }}</div>
                    <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ user.title }} · {{ user.department }}</div>
                  </div>
                  <NTag v-if="getFriendApplicationStatus(user) === 'accepted'" :bordered="false" size="small" type="success">已添加</NTag>
                  <NTag v-else-if="getFriendApplicationStatus(user) === 'pending'" :bordered="false" size="small" type="warning">申请中</NTag>
                  <NButton v-else size="small" tertiary @click="applyForFriend(user)">申请好友</NButton>
                </div>
              </NListItem>
            </NList>
            <NEmpty v-else class="py-8" description="暂无用户结果" />
          </div>
          <div v-if="addMode === 'group'" class="pr-1">
            <NList v-if="addGroupResults.length" hoverable>
              <NListItem v-for="group in addGroupResults" :key="group.id" class="im-list-item">
                <div class="flex items-center gap-3 px-4 py-3">
                  <NAvatar round :size="40" class="shrink-0">{{ group.avatarText }}</NAvatar>
                  <div class="min-w-0 flex-1">
                    <div class="im-ellipsis text-sm font-600">{{ group.name }}</div>
                    <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ group.memberCount }} 人 · {{ group.description }}</div>
                  </div>
                  <NTag v-if="getGroupApplicationStatus(group) === 'accepted'" :bordered="false" size="small" type="success">已加入</NTag>
                  <NTag v-else-if="getGroupApplicationStatus(group) === 'pending'" :bordered="false" size="small" type="warning">申请中</NTag>
                  <NButton v-else size="small" tertiary @click="applyForGroup(group)">申请入群</NButton>
                </div>
              </NListItem>
            </NList>
            <NEmpty v-else class="py-8" description="暂无群组结果" />
          </div>
        </NScrollbar>
        <div class="flex items-center gap-3 text-xs text-[var(--text-color-3)]">
          <span>结果 {{ addModeCount }} 项</span>
        </div>
      </div>
    </NModal>

    <NModal v-model:show="showCreateGroupModal" preset="card" :bordered="false" draggable
      title="创建群聊" :mask-closable="false"
      style="width: min(480px, calc(100vw - 24px));"
    >
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-4">
          <NAvatar round :size="56" class="shrink-0 cursor-pointer border-2 border-dashed border-[var(--border-color)]" @click="handleGroupAvatarClick">
            {{ createGroupAvatarText }}
          </NAvatar>
          <div class="min-w-0 flex-1">
            <NInput v-model:value="createGroupName" placeholder="群聊名称（必填）" size="large" />
          </div>
        </div>
        <NInput v-model:value="createGroupDesc" type="textarea" placeholder="群聊简介" :autosize="{ minRows: 2, maxRows: 4 }" />
        <NCheckbox v-model:value="createGroupPublic" class="!mt-0">公开群组（允许搜索加入）</NCheckbox>
        <div class="flex items-center justify-between">
          <span class="text-sm text-[var(--text-color-3)]">已邀请 {{ createGroupInvitees.length }} 人</span>
          <NButton size="small" @click="showInviteFriendModal = true">
            <template #icon><NovaIcon icon="icon-park-outline:add" :size="14" /></template>
            邀请好友
          </NButton>
        </div>
        <div v-if="createGroupInvitees.length" class="flex flex-wrap gap-2">
          <NTag v-for="id in createGroupInvitees" :key="id" closable :bordered="false" size="small" @close="removeGroupInvitee(id)">
            {{ data.friends.find(f => f.id === id)?.name || id }}
          </NTag>
        </div>
        <div class="flex justify-end gap-3 pt-2">
          <NButton @click="closeCreateGroup">取消</NButton>
          <NButton type="primary" :disabled="!createGroupName.trim()" @click="handleCreateGroup">创建</NButton>
        </div>
      </div>
    </NModal>

    <NModal v-model:show="showInviteFriendModal" preset="card" :bordered="false"
      title="邀请好友" :mask-closable="false"
      style="width: min(400px, calc(100vw - 24px)); max-height: 60vh;"
      content-style="display: flex; flex-direction: column; padding: 0 16px 16px; min-height: 0;"
    >
      <div class="flex min-h-0 flex-1 flex-col gap-3">
        <NInput v-model:value="inviteSearchText" clearable placeholder="搜索好友" size="small" />
        <NScrollbar class="flex-1" style="max-height: 40vh;">
          <NList v-if="filteredInviteFriends.length" hoverable>
            <NListItem v-for="friend in filteredInviteFriends" :key="friend.id" class="im-list-item cursor-pointer" @click="toggleGroupInvitee(friend.id)">
              <div class="flex items-center gap-3 px-2 py-2">
                <NCheckbox :checked="createGroupInvitees.includes(friend.id)" @click.stop="toggleGroupInvitee(friend.id)" />
                <NAvatar round :size="36" class="shrink-0">{{ friend.avatarText }}</NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="im-ellipsis text-sm font-500">{{ friend.name }}</div>
                  <div class="im-ellipsis text-xs text-[var(--text-color-3)]">{{ friend.title }} · {{ friend.department }}</div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else description="暂无好友" />
        </NScrollbar>
        <div class="flex justify-end">
          <NButton size="small" @click="showInviteFriendModal = false">确定</NButton>
        </div>
      </div>
    </NModal>

    <NDrawer v-model:show="showThreadDrawer" placement="right" :width="threadDrawerWidth">
      <NDrawerContent title="会话信息" closable :native-scrollbar="false">
        <template v-if="selectedThread">
          <NFlex vertical :size="18">
            <NFlex align="center" :wrap="false" :size="12">
              <NAvatar round :size="56" class="shrink-0">{{ selectedThread.avatarText }}</NAvatar>
              <div class="min-w-0 flex-1 overflow-hidden">
                <div class="im-ellipsis text-base font-600">{{ selectedThread.title }}</div>
                <div class="im-ellipsis mt-1 text-xs text-[var(--text-color-3)]">{{ selectedThread.subtitle }}</div>
              </div>
            </NFlex>
            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem label="类型">{{ selectedThread.kind === 'group' ? '群聊' : '好友' }}</NDescriptionsItem>
              <NDescriptionsItem label="最近消息">{{ getThreadLatestMessageText(selectedThread.id) }}</NDescriptionsItem>
              <NDescriptionsItem label="最近时间">{{ formatDateTime(selectedThread.lastMessageAt) }}</NDescriptionsItem>
              <NDescriptionsItem label="免打扰">{{ selectedThread.muted ? '已开启' : '未开启' }}</NDescriptionsItem>
            </NDescriptions>
            <template v-if="selectedFriend">
              <NDivider class="!my-0" />
              <NDescriptions :column="1" label-placement="left" size="small">
                <NDescriptionsItem label="职位">{{ selectedFriend.title }}</NDescriptionsItem>
                <NDescriptionsItem label="部门">{{ selectedFriend.department }}</NDescriptionsItem>
                <NDescriptionsItem label="状态">{{ selectedFriend.statusText }}</NDescriptionsItem>
              </NDescriptions>
              <NCard size="small" :bordered="false" :content-style="{ padding: '12px' }">{{ selectedFriend.signature }}</NCard>
            </template>
            <template v-if="selectedGroup">
              <NDivider class="!my-0" />
              <NDescriptions :column="1" label-placement="left" size="small">
                <NDescriptionsItem label="成员">{{ selectedGroup.memberCount }} 人</NDescriptionsItem>
                <NDescriptionsItem label="状态">{{ selectedGroup.statusText }}</NDescriptionsItem>
                <NDescriptionsItem label="说明">{{ selectedGroup.description }}</NDescriptionsItem>
              </NDescriptions>
            </template>
          </NFlex>
        </template>
      </NDrawerContent>
    </NDrawer>


    <div class="border-t border-[var(--border-color)] bg-[var(--body-color)] md:hidden">
      <div class="flex items-center justify-around py-1">
        <NButton
          text
          :class="activeSection === 'chat' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
          style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;"
          @click="openChatSection"
        >
          <template #icon>
            <NBadge :value="totalUnreadCount" :max="99" :show-zero="false">
              <NovaIcon icon="icon-park-outline:message" :size="20" />
            </NBadge>
          </template>
          <span style="font-size: 10px; line-height: 1;">聊天</span>
        </NButton>
        <NButton
          text
          :class="activeSection === 'contacts' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
          style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;"
          @click="openContactsSection"
        >
          <template #icon>
            <NovaIcon icon="icon-park-outline:people" :size="20" />
          </template>
          <span style="font-size: 10px; line-height: 1;">通讯录</span>
        </NButton>
        <NButton
          text
          :class="activeSection === 'todos' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
          style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;"
          @click="openTodosSection"
        >
          <template #icon>
            <NBadge :value="pendingTodoCount" :max="99" :show-zero="false">
              <NovaIcon icon="icon-park-outline:checklist" :size="20" />
            </NBadge>
          </template>
          <span style="font-size: 10px; line-height: 1;">待办</span>
        </NButton>
        <NButton
          text
          :class="activeSection === 'notice' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
          style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;"
          @click="openNoticeSection"
        >
          <template #icon>
            <NBadge :value="noticeBadgeTotal" :max="99" :show-zero="false">
              <NovaIcon icon="icon-park-outline:alarm" :size="20" />
            </NBadge>
          </template>
          <span style="font-size: 10px; line-height: 1;">通知</span>
        </NButton>
        <NButton
          text
          :class="activeSection === 'profile' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
          style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;"
          @click="openProfileSection"
        >
          <template #icon>
            <NovaIcon icon="icon-park-outline:user" :size="20" />
          </template>
          <span style="font-size: 10px; line-height: 1;">我的</span>
        </NButton>
      </div>
    </div>

  </n-el>
</template>

<style scoped>
.im-list-item {
  min-width: 0;
  overflow: hidden;
}

.im-list-item :deep(.n-list-item__main) {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.im-list-row,
.im-list-main-line,
.im-list-sub-line {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.im-list-body {
  min-width: 0;
  flex: 1 1 0;
  overflow: hidden;
}

.im-ellipsis {
  display: block;
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
