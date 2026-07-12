<template>
  <Layout title="消息">
    <view>
      <u-card title="消息概览">
        <template #body>
          <u-grid :col="3" :border="false">
            <u-grid-item>
              <text>{{ summary.notification_unread ?? 0 }}</text>
              <text>通知</text>
            </u-grid-item>
            <u-grid-item>
              <text>{{ summary.message_unread ?? 0 }}</text>
              <text>站内信</text>
            </u-grid-item>
            <u-grid-item>
              <text>{{ summary.todo_pending ?? 0 }}</text>
              <text>待办</text>
            </u-grid-item>
          </u-grid>
        </template>
      </u-card>

      <u-card :show-head="false">
        <template #body>
          <u-tabs :list="tabs" :current="active" @change="changeTab"></u-tabs>
        </template>
      </u-card>

      <u-card
        v-for="item in records"
        :key="item.id"
        :title="item.title || item.subject || item.name"
        :sub-title="formatDateTime(item.created_at || item.updated_at || item.publish_at)"
        @click="openItem(item)"
      >
        <template #body>
          <u-cell-item
            :title="
              item.content ||
              item.description ||
              item.last_message?.content ||
              '-'
            "
            :value="statusText(item)"
            :arrow="false"
          ></u-cell-item>
        </template>
      </u-card>
      <u-empty
        v-if="!records.length && !loading"
        mode="list"
        text="暂无数据"
      ></u-empty>
      <u-loadmore :status="loadStatus"></u-loadmore>
    </view>

    <u-popup
      :show="detailVisible"
      mode="bottom"
      :safe-area-inset-bottom="true"
      @close="detailVisible = false"
    >
      <view class="message-detail">
        <text class="message-detail__title">{{ detailTitle }}</text>
        <view v-for="row in detailRows" :key="row.label" class="message-detail__row">
          <text class="message-detail__label">{{ row.label }}</text>
          <text class="message-detail__value">{{ row.value }}</text>
        </view>
      </view>
    </u-popup>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onReachBottom, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { messageApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { dictTypeData, isDictLoaded, refreshDict } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'

const authStore = useAuthStore()
const tabs = [{ name: '通知' }, { name: '站内信' }, { name: '待办' }]
const active = ref(0)
const summary = ref<Record<string, any>>({})
const records = ref<any[]>([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detailTitle = ref('')
const detailRows = ref<Array<{ label: string; value: string }>>([])
const loadStatus = computed(() =>
  loading.value
    ? 'loading'
    : records.value.length >= total.value
      ? 'nomore'
      : 'loadmore'
)

onShow(async () => {
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  if (!isDictLoaded()) {
    await refreshDict()
  }
  await refresh()
})

onReachBottom(() => {
  if (!loading.value && records.value.length < total.value) {
    current.value += 1
    loadPage(true)
  }
})

async function refresh() {
  summary.value = await messageApi.summary().catch(() => ({}))
  current.value = 1
  await loadPage(false)
}

async function loadPage(append: boolean) {
  loading.value = true
  try {
    const params = { current: current.value, size: 20 }
    const page =
      active.value === 0
        ? await messageApi.myNotification(params)
        : active.value === 1
          ? await messageApi.myThreads(params)
          : await messageApi.myTodos(params)
    total.value = page.total ?? 0
    records.value = append
      ? [...records.value, ...(page.records ?? [])]
      : (page.records ?? [])
  } finally {
    loading.value = false
  }
}

function changeTab(event: number | { index?: number; name?: string }) {
  active.value = normalizeTabIndex(event)
  current.value = 1
  loadPage(false)
}

async function openItem(item: any) {
  const detail = await loadDetail(item)
  detailTitle.value = detail.title || detail.subject || detail.name || '详情'
  detailRows.value = buildDetailRows(detail)
  detailVisible.value = true
}

function statusText(item: any) {
  if (active.value === 0) {
    return (
      dictTypeData('NOTIFICATION_SEVERITY', item.severity) ||
      item.severity ||
      ''
    )
  }
  if (active.value === 2) {
    return dictTypeData('TODO_STATUS', item.status) || item.status || ''
  }
  return item.unread_count ? `${item.unread_count} 未读` : ''
}

function normalizeTabIndex(event: number | { index?: number; name?: string }) {
  if (typeof event === 'number') {
    return event
  }
  if (typeof event.index === 'number') {
    return event.index
  }
  const index = tabs.findIndex((item) => item.name === event.name)
  return index >= 0 ? index : active.value
}

async function loadDetail(item: any) {
  try {
    if (active.value === 0) {
      return await messageApi.myNotificationDetail({ id: item.id })
    }
    if (active.value === 2) {
      return await messageApi.myTodoDetail({ id: item.id })
    }
  } catch {
    return item
  }
  return item
}

function buildDetailRows(item: any) {
  if (active.value === 0) {
    return [
      { label: '内容', value: item.content || '-' },
      { label: '级别', value: statusText(item) || '-' },
      { label: '发布时间', value: formatDateTime(item.publish_at || item.created_at) },
    ]
  }
  if (active.value === 1) {
    return [
      { label: '最近消息', value: item.last_message?.content || item.content || '-' },
      { label: '未读', value: String(item.unread_count ?? 0) },
      { label: '时间', value: formatDateTime(item.last_message_at || item.updated_at) },
    ]
  }
  return [
    { label: '内容', value: item.content || '-' },
    { label: '状态', value: statusText(item) || '-' },
    { label: '截止时间', value: formatDateTime(item.due_at) },
  ]
}
</script>

<style lang="scss" scoped>
.message-detail {
  max-height: 70vh;
  overflow-y: auto;
  padding: var(--space-4);
  background-color: #ffffff;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.message-detail__title {
  display: block;
  margin-bottom: var(--space-4);
  font-size: var(--text-lg);
  font-weight: 600;
  line-height: 1.25;
  color: var(--color-neutral-900);
}

.message-detail__row {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-neutral-200);
}

.message-detail__label {
  font-size: var(--text-sm);
  color: var(--color-neutral-500);
}

.message-detail__value {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--color-neutral-900);
  text-align: right;
}
</style>
