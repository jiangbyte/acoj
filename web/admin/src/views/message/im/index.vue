<script setup lang="ts">
import { computed, onMounted, provide, reactive, ref, watch } from 'vue'
import { useThemeVars } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useAuthStore } from '@/stores'
import { createMockImData, type MockApplicationRequest, type MockAttachment, type MockFriend, type MockGroup, type MockSystemNotice, type MockTodoItem } from './mock'
import { loadImData, loadNotifications, readNotificationApi, readAllNotificationApi, deleteFriendApi } from './im-api'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from './im-provide'

import ImSidebar from './components/ImSidebar.vue'
import ImMobileBottomNav from './components/ImMobileBottomNav.vue'
import ImListPane from './components/ImListPane.vue'
import ImChatPane from './components/ImChatPane.vue'
import ImNoticeDetail from './components/ImNoticeDetail.vue'
import ImContactDetail from './components/ImContactDetail.vue'
import ImProfilePane from './components/ImProfilePane.vue'
import ImProfileModal from './components/ImProfileModal.vue'
import ImAddFriendModal from './components/ImAddFriendModal.vue'
import ImCreateGroupModal from './components/ImCreateGroupModal.vue'

const appStore = useAppStore()
const themeVars = useThemeVars()
const router = useRouter()
const route = useRoute()
const homePath = import.meta.env.VITE_HOME_PATH || '/dashboard'
const data = reactive(createMockImData())
const authStore = useAuthStore()

/* ---- URL-driven state ---- */

// 这些 ref 是单数据源，URL 查询参数是持久层。
// 通过 syncStateToUrl 将变化同步到 URL；通过 applyFromRoute 在 mounted 和路由变化时回读。
const activeSection = ref('chat')
const mobileView = ref('list')
const selectedThreadId = ref('')
const selectedContact = ref<{ kind: string; id: string } | null>(null)
const selectedNoticeId = ref<string | null>(null)
const selectedTodoId = ref<string | null>(null)
const selectedPendingRequestId = ref<string | null>(null)
const showAddModal = ref(false)
const addModalMode = ref<'friend' | 'group'>('friend')
const showCreateGroupModal = ref(false)
const threadDrafts = ref<Record<string, { text: string; attachments: MockAttachment[] }>>({})
const searchText = ref('')
const searchScope = ref('threads')
const contactTab = ref('friends')
const noticeTab = ref('notices')
const todoTab = ref('pending')

/* ---- Derived UI state ---- */

const showProfileModal = ref(false)
const contactActionHint = ref('')
const profile = computed(() => data.profile)
const isMobile = computed(() => appStore.isMobile)
const hasSearchKeyword = computed(() => searchText.value.trim().length > 0)

const selectedThread = computed(() =>
  data.threads.find((t) => t.id === selectedThreadId.value) ?? null,
)

const selectedNotice = computed(() =>
  data.notices.find((n) => n.id === selectedNoticeId.value) ?? null,
)
const selectedTodo = computed(() =>
  data.todos.find((t) => t.id === selectedTodoId.value) ?? null,
)
const selectedPendingRequest = computed(() =>
  data.requests.find((r) => r.id === selectedPendingRequestId.value) ?? null,
)

const showPendingDetail = computed(() =>
  (activeSection.value === 'notice' && selectedNoticeId.value !== null) ||
  (activeSection.value === 'todos' && selectedTodoId.value !== null) ||
  selectedPendingRequestId.value !== null,
)

const showListPane = computed(() => activeSection.value !== 'profile' && (!isMobile.value || mobileView.value === 'list'))
const showChatPane = computed(() => activeSection.value === 'chat' && (!isMobile.value || mobileView.value === 'chat') && selectedThread.value)
const showNoticeDetailPane = computed(() => showPendingDetail.value && (activeSection.value === 'notice' || activeSection.value === 'todos'))
const showContactDetailPane = computed(() => activeSection.value === 'contacts' && (!isMobile.value || mobileView.value === 'detail') && selectedContact.value)
const showProfilePane = computed(() => isMobile.value && activeSection.value === 'profile')

const selectedFriendContact = computed(() => {
  if (selectedContact.value?.kind !== 'friend') return null
  return data.friends.find((f) => f.id === selectedContact.value?.id) ?? null
})
const selectedGroupContact = computed(() => {
  if (selectedContact.value?.kind !== 'group') return null
  return data.groups.find((g) => g.id === selectedContact.value?.id) ?? null
})

/* ---- URL sync helpers ---- */

/** 把当前状态写入 URL 查询参数（不生成历史记录，仅 replace）。 */
function syncStateToUrl() {
  const query: Record<string, string> = {}
  if (activeSection.value !== 'chat') query.section = activeSection.value
  if (selectedThreadId.value) query.thread = selectedThreadId.value
  if (selectedContact.value) {
    query.contactKind = selectedContact.value.kind
    query.contactId = selectedContact.value.id
  }
  if (selectedNoticeId.value) query.notice = selectedNoticeId.value
  if (selectedTodoId.value) query.todo = selectedTodoId.value
  if (selectedPendingRequestId.value) query.pending = selectedPendingRequestId.value
  if (contactTab.value !== 'friends') query.ctab = contactTab.value
  if (noticeTab.value !== 'notices') query.ntab = noticeTab.value
  if (todoTab.value !== 'pending') query.ttab = todoTab.value
  if (isMobile.value && mobileView.value !== 'list') query.view = mobileView.value

  router.replace({ query }).catch(() => {})
}

