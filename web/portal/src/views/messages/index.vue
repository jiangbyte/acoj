<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { messageApi } from '@/api'
import { useAppStore } from '@/stores'
import { createTagColor, displayValue } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

type ActiveTab = 'notifications' | 'messages'
type LoadMode = 'replace' | 'append' | 'merge'

interface PageState<T> {
  records: T[]
  current: number
  size: number
  total: number
  loading: boolean
  loaded: boolean
}

const { t } = useI18n()
const appStore = useAppStore()
const activeTab = ref<ActiveTab>('notifications')
const replyContent = ref('')
const mobileDetailVisible = ref(false)

const notifications = reactive<PageState<any>>(createPageState())
const threads = reactive<PageState<any>>(createPageState())
const detail = reactive({
  loading: false,
  actionLoading: false,
  notification: null as any,
  thread: null as any,
  messages: [] as any[],
})

const currentListLoading = computed(() =>
  activeTab.value === 'notifications' ? notifications.loading : threads.loading,
)

const showListPane = computed(() => !appStore.isMobile || !mobileDetailVisible.value)
const showDetailPane = computed(() => !appStore.isMobile || mobileDetailVisible.value)

const selectedTitle = computed(() => {
  if (activeTab.value === 'notifications') {
    return detail.notification?.title || t('app.message_center.select_notification')
  }
  return detail.thread?.title || t('app.message_center.select_thread')
})

const sortedThreadMessages = computed(() =>
  [...detail.messages].sort((a, b) =>
    String(a.created_at || '').localeCompare(String(b.created_at || '')),
  ),
)

onMounted(async () => {
  await Promise.all([loadNotifications(), loadThreads()])
  selectFirstItem()
})

watch(activeTab, () => {
  mobileDetailVisible.value = false
  selectFirstItem()
})

watch(
  () => appStore.isMobile,
  (isMobile) => {
    if (isMobile) {
      mobileDetailVisible.value = false
      return
    }
    selectFirstItem()
  },
)

function createPageState<T>(): PageState<T> {
  return {
    records: [],
    current: 0,
    size: 10,
    total: 0,
    loading: false,
    loaded: false,
  }
}

async function loadNotifications(page = 1, mode: LoadMode = 'replace') {
  if (notifications.loading) {
    return
  }

  notifications.loading = true
  try {
    const response = await messageApi.myNotifications({
      current: page,
      size: notifications.size,
    })
    applyPageData(notifications, response.data, mode)
  } finally {
    notifications.loading = false
  }
}

async function loadThreads(page = 1, mode: LoadMode = 'replace') {
  if (threads.loading) {
    return
  }

  threads.loading = true
  try {
    const response = await messageApi.myThreads({
      current: page,
      size: threads.size,
    })
    applyPageData(threads, response.data, mode)
  } finally {
    threads.loading = false
  }
}

function applyPageData<T extends { id: string }>(state: PageState<T>, data: any, mode: LoadMode) {
  const incoming = (data?.records ?? []) as T[]
  if (mode === 'append') {
    const existing = new Set(state.records.map((item) => item.id))
    state.records = [...state.records, ...incoming.filter((item) => !existing.has(item.id))]
  } else if (mode === 'merge') {
    const currentMap = new Map(state.records.map((item) => [item.id, item]))
    incoming.forEach((item) => currentMap.set(item.id, { ...currentMap.get(item.id), ...item }))
    state.records = Array.from(currentMap.values())
  } else {
    state.records = incoming
  }

  state.current = data?.current ?? pageFallback(state, mode)
  state.size = data?.size ?? state.size
  state.total = data?.total ?? state.records.length
  state.loaded = true
}

function pageFallback<T>(state: PageState<T>, mode: LoadMode) {
  return mode === 'append' ? state.current + 1 : 1
}

function selectFirstItem() {
  if (appStore.isMobile) {
    return
  }

  if (activeTab.value === 'notifications') {
    if (!detail.notification && notifications.records.length) {
      void openNotification(notifications.records[0])
    }
    return
  }

  if (!detail.thread && threads.records.length) {
    void openThread(threads.records[0])
  }
}

async function loadMoreNotifications() {
  if (notifications.records.length >= notifications.total) {
    return
  }
  await loadNotifications(notifications.current + 1, 'append')
}

async function loadMoreThreads() {
  if (threads.records.length >= threads.total) {
    return
  }
  await loadThreads(threads.current + 1, 'append')
}

async function openNotification(item: any) {
  detail.notification = item
  openMobileDetail()
  detail.loading = true
  try {
    const response = await messageApi.myNotificationDetail({ id: item.id })
    detail.notification = { ...item, ...(response.data ?? {}) }
    if (!detail.notification.is_read) {
      await markNotificationRead(detail.notification.id)
    }
  } finally {
    detail.loading = false
  }
}

