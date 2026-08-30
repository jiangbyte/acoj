<!-- Author: Charlie -->

<script setup lang="ts">
import { myNoticeApi } from '@/api'
import MessageDetailModal from '@/components/sys/MessageDetailModal.vue'
import MessageListItem from '@/components/sys/MessageListItem.vue'
import { useMessageUnreadStore } from '@/stores'
import { dictTypeData, formatDateTime, plainTextExcerpt, wireBool } from '@/utils'
import { onMounted, reactive, ref } from 'vue'
import { readPageMeta } from '@/utils/wire'

const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
const unreadStore = useMessageUnreadStore()
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  page: 1,
  pageSize: 10,
})

onMounted(() => {
  void fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await myNoticeApi.myPage({
      current: state.page,
      size: state.pageSize,
    })
    const data = response.data ?? {}
    state.rows = (data.records ?? []).map((row: any) => ({
      ...row,
      is_read: wireBool(row.is_read ?? false),
    }))
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
  } finally {
    state.loading = false
  }
}

function kindLabel(row: any) {
  return row.kind === 'ANNOUNCEMENT' ? '公告' : '通知'
}

function kindType(row: any) {
  return row.kind === 'ANNOUNCEMENT' ? 'warning' : 'info'
}

function severityLabel(row: any) {
  if (!row.severity) return ''
  return dictTypeData('NOTIFICATION_SEVERITY', row.severity) || row.severity
}

function messageIcon(row: any) {
  return row.kind === 'ANNOUNCEMENT'
    ? 'icon-park-outline:volume-notice'
    : 'icon-park-outline:tips-one'
}

async function openDetail(row: any) {
  await detailModalRef.value?.open({
    id: row.id,
    sourceType: row.kind === 'ANNOUNCEMENT' ? 'ANNOUNCEMENT' : 'NOTIFICATION',
    title: row.title,
    is_read: row.is_read,
    publish_at: row.publish_at,
    content: row.content,
    severity: row.severity,
    content_type: row.content_type,
    kind: row.kind,
  })
}

async function handleDetailChanged(payload: { type: string; id: string }) {
  const row = state.rows.find((item) => item.id === payload.id)
  if (row) row.is_read = true
}

async function markAllRead() {
  await myNoticeApi.readAll()
  state.rows.forEach((row) => {
    row.is_read = true
  })
  unreadStore.notifyReadAll()
  window.$message.success('已全部标记为已读')
}

function handlePageChange(page: number) {
  state.page = page
  void fetchPage()
}

function handlePageSizeChange(size: number) {
  state.pageSize = size
  state.page = 1
  void fetchPage()
}
</script>

<template>
  <div class="msg-feed w-full min-w-0">
    <NSpace
      justify="end"
      class="msg-feed__toolbar"
    >
      <NButton
        text
        :loading="state.loading"
        @click="fetchPage"
      >
        刷新
      </NButton>
      <NButton
        text
        @click="markAllRead"
      >
        全部已读
      </NButton>
    </NSpace>

    <NSpin
      :show="state.loading"
      class="w-full min-w-0"
    >
      <NEmpty
        v-if="!state.loading && !state.rows.length"
        description="暂无消息"
        size="small"
      />
      <NList
        v-else
        hoverable
        clickable
        class="msg-feed__list"
      >
        <NListItem
          v-for="row in state.rows"
          :key="row.id"
          @click="openDetail(row)"
        >
          <MessageListItem
            :title="row.title"
            :time="formatDateTime(row.publish_at || row.created_at)"
            :excerpt="plainTextExcerpt(row.content, 120)"
            :icon="messageIcon(row)"
            :is-read="row.is_read"
            :kind-label="kindLabel(row)"
            :kind-type="kindType(row)"
            :severity-label="severityLabel(row)"
          />
        </NListItem>
      </NList>
    </NSpin>

    <div
      v-if="state.total > 0"
      class="msg-feed__pager"
    >
      <NPagination
        size="small"
        :page="state.page"
        :page-size="state.pageSize"
        :item-count="state.total"
        :page-sizes="[10, 20, 30]"
        show-size-picker
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <MessageDetailModal
      ref="detailModalRef"
      @changed="handleDetailChanged"
    />
  </div>
</template>

<style scoped>
.msg-feed {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.msg-feed__toolbar {
  margin-bottom: 4px;
}

.msg-feed__list :deep(.n-list-item) {
  padding: 10px 0;
}

.msg-feed__list :deep(.n-list-item:not(:last-child)) {
  border-bottom: 1px solid color-mix(in srgb, #000 6%, transparent);
}

html.dark .msg-feed__list :deep(.n-list-item:not(:last-child)) {
  border-bottom-color: color-mix(in srgb, #fff 8%, transparent);
}

.msg-feed__pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}
</style>
