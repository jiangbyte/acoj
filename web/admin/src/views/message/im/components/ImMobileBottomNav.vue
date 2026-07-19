<script setup lang="ts">
import { inject, computed } from 'vue'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const totalUnreadCount = computed(() => data.threads.reduce((s, t) => s + (t.unreadCount || 0), 0))
const pendingTodoCount = computed(() => data.todos.filter((t) => t.status === 'pending').length)
const requestBadgeCount = computed(() => data.requests.length)
const unreadNoticeCount = computed(() => data.notices.filter((n) => !n.read).length)
const noticeBadgeTotal = computed(() => requestBadgeCount.value + unreadNoticeCount.value)
</script>

<template>
  <div class="border-t md:hidden" :style="{ borderColor: 'var(--border-color)', backgroundColor: 'var(--body-color)' }">
    <div class="flex items-center justify-around py-1">
      <NButton text :class="ui.activeSection.value === 'chat' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
        style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;" @click="actions.openChatSection()">
        <template #icon>
          <NBadge :value="totalUnreadCount" :max="99" :show-zero="false">
            <NovaIcon icon="icon-park-outline:message" :size="20" />
          </NBadge>
        </template>
        <span style="font-size: 10px; line-height: 1;">聊天</span>
      </NButton>
      <NButton text :class="ui.activeSection.value === 'contacts' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
        style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;" @click="actions.openContactsSection()">
        <template #icon>
          <NovaIcon icon="icon-park-outline:people" :size="20" />
        </template>
        <span style="font-size: 10px; line-height: 1;">通讯录</span>
      </NButton>
      <NButton text :class="ui.activeSection.value === 'todos' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
        style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;" @click="actions.openTodosSection()">
        <template #icon>
          <NBadge :value="pendingTodoCount" :max="99" :show-zero="false">
            <NovaIcon icon="icon-park-outline:checklist" :size="20" />
          </NBadge>
        </template>
        <span style="font-size: 10px; line-height: 1;">待办</span>
      </NButton>
      <NButton text :class="ui.activeSection.value === 'notice' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
        style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;" @click="actions.openNoticeSection()">
        <template #icon>
          <NBadge :value="noticeBadgeTotal" :max="99" :show-zero="false">
            <NovaIcon icon="icon-park-outline:alarm" :size="20" />
          </NBadge>
        </template>
        <span style="font-size: 10px; line-height: 1;">通知</span>
      </NButton>
      <NButton text :class="ui.activeSection.value === 'profile' ? 'text-[var(--primary-color)]' : 'text-[var(--text-color-3)]'"
        style="flex-direction: column; height: auto; gap: 2px; padding: 4px 12px;" @click="actions.openProfileSection()">
        <template #icon>
          <NovaIcon icon="icon-park-outline:user" :size="20" />
        </template>
        <span style="font-size: 10px; line-height: 1;">我的</span>
      </NButton>
    </div>
  </div>
</template>
