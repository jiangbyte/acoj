<script setup lang="ts">
import { inject, computed } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime } from '@/utils'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const unreadNoticeCount = computed(() => data.notices.filter((n) => !n.read).length)
const requestBadgeCount = data.requests.length
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NTabs v-model:value="ui.noticeTab.value" type="segment" size="small" class="px-4 pt-3">
      <NTabPane name="notices" :tab="`通知 ${unreadNoticeCount ? `(${unreadNoticeCount})` : ''}`">
        <NScrollbar class="h-full">
          <NList v-if="data.notices.length" hoverable>
            <NListItem v-for="notice in data.notices" :key="notice.id" class="im-list-item cursor-pointer"
              @click="actions.openNoticeDetail(notice)">
              <div class="flex items-start gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0" :style="{
                  backgroundColor: notice.severity === 'error' ? 'var(--error-color)' : notice.severity === 'warning' ? 'var(--warning-color)' : 'var(--info-color)',
                }">{{ notice.severity === 'error' ? '!' : notice.severity === 'warning' ? '!' : 'i' }}</NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span class="im-ellipsis text-sm" :class="{ 'font-700': !notice.read }">{{ notice.title }}</span>
                      <NTag v-if="!notice.read" :bordered="false" size="tiny" type="primary">新</NTag>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(notice.createdAt) }}</span>
                  </div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ notice.content }}</div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无通知" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="requests" :tab="`申请 ${requestBadgeCount ? `(${requestBadgeCount})` : ''}`">
        <NScrollbar class="h-full">
          <NList v-if="data.requests?.length" hoverable>
            <NListItem v-for="req in data.requests" :key="req.id" class="im-list-item cursor-pointer"
              @click="actions.openPendingDetail(req)">
              <div class="flex items-start gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ req.avatarText }}</NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span class="im-ellipsis text-sm font-700">{{ req.name }}</span>
                      <NTag :bordered="false" size="tiny" :type="req.mode === 'friend' ? 'success' : 'info'">
                        {{ req.mode === 'friend' ? '好友' : '群组' }}
                      </NTag>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(req.createdAt) }}</span>
                  </div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ req.detail }}</div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无待处理申请" :style="{ gap: '8px' }" />
        </NScrollbar>
      </NTabPane>
    </NTabs>
  </div>
</template>
