<script setup lang="ts">
import { computed, inject } from 'vue'
import { resolveFileUrl } from '@/utils'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const profile = computed(() => data.profile)
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const totalUnreadCount = computed(() => data.threads.reduce((s, t) => s + (t.unreadCount || 0), 0))
const pendingTodoCount = computed(() => data.todos.filter((t) => t.status === 'pending').length)
const requestBadgeCount = computed(() => data.requests.length)
const unreadNoticeCount = computed(() => data.notices.filter((n) => !n.read).length)
const noticeBadgeTotal = computed(() => requestBadgeCount.value + unreadNoticeCount.value)

const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const avatarUrl = computed(() => resolveFileUrl(profile.value.avatar))
</script>

<template>
  <aside class="hidden h-full min-h-0 flex-col items-center py-3 md:flex" style="background-color: #2b2b2b;">
    <NAvatar
      v-if="avatarUrl"
      round
      :size="40"
      :src="avatarUrl"
      :img-props="avatarImgProps"
      class="shrink-0 cursor-pointer"
      @click="actions.openProfileModal()"
    />
    <NAvatar
      v-else
      round
      :size="40"
      class="shrink-0 cursor-pointer"
      @click="actions.openProfileModal()"
    >
      <NovaIcon icon="icon-park-outline:user" :size="20" />
    </NAvatar>

    <div class="mt-6 flex flex-1 flex-col items-center gap-6">
      <NTooltip placement="right">
        <template #trigger>
          <NBadge :value="totalUnreadCount" :max="99" :show-zero="false">
            <NButton
              text
              :class="ui.activeSection.value === 'chat' ? 'text-[var(--primary-color)]' : 'text-white'"
              aria-label="聊天"
              @click="actions.openChatSection()"
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
            :class="ui.activeSection.value === 'contacts' ? 'text-[var(--primary-color)]' : 'text-white'"
            aria-label="通讯录"
            @click="actions.openContactsSection()"
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
              :class="ui.activeSection.value === 'todos' ? 'text-[var(--primary-color)]' : 'text-white'"
              aria-label="待办"
              @click="actions.openTodosSection()"
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
              :class="ui.activeSection.value === 'notice' ? 'text-[var(--primary-color)]' : 'text-white'"
              aria-label="通知"
              @click="actions.openNoticeSection()"
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
          <NButton
            text
            class="text-white"
            aria-label="返回工作台"
            @click="actions.goHome()"
          >
            <template #icon>
              <NovaIcon icon="icon-park-outline:arrow-left" :size="20" />
            </template>
          </NButton>
        </template>
        返回工作台
      </NTooltip>
    </div>
  </aside>
</template>
