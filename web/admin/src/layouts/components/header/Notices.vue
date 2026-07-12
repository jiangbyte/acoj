<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { messageApi } from '@/api'
import MessageDetailModal from '@/components/message/MessageDetailModal.vue'
import { formatDateTime } from '@/utils'
import NoticeList, { type NoticeItem } from '../common/NoticeList.vue'

const noticeTypes = [0, 1, 2] as const
const pageSize = 8

type NoticeType = (typeof noticeTypes)[number]
type LoadMode = 'replace' | 'merge' | 'append'

interface NoticeSource {
  id: string
  type: NoticeType
  title: string
  icon: string
  tagTitle?: string
  tagType?: NoticeItem['tagType']
  description?: string
  date: string
  sourceType: string
  sourceId: string
  isRead: boolean
}

interface NoticeTabState {
  records: NoticeSource[]
  current: number
  size: number
  total: number
  loading: boolean
  loaded: boolean
}

function createTabState(): NoticeTabState {
  return {
    records: [],
    current: 0,
    size: pageSize,
    total: 0,
    loading: false,
    loaded: false,
  }
}

const tabStates = reactive<Record<NoticeType, NoticeTabState>>({
  0: createTabState(),
  1: createTabState(),
  2: createTabState(),
})
const summary = ref({
  notification_unread: 0,
  message_unread: 0,
  todo_pending: 0,
  total: 0,
})
const currentTab = ref<NoticeType>(0)
const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
let eventSource: EventSource | null = null
let pollTimer: number | null = null

const groups = computed(() => ({
  0: tabStates[0].records.map(toNoticeItem),
  1: tabStates[1].records.map(toNoticeItem),
  2: tabStates[2].records.map(toNoticeItem),
}))

const hasMore = computed(() => ({
  0: tabStates[0].records.length < tabStates[0].total,
  1: tabStates[1].records.length < tabStates[1].total,
  2: tabStates[2].records.length < tabStates[2].total,
}))

const unreadCount = computed(() => summary.value.total)

onMounted(() => {
  refresh()
  setupRealtime()
  pollTimer = window.setInterval(refreshSummary, 60000)
})

onBeforeUnmount(() => {
  eventSource?.close()
  if (pollTimer) {
    window.clearInterval(pollTimer)
  }
})

watch(currentTab, (type) => {
  if (!tabStates[type].loaded) {
    void loadTab(type)
  }
})

async function refresh() {
  await Promise.all([refreshSummary(), loadInitialHistories()])
}

async function refreshSummary() {
  const response = await messageApi.summary()
  summary.value = {
    notification_unread: response.data?.notification_unread ?? 0,
    message_unread: response.data?.message_unread ?? 0,
    todo_pending: response.data?.todo_pending ?? 0,
    total: response.data?.total ?? 0,
  }
}

async function loadInitialHistories() {
  await Promise.all(
    noticeTypes.map((type) => loadTab(type, 1, tabStates[type].loaded ? 'merge' : 'replace')),
  )
}

async function refreshFirstPages() {
  await Promise.all(noticeTypes.map((type) => loadTab(type, 1, 'merge')))
}

async function loadMore(type: NoticeType) {
  const state = tabStates[type]
  if (state.loading || state.records.length >= state.total) {
    return
  }
  await loadTab(type, state.current + 1, 'append')
}

async function loadTab(type: NoticeType, page = 1, mode: LoadMode = 'replace') {
  const state = tabStates[type]
  if (state.loading) {
    return
  }
  state.loading = true
  try {
    const response = await fetchHistoryPage(type, page, state.size)
    const data = response.data ?? {}
    const incoming = (data.records ?? []).map((item: any) => mapHistoryItem(type, item))
    state.records = mergeNoticeRecords(state.records, incoming, mode)
    state.total = data.total ?? state.records.length
    const responseCurrent = data.current ?? page
    state.current =
      mode === 'merge' && state.loaded ? Math.max(state.current, responseCurrent) : responseCurrent
    state.size = data.size ?? state.size
    state.loaded = true
  } finally {
    state.loading = false
  }
}

