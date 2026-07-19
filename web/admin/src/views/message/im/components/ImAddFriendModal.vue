<script setup lang="ts">
import { computed, inject, reactive, ref, watch } from 'vue'
import { useThemeVars } from 'naive-ui'
import type { MockDirectoryUser, MockDirectoryGroup } from '../mock'
import { searchUsersApi, applyFriendApi, applyJoinGroupApi } from '../im-api'
import { IM_ACTIONS_KEY, IM_DATA_KEY } from '../im-provide'

const themeVars = useThemeVars()
const data = inject(IM_DATA_KEY)!
const actions = inject(IM_ACTIONS_KEY)!

type AddMode = 'friend' | 'group'

const props = withDefaults(defineProps<{ initialMode?: AddMode }>(), { initialMode: 'friend' })
const show = defineModel<boolean>('show', { required: true })
const addMode = ref<AddMode>(props.initialMode)
const addSearchText = ref('')
const addSearchResults = ref<any[]>([])
const addSearchLoading = ref(false)

const addFriendUsers = computed(() => data.directoryUsers)
const addGroupResults = computed(() => data.groups.map(g => ({ id: g.id, name: g.name, avatarText: g.avatarText, memberCount: g.memberCount, description: g.description })))
const addSearchLabel = computed(() => addMode.value === 'friend' ? '搜索用户' : '搜索群组')

const addModeCount = computed(() => {
  if (addMode.value === 'friend') return addSearchResults.value.length || addFriendUsers.value.length
  return addGroupResults.value.length
})

let searchTimer: ReturnType<typeof setTimeout> | null = null
watch(() => show.value, (v) => { if (v) addMode.value = props.initialMode })
watch(addSearchText, (val) => {
  if (searchTimer) clearTimeout(searchTimer)
  const keyword = val.trim()
  if (!keyword) { addSearchResults.value = []; return }
  addSearchLoading.value = true
  searchTimer = setTimeout(async () => {
    try {
      const results = await searchUsersApi(keyword)
      addSearchResults.value = results.map((r: any) => ({
        id: `${r.account_type}-${r.account_id}`,
        name: r.name || '',
        subtitle: r.title || r.department || '',
        avatarText: (r.name || '?').charAt(0),
        accountType: r.account_type,
        accountId: r.account_id,
        isFriend: r.is_friend,
      }))
    } catch { addSearchResults.value = [] }
    finally { addSearchLoading.value = false }
  }, 300)
})

const requestList = reactive<Array<{ name: string; mode: string }>>([])

function applyForFriend(user: any) {
  const exists = requestList.some((i) => i.name === user.name && i.mode === 'friend')
  if (exists) return
  requestList.push({ name: user.name, mode: 'friend' })
  if (user.accountType && user.accountId) applyFriendApi(user.accountType, user.accountId)
}

function applyForGroup(group: MockDirectoryGroup) {
  const exists = requestList.some((i) => i.name === group.name && i.mode === 'group')
  if (exists) return
  requestList.push({ name: group.name, mode: 'group' })
  applyJoinGroupApi(group.id)
}

function getFriendStatus(user: any) {
  if (user.isFriend) return 'accepted'
  if (requestList.some((i) => i.name === user.name && i.mode === 'friend')) return 'pending'
  return 'idle'
}

function getGroupStatus(group: MockDirectoryGroup) {
  if (requestList.some((i) => i.name === group.name && i.mode === 'group')) return 'pending'
  return 'idle'
}

function closeModal() {
  show.value = false
  addSearchText.value = ''
  addSearchResults.value = []
}
</script>

<template>
  <NModal v-model:show="show" preset="card" :bordered="false" draggable
    title="添加好友 / 群聊" :mask-closable="false"
    style="width: min(700px, calc(100vw - 24px)); height: 75vh;"
    content-style="display: flex; flex-direction: column; height: 65vh; padding: 0 20px 20px"
    @update:show="closeModal"
  >
    <div class="flex min-h-0 flex-1 flex-col gap-4">
      <NTabs v-model:value="addMode" type="segment" size="small">
        <NTabPane name="friend" tab="添加好友" />
        <NTabPane name="group" tab="添加群聊" />
      </NTabs>
      <NInputGroup>
        <NInputGroupLabel :style="{ color: themeVars.textColor3 }">
          <NovaIcon icon="icon-park-outline:search" :size="16" />
        </NInputGroupLabel>
        <NInput v-model:value="addSearchText" clearable :placeholder="addSearchLabel" />
      </NInputGroup>
      <NScrollbar class="flex-1" style="max-height: calc(70vh - 140px);">
        <div v-if="addMode === 'friend'" class="pr-1">
          <NList v-if="addSearchText.trim() ? addSearchResults.length : addFriendUsers.length" hoverable>
            <NListItem v-for="user in (addSearchText.trim() ? addSearchResults : addFriendUsers)" :key="user.id" class="im-list-item">
              <div class="flex items-center gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ user.avatarText || user.name?.charAt(0) || '?' }}</NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="im-ellipsis text-sm font-600">{{ user.name }}</div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ user.subtitle || (user.title + ' · ' + user.department) }}</div>
                </div>
                <NTag v-if="getFriendStatus(user) === 'accepted'" :bordered="false" size="small" type="success">已添加</NTag>
                <NTag v-else-if="getFriendStatus(user) === 'pending'" :bordered="false" size="small" type="warning">申请中</NTag>
                <NButton v-else size="small" tertiary @click="applyForFriend(user)">申请好友</NButton>
              </div>
            </NListItem>
          </NList>
          <div v-else-if="addSearchLoading" class="py-8 text-center text-sm" :style="{ color: themeVars.textColor3 }">搜索中...</div>
          <NEmpty v-else class="py-8" description="暂无用户结果" />
        </div>
        <div v-if="addMode === 'group'" class="pr-1">
          <NList v-if="addGroupResults.length" hoverable>
            <NListItem v-for="group in addGroupResults" :key="group.id" class="im-list-item">
              <div class="flex items-center gap-3 px-4 py-3">
                <NAvatar round :size="40" class="shrink-0">{{ group.avatarText }}</NAvatar>
                <div class="min-w-0 flex-1">
                  <div class="im-ellipsis text-sm font-600">{{ group.name }}</div>
                  <div class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ group.memberCount }} 人 · {{ group.description }}</div>
                </div>
                <NTag v-if="getGroupStatus(group) === 'accepted'" :bordered="false" size="small" type="success">已加入</NTag>
                <NTag v-else-if="getGroupStatus(group) === 'pending'" :bordered="false" size="small" type="warning">申请中</NTag>
                <NButton v-else size="small" tertiary @click="applyForGroup(group)">申请入群</NButton>
              </div>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-8" description="暂无群组结果" />
        </div>
      </NScrollbar>
      <div class="flex items-center gap-3 text-xs" :style="{ color: themeVars.textColor3 }">
        <span>结果 {{ addModeCount }} 项</span>
      </div>
    </div>
  </NModal>
</template>
