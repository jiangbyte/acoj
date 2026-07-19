<script setup lang="ts">
import { computed, inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { createMockImData } from '../mock'
import { formatDateTime } from '@/utils'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY } from '../im-provide'

const props = defineProps<{ keyword: string }>()
const data = createMockImData()
const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const filteredThreads = computed(() => {
  const k = props.keyword
  if (!k) return data.threads
  return data.threads.filter((t) =>
    [t.title, t.subtitle, t.lastMessage].filter(Boolean).some((v) => String(v).toLowerCase().includes(k)),
  )
})

const filteredFriends = computed(() => {
  const k = props.keyword
  if (!k) return data.friends
  return data.friends.filter((f) =>
    [f.name, f.title, f.department, f.signature].filter(Boolean).some((v) => String(v).toLowerCase().includes(k)),
  )
})

const filteredGroups = computed(() => {
  const k = props.keyword
  if (!k) return data.groups
  return data.groups.filter((g) =>
    [g.name, g.description, g.statusText].filter(Boolean).some((v) => String(v).toLowerCase().includes(k)),
  )
})
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NTabs v-model:value="(ui as any).searchScope.value" type="segment" size="small" class="px-4 pt-3">
      <NTabPane name="threads" tab="对话">
        <NScrollbar class="h-full">
          <NList v-if="filteredThreads.length" hoverable clickable>
            <NListItem v-for="thread in filteredThreads" :key="thread.id" class="im-list-item cursor-pointer"
              @click="actions.openThread(thread.id)">
              <div class="im-list-row flex items-start gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ thread.avatarText }}</NAvatar>
                <div class="im-list-body">
                  <div class="im-list-main-line flex items-center justify-between gap-3">
                    <span class="im-ellipsis flex-1 text-sm font-600">{{ thread.title }}</span>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(thread.lastMessageAt) }}</span>
                  </div>
                  <span class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ thread.lastMessage }}</span>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无对话结果" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="users" tab="用户">
        <NScrollbar class="h-full">
          <NList v-if="filteredFriends.length" hoverable clickable>
            <NListItem v-for="friend in filteredFriends" :key="friend.id" class="im-list-item cursor-pointer"
              @click="actions.openFriend(friend)">
              <div class="im-list-row flex items-start gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ friend.avatarText }}</NAvatar>
                <div class="im-list-body">
                  <div class="im-list-main-line flex items-center justify-between gap-3">
                    <span class="im-ellipsis flex-1 text-sm font-600">{{ friend.name }}</span>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ friend.statusText }}</span>
                  </div>
                  <span class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ friend.title }} · {{ friend.department }}</span>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无用户结果" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="groups" tab="群组">
        <NScrollbar class="h-full">
          <NList v-if="filteredGroups.length" hoverable clickable>
            <NListItem v-for="group in filteredGroups" :key="group.id" class="im-list-item cursor-pointer"
              @click="actions.openGroup(group)">
              <div class="im-list-row flex items-start gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ group.avatarText }}</NAvatar>
                <div class="im-list-body">
                  <div class="im-list-main-line flex items-center justify-between gap-3">
                    <span class="im-ellipsis flex-1 text-sm font-600">{{ group.name }}</span>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ group.statusText }}</span>
                  </div>
                  <span class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ group.memberCount }} 人 · {{ group.description }}</span>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无群组结果" />
        </NScrollbar>
      </NTabPane>
    </NTabs>
  </div>
</template>
