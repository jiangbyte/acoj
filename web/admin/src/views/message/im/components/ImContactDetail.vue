<script setup lang="ts">
import { inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import type { MockFriend, MockGroup } from '../mock'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY } from '../im-provide'

defineProps<{
  friend: MockFriend | null
  group: MockGroup | null
  hint: string
}>()

const emit = defineEmits<{
  chat: []
  removeFriend: []
  leaveGroup: []
  back: []
}>()

const themeVars = useThemeVars()
const ui = inject(IM_UI_STATE_KEY)!

const title = (f: MockFriend | null, g: MockGroup | null) => f?.name ?? g?.name ?? ''
const subtitle = (f: MockFriend | null, g: MockGroup | null) =>
  f ? `${f.title} · ${f.department}` : g ? `${g.memberCount} 人 · ${g.statusText}` : ''
</script>

<template>
  <NCard :bordered="false" class="h-full min-h-0 overflow-hidden shadow-sm" :content-style="{ height: '100%', padding: '0' }">
    <template v-if="friend || group">
      <div class="flex h-full min-h-0 flex-col">
        <NScrollbar class="h-full">
          <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
            <div v-if="ui.isMobile.value" class="flex justify-start">
              <NButton text size="small" @click="emit('back')">
                <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
              </NButton>
            </div>
            <NAlert v-if="hint" type="success" :bordered="false">{{ hint }}</NAlert>
            <div class="flex items-center gap-3">
              <NAvatar round :size="64" class="shrink-0">{{ friend?.avatarText ?? group?.avatarText }}</NAvatar>
              <div class="min-w-0 text-left">
                <div class="truncate text-lg font-600">{{ title(friend, group) }}</div>
                <div class="truncate text-xs" :style="{ color: themeVars.textColor3 }">{{ subtitle(friend, group) }}</div>
              </div>
            </div>
            <NDescriptions :column="1" label-placement="left" size="small">
              <template v-if="friend">
                <NDescriptionsItem label="职位">{{ friend.title }}</NDescriptionsItem>
                <NDescriptionsItem label="部门">{{ friend.department }}</NDescriptionsItem>
                <NDescriptionsItem label="签名">{{ friend.signature }}</NDescriptionsItem>
              </template>
              <template v-else-if="group">
                <NDescriptionsItem label="成员">{{ group.memberCount }} 人</NDescriptionsItem>
                <NDescriptionsItem label="说明">{{ group.description }}</NDescriptionsItem>
                <NDescriptionsItem label="状态">{{ group.statusText }}</NDescriptionsItem>
              </template>
            </NDescriptions>
            <div class="rounded-1 border px-3 py-3 text-sm leading-6" :style="{ borderColor: themeVars.borderColor, backgroundColor: themeVars.cardColor }">
              <template v-if="friend">{{ friend.signature }}</template>
              <template v-else>{{ group?.description }}</template>
            </div>
            <NFlex justify="center" :wrap="true" :size="12">
              <NButton type="primary" @click="emit('chat')">继续聊天</NButton>
              <NButton v-if="friend" tertiary type="error" @click="emit('removeFriend')">删除好友</NButton>
              <NButton v-else tertiary type="error" @click="emit('leaveGroup')">退出群聊</NButton>
            </NFlex>
          </div>
        </NScrollbar>
      </div>
    </template>
    <NEmpty v-else class="h-full flex items-center justify-center" :description="friend || group ? '' : '请选择联系人'" />
  </NCard>
</template>
