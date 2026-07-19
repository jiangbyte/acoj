<script setup lang="ts">
import { computed, inject, ref } from 'vue'
import type { MockMessage } from '../mock'
import { IM_ACTIONS_KEY, IM_DATA_KEY } from '../im-provide'
import { createGroupApi } from '../im-api'

const data = inject(IM_DATA_KEY)!
const actions = inject(IM_ACTIONS_KEY)!

const show = defineModel<boolean>('show', { required: true })

const createGroupName = ref('')
const createGroupDesc = ref('')
const createGroupPublic = ref(true)
const createGroupAvatarText = ref('群')
const createGroupInvitees = ref<string[]>([])
const showInviteFriendModal = ref(false)
const inviteSearchText = ref('')

const filteredInviteFriends = computed(() => {
  const keyword = inviteSearchText.value.trim().toLowerCase()
  if (!keyword) return data.friends
  return data.friends.filter((f) => f.name.toLowerCase().includes(keyword) || f.title?.toLowerCase().includes(keyword))
})

function handleGroupAvatarClick() {
  const chars = data.friends.map((f) => f.avatarText).filter(Boolean)
  const all = [...new Set(['群', ...chars, '组', '聊', '天', '新'])]
  const current = createGroupAvatarText.value
  const idx = all.indexOf(current)
  createGroupAvatarText.value = all[(idx + 1) % all.length]
}

function toggleGroupInvitee(friendId: string) {
  const idx = createGroupInvitees.value.indexOf(friendId)
  if (idx >= 0) createGroupInvitees.value.splice(idx, 1)
  else createGroupInvitees.value.push(friendId)
}

function removeGroupInvitee(friendId: string) {
  const idx = createGroupInvitees.value.indexOf(friendId)
  if (idx >= 0) createGroupInvitees.value.splice(idx, 1)
}

function closeCreateGroup() {
  show.value = false
  createGroupName.value = ''
  createGroupDesc.value = ''
  createGroupInvitees.value = []
  createGroupAvatarText.value = '群'
  inviteSearchText.value = ''
}

async function handleCreateGroup() {
  const name = createGroupName.value.trim()
  if (!name) return

  const memberRefs = createGroupInvitees.value
    .map((id) => {
      const friend = data.friends.find((f) => f.id === id)
      if (!friend) return null
      return { account_type: 'ADMIN', account_id: friend.id }
    })
    .filter(Boolean) as Array<{ account_type: string; account_id: string }>

  const ok = await createGroupApi({
    name,
    description: createGroupDesc.value.trim() || undefined,
    member_refs: memberRefs.length ? memberRefs : undefined,
  })

  if (!ok) {
    window.$message?.error?.('创建群聊失败')
    return
  }

  // Reload groups and threads from API
  try {
    const [{ myGroups }, { myThreads }] = await Promise.all([
      import('@/api/message'),
      import('@/api/message'),
    ])
    const [groupsRes, threadsRes] = await Promise.all([
      myGroups(),
      myThreads({ current: 1, size: 50 }).catch(() => null),
    ])
    if (groupsRes?.data?.length) {
      data.groups = groupsRes.data.map((g: any) => ({
        id: g.id ?? '',
        name: g.name ?? '',
        description: g.description ?? '',
        memberCount: g.member_count ?? 0,
        avatarText: (g.name ?? '').charAt(0) || '?',
        statusText: '活跃',
        threadId: g.id ? `thread-${g.id}` : '',
      }))
    }
    if (threadsRes?.data?.records?.length) {
      data.threads = threadsRes.data.records.map((t: any) => ({
        id: t.id ?? '',
        kind: t.thread_type === 'GROUP' ? 'group' : 'direct',
        title: t.title ?? '',
        subtitle: '',
        avatarText: (t.title ?? '').charAt(0) || '?',
        lastMessage: t.last_message?.content ?? '',
        lastMessageAt: t.last_message_at ?? t.created_at ?? new Date().toISOString(),
        unreadCount: t.unread_count ?? 0,
        pinned: false,
        muted: false,
        contactId: '',
        groupId: t.group_id,
      }))
    }
  } catch {}

  closeCreateGroup()
  window.$message?.success?.('群聊创建成功')

  // Find the newly created group by name and open its thread
  const newGroup = data.groups.find((g) => g.name === name)
  if (newGroup) actions.openThread(newGroup.threadId)
}
</script>