async function markNotificationRead(id: string) {
  detail.actionLoading = true
  try {
    await messageApi.readNotifications({ ids: [id] })
    markNotificationLocalRead(id)
    dispatchSummaryRefresh()
  } finally {
    detail.actionLoading = false
  }
}

function markNotificationLocalRead(id: string) {
  notifications.records = notifications.records.map((item) =>
    item.id === id ? { ...item, is_read: true } : item,
  )
  if (detail.notification?.id === id) {
    detail.notification = { ...detail.notification, is_read: true }
  }
}

async function openThread(item: any) {
  detail.thread = item
  replyContent.value = ''
  openMobileDetail()
  await loadThreadMessages(item.id)

  if ((item.unread_count ?? 0) > 0) {
    await messageApi.readThread({ thread_id: item.id })
    markThreadLocalRead(item.id)
    dispatchSummaryRefresh()
  }
}

async function loadThreadMessages(threadId: string) {
  detail.loading = true
  try {
    const response = await messageApi.myThreadMessages({
      thread_id: threadId,
      current: 1,
      size: 30,
    })
    detail.messages = response.data?.records ?? []
  } finally {
    detail.loading = false
  }
}

function markThreadLocalRead(id: string) {
  threads.records = threads.records.map((item) =>
    item.id === id ? { ...item, unread_count: 0 } : item,
  )
  if (detail.thread?.id === id) {
    detail.thread = { ...detail.thread, unread_count: 0 }
  }
}

async function replyMessage() {
  const content = replyContent.value.trim()
  if (!content || !detail.thread?.id) {
    window.$message.warning(t('resource.message.message.reply_required'))
    return
  }

  detail.actionLoading = true
  try {
    await messageApi.replyMessage({
      thread_id: detail.thread.id,
      content,
    })
    replyContent.value = ''
    await Promise.all([loadThreadMessages(detail.thread.id), loadThreads(1, 'merge')])
    dispatchSummaryRefresh()
  } finally {
    detail.actionLoading = false
  }
}

function dispatchSummaryRefresh() {
  window.dispatchEvent(new Event('portal-message-summary-refresh'))
}

function openMobileDetail() {
  if (appStore.isMobile) {
    mobileDetailVisible.value = true
  }
}

function backToList() {
  mobileDetailVisible.value = false
}

function statusTagType(isRead: boolean) {
  return isRead ? 'success' : 'warning'
}

function displayTime(value?: string | null) {
  return value || ''
}
</script>

