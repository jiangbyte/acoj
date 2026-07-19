<script setup lang="ts">
import { inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { type MockFriend, type MockGroup } from '../mock'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NTabs v-model:value="ui.contactTab.value" type="segment" size="small" class="px-4 pt-3">
      <NTabPane name="friends" tab="好友">
        <NScrollbar class="h-full">
          <NList v-if="data.friends.length" hoverable clickable>
            <NListItem v-for="friend in data.friends" :key="friend.id" class="im-list-item cursor-pointer"
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
          <NEmpty v-else class="py-12" description="暂无好友" />
        </NScrollbar>
      </NTabPane>
      <NTabPane name="groups" tab="群组">
        <NScrollbar class="h-full">
          <NList v-if="data.groups.length" hoverable clickable>
            <NListItem v-for="group in data.groups" :key="group.id" class="im-list-item cursor-pointer"
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
          <NEmpty v-else class="py-12" description="暂无群组" />
        </NScrollbar>
      </NTabPane>
    </NTabs>
  </div>
</template>
