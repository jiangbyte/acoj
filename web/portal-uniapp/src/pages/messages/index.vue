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
        :title="item.title || item.subject || item.name || '-'"
        @click="openItem(item)"
      >
        <template #body>
          <view class="message-row">
            <text>{{ contentText(item) }}</text>
            <text>{{ labelText(item) }}</text>
            <text v-if="statusText(item)">{{ statusText(item) }}</text>
          </view>
        </template>
      </u-card>
      <u-empty
        v-if="!records.length && !loading"
        mode="list"
        text="暂无数据"
      ></u-empty>
      <u-loadmore :status="loadStatus"></u-loadmore>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { messageApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { dictTypeData } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'

const authStore = useAuthStore()
const dictStore = useDictStore()
const tabs = [{ name: '通知' }, { name: '站内信' }, { name: '待办' }]
const active = ref(0)
const summary = ref<Record<string, any>>({})
const records = ref<any[]>([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const loadStatus = computed(() =>
  loading.value
    ? 'loading'
    : records.value.length >= total.value
      ? 'nomore'
      : 'loadmore'
)

onShow(async () => {
  if (!authStore.isLogin) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!dictStore.loaded) {
    await dictStore.refreshDict()
  }
  await refresh()
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
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
        ? await messageApi.myNotifications(params)
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

function openItem(item: any) {
  const type =
    active.value === 0 ? 'notification' : active.value === 1 ? 'thread' : 'todo'
  uni.navigateTo({ url: `/pages/messages/detail?type=${type}&id=${item.id}` })
}

function contentText(item: any) {
  return item.content || item.description || item.last_message?.content || '-'
}

function labelText(item: any) {
  return formatDateTime(
    item.created_at ||
      item.updated_at ||
      item.publish_at ||
      item.last_message_at
  )
}

function statusText(item: any) {
  if (active.value === 0)
    return (
      dictTypeData('NOTIFICATION_SEVERITY', item.severity) ||
      item.severity ||
      ''
    )
  if (active.value === 2)
    return dictTypeData('TODO_STATUS', item.status) || item.status || ''
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
</script>

<style lang="scss" scoped>
.message-row {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}
</style>
