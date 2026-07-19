<script setup lang="ts">
import { inject, computed } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime } from '@/utils'
import type { MockTodoItem } from '../mock'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!
const pendingTodoCount = computed(() => data.todos.filter((t) => t.status === 'pending').length)

const filteredTodos = computed(() => {
  if (ui.todoTab.value === 'pending') return data.todos.filter((t) => t.status === 'pending')
  return data.todos.filter((t) => t.status !== 'pending')
})

function getPriorityType(p: string) {
  if (p === 'urgent') return 'error' as const
  if (p === 'high') return 'warning' as const
  if (p === 'medium') return 'info' as const
  return 'default' as const
}

function getPriorityLabel(p: string) {
  if (p === 'urgent') return '紧急'
  if (p === 'high') return '高'
  if (p === 'medium') return '中'
  return '低'
}

function getTodoStatusLabel(s: string) {
  if (s === 'pending') return '待处理'
  if (s === 'done') return '已完成'
  return '已取消'
}

function markDone(todo: MockTodoItem) {
  todo.status = 'done'
}
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NTabs v-model:value="ui.todoTab.value" type="segment" size="small" class="px-4 pt-3">
      <NTabPane name="pending" :tab="`待处理 ${pendingTodoCount ? `(${pendingTodoCount})` : ''}`">
        <NScrollbar class="h-full">
          <NList v-if="filteredTodos.filter(t => t.status === 'pending').length" hoverable>
            <NListItem v-for="todo in filteredTodos" :key="todo.id" class="im-list-item cursor-pointer"
              @click="actions.openTodoDetail(todo)">
              <div class="flex items-start gap-3 px-4 py-3">
                <div class="shrink-0 mt-1">
                  <NSwitch :value="false" size="small" @click.stop="markDone(todo)" />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span class="im-ellipsis text-sm font-600">{{ todo.title }}</span>
                      <NTag :bordered="false" size="tiny" :type="getPriorityType(todo.priority)">{{ getPriorityLabel(todo.priority) }}</NTag>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(todo.createdAt) }}</span>
                  </div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ todo.content }}</div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无待处理待办" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="done" tab="已处理">
        <NScrollbar class="h-full">
          <NList v-if="filteredTodos.filter(t => t.status !== 'pending').length" hoverable>
            <NListItem v-for="todo in filteredTodos" :key="todo.id" class="im-list-item cursor-pointer" style="opacity: 0.6"
              @click="actions.openTodoDetail(todo)">
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
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(todo.createdAt) }}</span>
                  </div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ todo.content }}</div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无已处理待办" />
        </NScrollbar>
      </NTabPane>
    </NTabs>
  </div>
</template>