function fetchHistoryPage(type: NoticeType, current: number, size: number) {
  if (type === 0) {
    return messageApi.myNotification({ current, size })
  }
  if (type === 1) {
    return messageApi.myThreads({ current, size })
  }
  return messageApi.myTodos({ current, size, include_done: true })
}

function setupRealtime() {
  eventSource = messageApi.createEventSource()
  if (!eventSource) {
    return
  }
  eventSource.addEventListener('summary', (event) => {
    try {
      const data = JSON.parse((event as MessageEvent).data)
      summary.value = {
        notification_unread: data.notification_unread ?? 0,
        message_unread: data.message_unread ?? 0,
        todo_pending: data.todo_pending ?? 0,
        total: data.total ?? 0,
      }
      void refreshFirstPages()
    } catch {
      refresh()
    }
  })
}

async function handleOpen(id: string) {
  const item = findNotice(id)
  if (!item) {
    return
  }
  await detailModalRef.value?.open(item.sourceType as any, {
    ...item,
    id: item.sourceId,
    is_read: item.isRead,
    assignee_status: item.isRead ? 'IN_PROGRESS' : null,
    unread_count: item.isRead ? 0 : 1,
  })
}

function findNotice(id: string) {
  for (const type of noticeTypes) {
    const item = tabStates[type].records.find((notice) => notice.id === id)
    if (item) {
      return item
    }
  }
  return null
}

function applyLocalReadCount(item: NoticeSource) {
  if (item.type === 0 && summary.value.notification_unread > 0) {
    summary.value.notification_unread -= 1
  } else if (item.type === 1 && summary.value.message_unread > 0) {
    summary.value.message_unread -= 1
  } else if (item.type === 2 && summary.value.todo_pending > 0) {
    summary.value.todo_pending -= 1
  }
  summary.value.total = Math.max(0, summary.value.total - 1)
}

async function handleDetailChanged(payload: { type: string; id: string }) {
  const item = findNotice(`${payload.type}:${payload.id}`)
  if (item && !item.isRead) {
    item.isRead = true
    applyLocalReadCount(item)
  }
  await refreshSummary()
}

function mergeNoticeRecords(
  current: NoticeSource[],
  incoming: NoticeSource[],
  mode: LoadMode,
): NoticeSource[] {
  if (mode === 'replace') {
    return incoming
  }

  const currentMap = new Map(current.map((item) => [item.id, item]))
  const incomingMap = new Map(incoming.map((item) => [item.id, item]))
  if (mode === 'append') {
    const result = current.map((item) => mergeNoticeItem(item, incomingMap.get(item.id)))
    incoming.forEach((item) => {
      if (!currentMap.has(item.id)) {
        result.push(item)
      }
    })
    return result
  }

  const used = new Set<string>()
  const result = incoming.map((item) => {
    used.add(item.id)
    return mergeNoticeItem(currentMap.get(item.id), item)
  })
  current.forEach((item) => {
    if (!used.has(item.id)) {
      result.push(item)
    }
  })
  return result
}

function mergeNoticeItem(current?: NoticeSource, incoming?: NoticeSource): NoticeSource {
  if (!incoming) {
    return current!
  }
  if (!current) {
    return incoming
  }
  return {
    ...current,
    ...incoming,
    isRead: current.isRead || incoming.isRead,
  }
}