/** 从 URL 查询参数恢复内部状态。 */
function applyFromRoute() {
  const q = route.query
  activeSection.value = (q.section as string) || 'chat'
  selectedThreadId.value = (q.thread as string) || ''
  selectedContact.value = q.contactKind && q.contactId
    ? { kind: q.contactKind as string, id: q.contactId as string }
    : null
  selectedNoticeId.value = (q.notice as string) || null
  selectedTodoId.value = (q.todo as string) || null
  selectedPendingRequestId.value = (q.pending as string) || null
  contactTab.value = (q.ctab as string) || 'friends'
  noticeTab.value = (q.ntab as string) || 'notices'
  todoTab.value = (q.ttab as string) || 'pending'
  if (isMobile.value) {
    mobileView.value = (q.view as string) || (selectedThreadId.value ? 'chat' : 'list')
  }
}

// 监听所有导航相关的 ref 变化，同步到 URL。
// 使用 flush: 'post' 确保 DOM 已更新后再同步 URL，不阻塞渲染。
watch(
  [activeSection, selectedThreadId, selectedContact, selectedNoticeId, selectedTodoId, selectedPendingRequestId, mobileView, contactTab, noticeTab, todoTab],
  syncStateToUrl,
  { flush: 'post' },
)

// 监听浏览器前进/后退导致的 route.query 变化，回读状态。
watch(
  () => route.query,
  () => { applyFromRoute() },
)

// 兼容 openThread 调用方传递 syncRoute=false 跳过 URL 同步的场景（如从联系人发起聊天时的额外调用）
let skipNextSync = false
function skipOnce() { skipNextSync = true }

/* ---- Actions ---- */

function goHome() { router.push(homePath) }
function openProfileModal() { showProfileModal.value = true }
function goProfileCenter() { showProfileModal.value = false; router.push('/usercenter') }
function handleLogout() { authStore.logout() }

function openChatSection() { activeSection.value = 'chat'; if (isMobile.value) mobileView.value = 'list' }
function openContactsSection() { activeSection.value = 'contacts'; if (isMobile.value) mobileView.value = 'list' }
function openNoticeSection() { activeSection.value = 'notice'; if (isMobile.value) mobileView.value = 'list' }
function openTodosSection() { activeSection.value = 'todos'; if (isMobile.value) mobileView.value = 'list' }
function openProfileSection() { activeSection.value = 'profile'; if (isMobile.value) mobileView.value = 'list' }

function openFriend(friend: MockFriend) {
  contactActionHint.value = ''
  selectedContact.value = { kind: 'friend', id: friend.id }
  selectedNoticeId.value = null; selectedTodoId.value = null; selectedPendingRequestId.value = null
  activeSection.value = 'contacts'
  if (isMobile.value) mobileView.value = 'detail'
}
function openGroup(group: MockGroup) {
  contactActionHint.value = ''
  selectedContact.value = { kind: 'group', id: group.id }
  selectedNoticeId.value = null; selectedTodoId.value = null; selectedPendingRequestId.value = null
  activeSection.value = 'contacts'
  if (isMobile.value) mobileView.value = 'detail'
}

function openThread(threadId: string) {
  const thread = data.threads.find((t) => t.id === threadId)
  if (!thread) return
  selectedThreadId.value = threadId
  selectedContact.value = null
  selectedNoticeId.value = null; selectedTodoId.value = null; selectedPendingRequestId.value = null
  activeSection.value = 'chat'
  if (isMobile.value) mobileView.value = 'chat'
  thread.unreadCount = 0
}

function saveDraft(draft: { text: string; attachments: MockAttachment[] }) {
  if (selectedThreadId.value) threadDrafts.value[selectedThreadId.value] = draft
}
function closeCurrentThread() {
  selectedThreadId.value = ''
  selectedContact.value = null
  if (isMobile.value) mobileView.value = 'list'
}

function backToListPane() { if (isMobile.value) mobileView.value = 'list' }
function openAddModal(mode?: 'friend' | 'group') { addModalMode.value = mode || 'friend'; showAddModal.value = true }