<template>
  <NModal v-model:show="show" preset="card" :bordered="false" draggable
    title="创建群聊" :mask-closable="false"
    style="width: min(480px, calc(100vw - 24px));"
  >
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <NAvatar round :size="56" class="shrink-0 cursor-pointer border-2 border-dashed" :style="{ borderColor: 'var(--border-color)' }" @click="handleGroupAvatarClick">
          {{ createGroupAvatarText }}
        </NAvatar>
        <div class="min-w-0 flex-1">
          <NInput v-model:value="createGroupName" placeholder="群聊名称（必填）" size="large" />
        </div>
      </div>
      <NInput v-model:value="createGroupDesc" type="textarea" placeholder="群聊简介" :autosize="{ minRows: 2, maxRows: 4 }" />
      <NCheckbox v-model:value="createGroupPublic">公开群组（允许搜索加入）</NCheckbox>
      <div class="flex items-center justify-between">
        <span class="text-sm" style="color: var(--text-color-3)">已邀请 {{ createGroupInvitees.length }} 人</span>
        <NButton size="small" @click="showInviteFriendModal = true">
          <template #icon><NovaIcon icon="icon-park-outline:add" :size="14" /></template>
          邀请好友
        </NButton>
      </div>
      <div v-if="createGroupInvitees.length" class="flex flex-wrap gap-2">
        <NTag v-for="id in createGroupInvitees" :key="id" closable :bordered="false" size="small" @close="removeGroupInvitee(id)">
          {{ data.friends.find((f) => f.id === id)?.name || id }}
        </NTag>
      </div>
      <div class="flex justify-end gap-3 pt-2">
        <NButton @click="closeCreateGroup">取消</NButton>
        <NButton type="primary" :disabled="!createGroupName.trim()" @click="handleCreateGroup">创建</NButton>
      </div>
    </div>
  </NModal>

  <NModal v-model:show="showInviteFriendModal" preset="card" :bordered="false"
    title="邀请好友" :mask-closable="false"
    style="width: min(400px, calc(100vw - 24px)); max-height: 60vh;"
    content-style="display: flex; flex-direction: column; padding: 0 16px 16px; min-height: 0;"
  >
    <div class="flex min-h-0 flex-1 flex-col gap-3">
      <NInput v-model:value="inviteSearchText" clearable placeholder="搜索好友" size="small" />
      <NScrollbar class="flex-1" style="max-height: 40vh;">
        <NList v-if="filteredInviteFriends.length" hoverable>
          <NListItem v-for="friend in filteredInviteFriends" :key="friend.id" class="im-list-item cursor-pointer" @click="toggleGroupInvitee(friend.id)">
            <div class="flex items-center gap-3 px-2 py-2">
              <NCheckbox :checked="createGroupInvitees.includes(friend.id)" @click.stop="toggleGroupInvitee(friend.id)" />
              <NAvatar round :size="36" class="shrink-0">{{ friend.avatarText }}</NAvatar>
              <div class="min-w-0 flex-1">
                <div class="im-ellipsis text-sm font-500">{{ friend.name }}</div>
                <div class="im-ellipsis text-xs" style="color: var(--text-color-3)">{{ friend.title }} · {{ friend.department }}</div>
              </div>
            </div>
          </NListItem>
        </NList>
        <NEmpty v-else description="暂无好友" />
      </NScrollbar>
      <div class="flex justify-end">
        <NButton size="small" @click="showInviteFriendModal = false">确定</NButton>
      </div>
    </div>
  </NModal>
</template>
