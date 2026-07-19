<script setup lang="ts">
import { inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime } from '@/utils'
import type { MockApplicationRequest, MockSystemNotice, MockTodoItem } from '../mock'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY } from '../im-provide'

defineProps<{
  request: MockApplicationRequest | null
  notice: MockSystemNotice | null
  todo: MockTodoItem | null
}>()

const emit = defineEmits<{
  acceptRequest: [request: MockApplicationRequest]
  rejectRequest: [request: MockApplicationRequest]
  markTodoDone: [todo: MockTodoItem]
  markTodoCancelled: [todo: MockTodoItem]
  close: []
}>()

const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

function getPriorityLabel(p: string) {
  if (p === 'urgent') return '紧急'
  if (p === 'high') return '高'
  if (p === 'medium') return '中'
  return '低'
}

function getPriorityType(p: string) {
  if (p === 'urgent') return 'error' as const
  if (p === 'high') return 'warning' as const
  if (p === 'medium') return 'info' as const
  return 'default' as const
}

function getTodoStatusLabel(s: string) {
  if (s === 'pending') return '待处理'
  if (s === 'done') return '已完成'
  return '已取消'
}
</script>

<template>
  <NCard :bordered="false" class="h-full min-h-0 overflow-hidden shadow-sm" :content-style="{ height: '100%', padding: '0' }">
    <!-- 好友/入群申请详情 -->
    <template v-if="request">
      <div class="flex h-full min-h-0 flex-col">
        <NScrollbar class="h-full">
          <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
            <div v-if="ui.isMobile.value" class="flex justify-start">
              <NButton text size="small" @click="emit('close')">
                <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
              </NButton>
            </div>
            <div class="flex items-center gap-3">
              <NAvatar round :size="64" class="shrink-0">{{ request.avatarText }}</NAvatar>
              <div class="min-w-0 text-left">
                <div class="truncate text-lg font-600">{{ request.name }}</div>
                <div class="truncate text-xs" :style="{ color: themeVars.textColor3 }">{{ request.subtitle }}</div>
              </div>
            </div>
            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem label="类型">{{ request.mode === 'friend' ? '好友申请' : '入群申请' }}</NDescriptionsItem>
              <NDescriptionsItem label="说明">{{ request.detail }}</NDescriptionsItem>
              <NDescriptionsItem label="时间">{{ formatDateTime(request.createdAt) }}</NDescriptionsItem>
            </NDescriptions>
            <NFlex justify="center" :wrap="true" :size="12">
              <NButton type="primary" @click="emit('acceptRequest', request)">通过</NButton>
              <NButton tertiary type="error" @click="emit('rejectRequest', request)">拒绝</NButton>
            </NFlex>
          </div>
        </NScrollbar>
      </div>
    </template>
    <!-- 系统通知详情 -->
    <template v-else-if="notice">
      <div class="flex h-full min-h-0 flex-col">
        <NScrollbar class="h-full">
          <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
            <div v-if="ui.isMobile.value" class="flex justify-start">
              <NButton text size="small" @click="emit('close')">
                <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
              </NButton>
            </div>
            <div class="flex items-center gap-3">
              <NAvatar round :size="64" class="shrink-0" :style="{
                backgroundColor: notice.severity === 'error' ? 'var(--error-color)' : notice.severity === 'warning' ? 'var(--warning-color)' : 'var(--primary-color)',
              }">{{ notice.severity === 'error' ? '!' : notice.severity === 'warning' ? '!' : 'i' }}</NAvatar>
              <div class="min-w-0 text-left">
                <div class="truncate text-lg font-600">{{ notice.title }}</div>
                <div class="truncate text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(notice.createdAt) }}</div>
              </div>
            </div>
            <NAlert v-if="notice.severity === 'error'" type="error" :bordered="false">严重通知</NAlert>
            <NAlert v-else-if="notice.severity === 'warning'" type="warning" :bordered="false">重要通知</NAlert>
            <div class="rounded-1 border px-4 py-4 text-sm leading-7 whitespace-pre-wrap" :style="{ borderColor: themeVars.borderColor, backgroundColor: themeVars.cardColor }">
              {{ notice.content }}
            </div>
            <NFlex justify="center" :wrap="true" :size="12">
              <NButton @click="emit('close')">关闭</NButton>
            </NFlex>
          </div>
        </NScrollbar>
      </div>
    </template>
    <!-- 待办详情 -->
    <template v-else-if="todo">
      <div class="flex h-full min-h-0 flex-col">
        <NScrollbar class="h-full">
          <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
            <div v-if="ui.isMobile.value" class="flex justify-start">
              <NButton text size="small" @click="emit('close')">
                <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
              </NButton>
            </div>
            <div class="flex items-center gap-3">
              <div class="shrink-0 flex items-center justify-center w-[64px] h-[64px] rounded-full" :style="{
                backgroundColor: todo.priority === 'urgent' ? 'var(--error-color)' : todo.priority === 'high' ? 'var(--warning-color)' : 'var(--primary-color)',
                color: '#fff', fontSize: '20px',
              }">
                <NovaIcon :icon="todo.status === 'done' ? 'icon-park-outline:check-one' : 'icon-park-outline:file-text'" :size="28" />
              </div>
              <div class="min-w-0 text-left">
                <div class="truncate text-lg font-600">{{ todo.title }}</div>
                <div class="truncate text-xs" :style="{ color: themeVars.textColor3 }">{{ getTodoStatusLabel(todo.status) }} · {{ formatDateTime(todo.createdAt) }}</div>
              </div>
            </div>
            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem label="优先级">
                <NTag :bordered="false" size="small" :type="getPriorityType(todo.priority)">{{ getPriorityLabel(todo.priority) }}</NTag>
              </NDescriptionsItem>
              <NDescriptionsItem label="截止时间">{{ formatDateTime(todo.dueAt) }}</NDescriptionsItem>
              <NDescriptionsItem label="状态">
                <NTag :bordered="false" size="small" :type="todo.status === 'done' ? 'success' : todo.status === 'cancelled' ? 'default' : 'warning'">{{ getTodoStatusLabel(todo.status) }}</NTag>
              </NDescriptionsItem>
            </NDescriptions>
            <div class="rounded-1 border px-4 py-4 text-sm leading-7 whitespace-pre-wrap" :style="{ borderColor: themeVars.borderColor, backgroundColor: themeVars.cardColor }">
              {{ todo.content }}
            </div>
            <NFlex v-if="todo.status === 'pending'" justify="center" :wrap="true" :size="12">
              <NButton type="primary" @click="emit('markTodoDone', todo)">标记完成</NButton>
              <NButton tertiary @click="emit('markTodoCancelled', todo)">取消</NButton>
            </NFlex>
            <NFlex v-else justify="center" :wrap="true" :size="12">
              <NButton @click="emit('close')">关闭</NButton>
            </NFlex>
          </div>
        </NScrollbar>
      </div>
    </template>
    <NEmpty v-else class="grid h-full place-items-center" description="请选择通知项查看" />
  </NCard>
</template>