function openNoticeDetail(notice: MockSystemNotice) {
  notice.read = true
  readNotificationApi([notice.id])
  selectedNoticeId.value = notice.id
  selectedTodoId.value = null; selectedPendingRequestId.value = null
  selectedContact.value = null
  if (isMobile.value) mobileView.value = 'detail'
}
function openTodoDetail(todo: MockTodoItem) {
  selectedTodoId.value = todo.id
  selectedNoticeId.value = null; selectedPendingRequestId.value = null
  selectedContact.value = null
  if (isMobile.value) mobileView.value = 'detail'
}
function openPendingDetail(request: MockApplicationRequest) {
  selectedPendingRequestId.value = request.id
  selectedNoticeId.value = null; selectedTodoId.value = null
  selectedContact.value = null
  if (isMobile.value) mobileView.value = 'detail'
}
function closePendingDetail() {
  selectedNoticeId.value = null; selectedTodoId.value = null; selectedPendingRequestId.value = null
  if (isMobile.value) mobileView.value = 'list'
}
function markTodoDone(todo: MockTodoItem) { todo.status = 'done'; closePendingDetail() }
function markTodoCancelled(todo: MockTodoItem) { todo.status = 'cancelled'; closePendingDetail() }

function acceptPendingRequest() {
  if (selectedPendingRequest.value) selectedPendingRequest.value.status = 'approved'
  closePendingDetail()
}
function rejectPendingRequest() {
  if (selectedPendingRequest.value) selectedPendingRequest.value.status = 'rejected'
  closePendingDetail()
}
function continueChatFromContact() {
  if (selectedFriendContact.value) { openThread(selectedFriendContact.value.threadId); return }
  if (selectedGroupContact.value) openThread((selectedGroupContact.value as any).threadId ?? '')
}
async function handleRemoveFriend() {
  if (!selectedFriendContact.value) return
  contactActionHint.value = ''
  const ok = await deleteFriendApi('ADMIN', selectedFriendContact.value.id)
  if (ok) {
    const idx = data.friends.findIndex((f) => f.id === selectedFriendContact.value?.id)
    if (idx >= 0) data.friends.splice(idx, 1)
    selectedContact.value = null
    window.$message?.success?.('已删除好友')
  } else {
    window.$message?.error?.('删除好友失败')
  }
}
async function handleLeaveGroup() {
  if (!selectedGroupContact.value) return
  contactActionHint.value = ''
  try {
    const { removeGroupMembers } = await import('@/api/message')
    await removeGroupMembers({ group_id: selectedGroupContact.value.id, member_refs: [] })
    const idx = data.groups.findIndex((g) => g.id === selectedGroupContact.value?.id)
    if (idx >= 0) data.groups.splice(idx, 1)
    selectedContact.value = null
    window.$message?.success?.('已退出群聊')
  } catch {
    window.$message?.error?.('退出群聊失败')
  }
}

const imActions = {
  goHome, openProfileModal, goProfileCenter, handleLogout,
  openThread, closeCurrentThread,
  openChatSection, openContactsSection, openNoticeSection, openTodosSection, openProfileSection,
  backToListPane, openFriend, openGroup,
  openNoticeDetail, openTodoDetail, openPendingDetail, closePendingDetail,
  openAddModal, markTodoDone, markTodoCancelled,
}
const imUIState = { activeSection, isMobile, mobileView, showProfileModal, searchText, hasSearchKeyword, searchScope, contactTab, noticeTab, todoTab }

provide(IM_ACTIONS_KEY, imActions)
provide(IM_UI_STATE_KEY, imUIState)
provide(IM_DATA_KEY, data)

onMounted(async () => {
  const [apiData, notices] = await Promise.all([
    loadImData(),
    loadNotifications(1, 50).catch(() => null),
  ])
  Object.assign(data, apiData)
  if (notices?.length) data.notices = notices
  applyFromRoute()
})

const pageStyle = computed(() => ({
  backgroundColor: themeVars.value.bodyColor,
  color: themeVars.value.textColorBase,
  height: '100dvh',
  minHeight: '100vh',
}))
</script>

<template>
  <n-el tag="main" class="fixed inset-0 flex flex-col overflow-hidden" :style="pageStyle">
    <div class="grid min-h-0 flex-1 gap-0 md:grid-cols-[60px_minmax(280px,360px)_minmax(0,1fr)]">
      <ImSidebar />

      <ImListPane v-show="showListPane" />

      <ImChatPane v-if="showChatPane" :thread="selectedThread!" :draft="threadDrafts[selectedThreadId] || { text: '', attachments: [] }" @update:draft="saveDraft($event)" @close="closeCurrentThread" />

      <ImNoticeDetail
        v-show="showNoticeDetailPane"
        :request="selectedPendingRequest"
        :notice="selectedNotice"
        :todo="selectedTodo"
        @accept-request="acceptPendingRequest"
        @reject-request="rejectPendingRequest"
        @mark-todo-done="markTodoDone"
        @mark-todo-cancelled="markTodoCancelled"
        @close="closePendingDetail"
      />

      <ImContactDetail
        v-show="showContactDetailPane"
        :friend="selectedFriendContact"
        :group="selectedGroupContact"
        :hint="contactActionHint"
        @chat="continueChatFromContact"
        @remove-friend="handleRemoveFriend"
        @leave-group="handleLeaveGroup"
        @back="backToListPane"
      />

      <ImProfilePane v-show="showProfilePane" />
    </div>

    <ImMobileBottomNav />

    <ImProfileModal />
    <ImAddFriendModal v-model:show="showAddModal" :initial-mode="addModalMode" />
    <ImCreateGroupModal v-model:show="showCreateGroupModal" />
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