<template>
  <section class="px-4 py-6 sm:px-6 lg:px-8">
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-2xl font-800">{{ t('app.messages_center') }}</h1>
        <p class="mt-1 text-sm text-[var(--text-color-3)]">
          {{ t('app.message_center.subtitle') }}
        </p>
      </div>
      <n-button
        text
        :focusable="false"
        :title="t('common.reload')"
        :aria-label="t('common.reload')"
        :loading="currentListLoading"
        @click="
          activeTab === 'notifications'
            ? loadNotifications(1, 'replace')
            : loadThreads(1, 'replace')
        "
      >
        <template #icon>
          <NovaIcon icon="icon-park-outline:refresh" />
        </template>
      </n-button>
    </div>

    <div class="grid w-full min-w-0 gap-4 lg:grid-cols-[minmax(320px,420px)_1fr]">
      <n-el
        v-if="showListPane"
        tag="aside"
        class="min-h-120 w-full min-w-0 overflow-hidden rounded-2 border border-[var(--border-color)] bg-[var(--card-color)]"
      >
        <n-tabs v-model:value="activeTab" type="line" animated class="messages-tabs">
          <n-tab-pane name="notifications">
            <template #tab>
              <div class="inline-flex items-center gap-2">
                {{ t('app.notifications') }}
                <n-badge
                  type="info"
                  :value="notifications.records.filter((item) => !item.is_read).length"
                  :max="99"
                />
              </div>
            </template>

            <n-scrollbar class="h-[calc(100vh-290px)] min-h-96">
              <div
                v-if="notifications.loading && !notifications.records.length"
                class="py-20 text-center"
              >
                <n-spin size="small" />
              </div>
              <n-empty
                v-else-if="!notifications.records.length"
                class="py-18"
                :description="t('app.notice.empty')"
              />
              <n-list v-else hoverable clickable>
                <n-list-item
                  v-for="item in notifications.records"
                  :key="item.id"
                  :class="{ 'is-active-list-item': detail.notification?.id === item.id }"
                  @click="openNotification(item)"
                >
                  <n-thing :class="{ 'opacity-45': item.is_read }">
                    <template #avatar>
                      <NovaIcon
                        icon="icon-park-outline:tips-one"
                        :size="26"
                        class="text-[var(--primary-color)]"
                      />
                    </template>
                    <template #header>
                      <n-ellipsis :line-clamp="1">{{ item.title }}</n-ellipsis>
                    </template>
                    <template #header-extra>
                      <n-tag
                        size="small"
                        :bordered="false"
                        :type="statusTagType(Boolean(item.is_read))"
                      >
                        {{ item.is_read ? t('app.notice.read') : t('app.notice.unread') }}
                      </n-tag>
                    </template>
                    <template #description>
                      <n-ellipsis :line-clamp="2">{{ item.content }}</n-ellipsis>
                    </template>
                    <template #footer>
                      {{ displayTime(item.publish_at || item.created_at) }}
                    </template>
                  </n-thing>
                </n-list-item>
                <div
                  v-if="notifications.records.length < notifications.total"
                  class="py-3 text-center"
                >
                  <n-button
                    text
                    size="small"
                    :loading="notifications.loading"
                    @click.stop="loadMoreNotifications"
                  >
                    {{ t('app.notice.load_more') }}
                  </n-button>
                </div>
              </n-list>
            </n-scrollbar>
          </n-tab-pane>

          <n-tab-pane name="messages">
            <template #tab>
              <div class="inline-flex items-center gap-2">
                {{ t('app.messages') }}
                <n-badge
                  type="warning"
                  :value="threads.records.reduce((sum, item) => sum + (item.unread_count ?? 0), 0)"
                  :max="99"
                />
              </div>
            </template>

            <n-scrollbar class="h-[calc(100vh-290px)] min-h-96">
              <div v-if="threads.loading && !threads.records.length" class="py-20 text-center">
                <n-spin size="small" />
              </div>
              <n-empty
                v-else-if="!threads.records.length"
                class="py-18"
                :description="t('app.notice.empty')"
              />
              <n-list v-else hoverable clickable>
                <n-list-item
                  v-for="item in threads.records"
                  :key="item.id"
                  :class="{ 'is-active-list-item': detail.thread?.id === item.id }"
                  @click="openThread(item)"
                >
                  <n-thing :class="{ 'opacity-45': (item.unread_count ?? 0) <= 0 }">
                    <template #avatar>
                      <NovaIcon
                        icon="icon-park-outline:message"
                        :size="26"
                        class="text-[var(--primary-color)]"
                      />
                    </template>
                    <template #header>
                      <n-ellipsis :line-clamp="1">
                        {{ item.title || t('resource.message.message.thread_title') }}
                      </n-ellipsis>
                    </template>
                    <template v-if="item.unread_count" #header-extra>
                      <n-badge type="warning" :value="item.unread_count" :max="99" />
                    </template>
                    <template #description>
                      <n-ellipsis :line-clamp="2">
                        {{
                          item.last_message?.content || t('resource.message.message.no_messages')
                        }}
                      </n-ellipsis>
                    </template>
                    <template #footer>
                      {{ displayTime(item.last_message_at || item.updated_at || item.created_at) }}
                    </template>
                  </n-thing>
                </n-list-item>
                <div v-if="threads.records.length < threads.total" class="py-3 text-center">
                  <n-button
                    text
                    size="small"
                    :loading="threads.loading"
                    @click.stop="loadMoreThreads"
                  >
                    {{ t('app.notice.load_more') }}
                  </n-button>
                </div>
              </n-list>
            </n-scrollbar>
          </n-tab-pane>
        </n-tabs>
      </n-el>

      <n-el
        v-if="showDetailPane"
        tag="section"
        class="min-h-120 w-full min-w-0 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] p-4"
      >
        <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <n-button
            v-if="appStore.isMobile"
            quaternary
            size="small"
            :focusable="false"
            class="-ml-1 shrink-0"
            @click="backToList"
          >
            <template #icon>
              <NovaIcon icon="icon-park-outline:arrow-left" />
            </template>
            {{ t('app.message_center.back_to_list') }}
          </n-button>
          <div class="min-w-0">
            <h2 class="truncate text-lg font-750">{{ selectedTitle }}</h2>
            <p class="mt-1 text-sm text-[var(--text-color-3)]">
              {{
                activeTab === 'notifications'
                  ? t('app.message_center.notification_detail')
                  : t('app.message_center.thread_detail')
              }}
            </p>
          </div>
        </div>

        <n-spin :show="detail.loading">
          <template v-if="activeTab === 'notifications'">
            <n-empty
              v-if="!detail.notification"
              class="py-24"
              :description="t('app.message_center.select_notification')"
            />
            <div v-else class="space-y-4">
              <n-descriptions
                :label-placement="appStore.isMobile ? 'top' : 'left'"
                bordered
                :column="1"
              >
                <n-descriptions-item :label="t('resource.message.notification.title_field')">
                  {{ displayValue(detail.notification.title) }}
                </n-descriptions-item>
                <n-descriptions-item :label="t('resource.message.notification.severity')">
                  <n-tag
                    :color="
                      createTagColor(
                        dictTypeColor('NOTIFICATION_SEVERITY', detail.notification.severity),
                      )
                    "
                    :bordered="false"
                  >
                    {{
                      dictTypeData('NOTIFICATION_SEVERITY', detail.notification.severity) ||
                      displayValue(detail.notification.severity)
                    }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item :label="t('common.often.status')">
                  <n-tag
                    :type="statusTagType(Boolean(detail.notification.is_read))"
                    :bordered="false"
                  >
                    {{
                      detail.notification.is_read ? t('app.notice.read') : t('app.notice.unread')
                    }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item :label="t('resource.message.notification.publish_at')">
                  {{
                    displayValue(
                      displayTime(detail.notification.publish_at || detail.notification.created_at),
                    )
                  }}
                </n-descriptions-item>
                <n-descriptions-item :label="t('resource.message.notification.content')">
                  <div class="whitespace-pre-wrap leading-7">
                    {{ displayValue(detail.notification.content) }}
                  </div>
                </n-descriptions-item>
              </n-descriptions>
            </div>
          </template>

          <template v-else>
            <n-empty
              v-if="!detail.thread"
              class="py-24"
              :description="t('app.message_center.select_thread')"
            />
            <div v-else class="flex min-h-[calc(100vh-300px)] flex-col">
              <n-descriptions
                class="mb-4"
                :label-placement="appStore.isMobile ? 'top' : 'left'"
                bordered
                :column="1"
              >
                <n-descriptions-item :label="t('resource.message.message.thread_title')">
                  {{ displayValue(detail.thread.title) }}
                </n-descriptions-item>
                <n-descriptions-item :label="t('resource.message.message.thread_type')">
                  <n-tag
                    :color="
                      createTagColor(
                        dictTypeColor('MESSAGE_THREAD_TYPE', detail.thread.thread_type),
                      )
                    "
                    :bordered="false"
                  >
                    {{
                      dictTypeData('MESSAGE_THREAD_TYPE', detail.thread.thread_type) ||
                      displayValue(detail.thread.thread_type)
                    }}
                  </n-tag>
                </n-descriptions-item>
              </n-descriptions>

              <n-scrollbar
                class="min-h-64 flex-1 rounded-2 border border-[var(--border-color)] p-3"
              >
                <n-empty
                  v-if="!sortedThreadMessages.length"
                  class="py-16"
                  :description="t('resource.message.message.no_messages')"
                />
                <div v-else class="space-y-3">
                  <div
                    v-for="item in sortedThreadMessages"
                    :key="item.id"
                    class="rounded-2 border border-[var(--border-color)] p-3"
                  >
                    <div class="flex items-center justify-between gap-3">
                      <div class="font-650">
                        {{
                          item.sender_name ||
                          item.sender_account_id ||
                          t('resource.message.message.system_sender')
                        }}
                      </div>
                      <div class="shrink-0 text-xs text-[var(--text-color-3)]">
                        {{ displayTime(item.created_at) }}
                      </div>
                    </div>
                    <div
                      class="mt-2 whitespace-pre-wrap text-sm leading-6 text-[var(--text-color-2)]"
                    >
                      {{ displayValue(item.content) }}
                    </div>
                  </div>
                </div>
              </n-scrollbar>

              <div class="mt-4">
                <n-input
                  v-model:value="replyContent"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                  :placeholder="t('resource.message.message.reply_placeholder')"
                />
                <div class="mt-3 flex justify-end">
                  <n-button
                    type="primary"
                    :focusable="false"
                    :loading="detail.actionLoading"
                    @click="replyMessage"
                  >
                    {{ t('resource.message.message.reply') }}
                  </n-button>
                </div>
              </div>
            </div>
          </template>
        </n-spin>
      </n-el>
    </div>
  </section>
</template>

<style scoped>
.messages-tabs :deep(.n-tabs-nav) {
  padding: 0 16px;
}

.messages-tabs :deep(.n-tab-pane) {
  padding-top: 0;
}

.is-active-list-item {
  background: var(--button-color-2-hover);
}
</style>
