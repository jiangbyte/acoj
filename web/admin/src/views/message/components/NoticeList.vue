<script setup lang="ts">
import { inject, computed } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime, resolveFileUrl } from '@/utils'
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
import { MESSAGE_ACTIONS_KEY, MESSAGE_UI_STATE_KEY, MESSAGE_DATA_KEY } from '../provide-keys'

const data = inject(MESSAGE_DATA_KEY)!
const themeVars = useThemeVars()
const actions = inject(MESSAGE_ACTIONS_KEY)!
const ui = inject(MESSAGE_UI_STATE_KEY)!

// Sorted by backend

const unreadNoticeCount = computed(() => data.notices.filter((n) => !n.is_read).length)
const requestBadgeCount = computed(
  () =>
    data.friendRequests.filter((r: any) => r.status === 'PENDING').length +
    data.groupJoinRequests.filter((r: any) => r.status === 'PENDING').length +
    data.pendingGroupJoinRequests.length,
)

const combinedFriendItems = computed(() => data.friendRequests)

const combinedGroupItems = computed(() => [
  ...data.groupJoinRequests,
  ...data.pendingGroupJoinRequests,
])
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NTabs v-model:value="ui.noticeTab.value" type="segment" size="small" class="px-4 pt-3">
      <NTabPane name="notices" :tab="`通知 ${unreadNoticeCount ? `(${unreadNoticeCount})` : ''}`">
        <NScrollbar class="h-full">
          <NList v-if="data.notices.length" hoverable>
            <NListItem
              v-for="notice in data.notices"
              :key="notice.id"
              class="message-list-item cursor-pointer"
              @click="actions.openNoticeDetail(notice)"
            >
              <div class="flex items-start gap-3 px-4 py-3">
                <NAvatar
                  round
                  :size="40"
                  class="shrink-0"
                  :style="{
                    backgroundColor:
                      notice.severity === 'error'
                        ? 'var(--error-color)'
                        : notice.severity === 'warning'
                          ? 'var(--warning-color)'
                          : 'var(--info-color)',
                  }"
                >
                  {{
                    notice.severity === 'error' ? '!' : notice.severity === 'warning' ? '!' : 'i'
                  }}
                </NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span
                        class="message-ellipsis text-sm"
                        :class="{ 'font-700': !notice.is_read }"
                        >{{ notice.title }}</span
                      >
                      <NTag v-if="!notice.is_read" :bordered="false" size="tiny" type="primary">
                        新
                      </NTag>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{
                      formatDateTime(notice.created_at)
                    }}</span>
                  </div>
                  <div
                    class="message-ellipsis mt-1 text-xs"
                    :style="{ color: themeVars.textColor3 }"
                  >
                    {{ notice.content }}
                  </div>
                </div>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-12" description="暂无通知" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="requests" :tab="`申请 ${requestBadgeCount ? `(${requestBadgeCount})` : ''}`">
        <NScrollbar class="h-full">
          <template v-if="combinedFriendItems.length || combinedGroupItems.length">
            <div class="divide-y divide-gray-100/60">
              <div
                v-for="req in combinedFriendItems"
                :key="'f-' + req.id"
                class="flex items-start gap-3 px-4 py-3 cursor-pointer transition-colors hover:bg-gray-50/50 select-none relative"
                @click="actions.openPendingDetail(req)"
              >
                <div
                  v-if="req.status !== 'PENDING'"
                  class="absolute z-10 pointer-events-none select-none"
                  style="
                    right: 6px;
                    bottom: 6px;
                    padding: 1px 8px;
                    border-width: 2px;
                    border-style: solid;
                    border-radius: 3px;
                    transform: rotate(-15deg);
                    opacity: 0.7;
                    font-size: 11px;
                    font-weight: 700;
                    line-height: 1.5;
                    background: white;
                  "
                  :style="
                    req.status === 'ACCEPTED'
                      ? 'color:#18a058;border-color:#18a058;'
                      : 'color:#d03050;border-color:#d03050;'
                  "
                >
                  {{ req.status === 'ACCEPTED' ? '已通过' : '已拒绝' }}
                </div>
                <NAvatar
                  v-if="req.applicant_avatar"
                  round
                  :size="40"
                  class="shrink-0"
                  :src="resolveFileUrl(req.applicant_avatar)"
                  :img-props="avatarImgProps"
                />
                <NAvatar v-else round :size="40" class="shrink-0">
                  {{ req.applicant_name?.charAt(0) || '?' }}
                </NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span class="message-ellipsis text-sm font-700">{{
                        req.applicant_name
                      }}</span>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{
                      formatDateTime(req.created_at)
                    }}</span>
                  </div>
                  <div
                    class="message-ellipsis mt-1 text-xs"
                    :style="{ color: themeVars.textColor3 }"
                  >
                    {{ req.message || '-' }}
                  </div>
                </div>
              </div>
              <div
                v-for="req in combinedGroupItems"
                :key="'g-' + req.id"
                class="flex items-start gap-3 px-4 py-3 cursor-pointer transition-colors hover:bg-gray-50/50 select-none relative"
                @click="actions.openPendingDetail(req)"
              >
                <div
                  v-if="req.status !== 'PENDING'"
                  class="absolute z-10 pointer-events-none select-none"
                  style="
                    right: 6px;
                    bottom: 6px;
                    padding: 1px 8px;
                    border-width: 2px;
                    border-style: solid;
                    border-radius: 3px;
                    transform: rotate(-15deg);
                    opacity: 0.7;
                    font-size: 11px;
                    font-weight: 700;
                    line-height: 1.5;
                    background: white;
                  "
                  :style="
                    req.status === 'ACCEPTED'
                      ? 'color:#18a058;border-color:#18a058;'
                      : 'color:#d03050;border-color:#d03050;'
                  "
                >
                  {{ req.status === 'ACCEPTED' ? '已通过' : '已拒绝' }}
                </div>
                <NAvatar
                  v-if="req.applicant_avatar"
                  round
                  :size="40"
                  class="shrink-0"
                  :src="resolveFileUrl(req.applicant_avatar)"
                  :img-props="avatarImgProps"
                />
                <NAvatar v-else round :size="40" class="shrink-0">
                  {{ req.applicant_name?.charAt(0) || '?' }}
                </NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex items-center gap-2">
                      <span class="message-ellipsis text-sm font-700">{{
                        req.group_name || req.applicant_name
                      }}</span>
                    </div>
                    <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{
                      formatDateTime(req.created_at)
                    }}</span>
                  </div>
                  <div
                    class="message-ellipsis mt-1 text-xs"
                    :style="{ color: themeVars.textColor3 }"
                  >
                    {{ req.group_name || req.message || '-' }}
                  </div>
                </div>
              </div>
            </div>
          </template>
          <NEmpty v-else class="py-12" description="暂无待处理申请" />
        </NScrollbar>
      </NTabPane>
    </NTabs>
  </div>
</template>