function mapHistoryItem(type: NoticeType, item: any): NoticeSource {
  if (type === 0) {
    const severity = normalizeEnum(item.severity)
    return {
      id: `notification:${item.id}`,
      type,
      title: item.title,
      icon: 'icon-park-outline:tips-one',
      tagTitle: translateTag(severity, item.severity),
      tagType: notificationTagType(severity),
      description: item.content,
      date: formatDateTime(item.publish_at || item.created_at),
      sourceType: 'notification',
      sourceId: item.id,
      isRead: Boolean(item.is_read),
    }
  }
  if (type === 1) {
    const threadType = normalizeEnum(item.thread_type)
    return {
      id: `message:${item.id}`,
      type,
      title: item.title || '会话标题',
      icon: 'icon-park-outline:message',
      tagTitle: translateTag(threadType, item.thread_type),
      tagType: 'info',
      description: item.last_message?.content,
      date: formatDateTime(item.last_message_at || item.updated_at || item.created_at),
      sourceType: 'message',
      sourceId: item.id,
      isRead: (item.unread_count ?? 0) <= 0,
    }
  }

  const priority = normalizeEnum(item.priority)
  return {
    id: `todo:${item.id}`,
    type,
    title: item.title,
    icon: 'icon-park-outline:checklist',
    tagTitle: translateTag(priority, item.priority),
    tagType: priorityTagType(priority),
    description: item.content,
    date: formatDateTime(item.due_at || item.updated_at || item.created_at),
    sourceType: 'todo',
    sourceId: item.id,
    isRead: Boolean(item.assignee_status),
  }
}

function toNoticeItem(item: NoticeSource): NoticeItem {
  return {
    ...item,
    isRead: item.isRead,
  }
}

function translateTag(normalized: string, fallback: unknown) {
  if (!normalized) {
    return undefined
  }
  return String(fallback ?? normalized)
}

function normalizeEnum(value: unknown) {
  return String(value ?? '').toLowerCase()
}

function notificationTagType(severity: string): NoticeItem['tagType'] {
  const typeMap: Record<string, NoticeItem['tagType']> = {
    success: 'success',
    warning: 'warning',
    error: 'error',
  }
  return typeMap[severity] || 'info'
}

function priorityTagType(priority: string): NoticeItem['tagType'] {
  const typeMap: Record<string, NoticeItem['tagType']> = {
    low: 'default',
    normal: 'info',
    high: 'warning',
    urgent: 'error',
  }
  return typeMap[priority] || 'info'
}

</script>

<template>
  <n-popover placement="bottom" trigger="click" arrow-point-to-center class="!p-0">
    <template #trigger>
      <n-tooltip placement="bottom" trigger="hover">
        <template #trigger>
          <CommonWrapper>
            <n-badge :value="unreadCount" :max="99" style="color: unset">
              <NovaIcon icon="icon-park-outline:remind" />
            </n-badge>
          </CommonWrapper>
        </template>
        通知
      </n-tooltip>
    </template>
    <n-tabs
      v-model:value="currentTab"
      type="line"
      animated
      justify-content="space-evenly"
      class="w-390px"
    >
      <n-tab-pane :name="0">
        <template #tab>
          <n-space class="w-130px" justify="center">
            通知
            <n-badge type="info" :value="summary.notification_unread" :max="99" />
          </n-space>
        </template>
        <NoticeList
          :list="groups[0]"
          :loading="tabStates[0].loading"
          :has-more="hasMore[0]"
          @open="handleOpen"
          @load-more="loadMore(0)"
        />
      </n-tab-pane>
      <n-tab-pane :name="1">
        <template #tab>
          <n-space class="w-130px" justify="center">
            消息
            <n-badge type="warning" :value="summary.message_unread" :max="99" />
          </n-space>
        </template>
        <NoticeList
          :list="groups[1]"
          :loading="tabStates[1].loading"
          :has-more="hasMore[1]"
          @open="handleOpen"
          @load-more="loadMore(1)"
        />
      </n-tab-pane>
      <n-tab-pane :name="2">
        <template #tab>
          <n-space class="w-130px" justify="center">
            待办
            <n-badge type="error" :value="summary.todo_pending" :max="99" />
          </n-space>
        </template>
        <NoticeList
          :list="groups[2]"
          :loading="tabStates[2].loading"
          :has-more="hasMore[2]"
          @open="handleOpen"
          @load-more="loadMore(2)"
        />
      </n-tab-pane>
    </n-tabs>
  </n-popover>

  <MessageDetailModal ref="detailModalRef" @changed="handleDetailChanged" />
</template>
