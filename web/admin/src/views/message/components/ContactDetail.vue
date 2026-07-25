<script setup lang="ts">
import { computed, inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { resolveFileUrl } from '@/utils'
import type { Friend, Group } from '../types'
import { MESSAGE_UI_STATE_KEY } from '../provide-keys'

const props = defineProps<{
  friend: Friend | null
  group: Group | null
  hint: string
}>()

const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const avatarUrl = computed(
  () => resolveFileUrl(props.friend?.avatar) || resolveFileUrl(props.group?.avatar) || undefined,
)

const emit = defineEmits<{
  chat: []
  removeFriend: []
  leaveGroup: []
  back: []
}>()

const themeVars = useThemeVars()
const ui = inject(MESSAGE_UI_STATE_KEY)!
</script>

<template>
  <NCard
    :bordered="false"
    class="h-full min-h-0 overflow-hidden shadow-sm"
    :content-style="{ height: '100%', padding: '0' }"
  >
    <template v-if="friend || group">
      <div class="flex h-full min-h-0 flex-col">
        <NScrollbar class="h-full">
          <div class="mx-auto flex w-full max-w-[460px] flex-col gap-4 px-4 py-6">
            <div v-if="ui.isMobile.value" class="flex justify-start">
              <NButton text size="small" @click="emit('back')">
                <template #icon>
                  <NovaIcon icon="icon-park-outline:arrow-left" :size="18" />
                </template>
              </NButton>
            </div>
            <NAlert v-if="hint" type="success" :bordered="false">
              {{ hint }}
            </NAlert>
            <div class="flex items-center gap-3">
              <NAvatar
                v-if="avatarUrl"
                round
                :size="64"
                class="shrink-0"
                :src="avatarUrl"
                :img-props="avatarImgProps"
              />
              <NAvatar v-else round :size="64" class="shrink-0">
                {{ (friend?.name || group?.name || '?').charAt(0) }}
              </NAvatar>
              <div class="min-w-0 text-left">
                <div class="truncate text-lg font-600">
                  {{ friend?.name || group?.name }}
                </div>
                <div class="truncate text-xs" :style="{ color: themeVars.textColor3 }">
                  {{ friend ? friend.signature || '-' : group?.description || '-' }}
                </div>
              </div>
            </div>
            <NDescriptions :column="1" label-placement="left" size="small">
              <template v-if="friend">
                <NDescriptionsItem label="备注">
                  {{ friend.remark || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="签名">
                  {{ friend.signature || '-' }}
                </NDescriptionsItem>
              </template>
              <template v-else-if="group">
                <NDescriptionsItem label="成员"> {{ group.member_count }} 人 </NDescriptionsItem>
                <NDescriptionsItem label="说明">
                  {{ group.description || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="状态">
                  {{ group.status }}
                </NDescriptionsItem>
              </template>
            </NDescriptions>
            <NFlex justify="center" :wrap="true" :size="12">
              <NButton type="primary" @click="emit('chat')"> 发消息 </NButton>
              <NButton v-if="friend" tertiary type="error" @click="emit('removeFriend')">
                删除好友
              </NButton>
              <NButton v-else tertiary type="error" @click="emit('leaveGroup')"> 退出群聊 </NButton>
            </NFlex>
          </div>
        </NScrollbar>
      </div>
    </template>
    <NEmpty v-else class="h-full flex items-center justify-center" description="请选择联系人" />
  </NCard>
</template>
