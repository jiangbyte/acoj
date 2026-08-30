<!-- Author: Charlie -->

<script setup lang="ts">
import { useAuthStore } from '@/stores'
import { computed, onMounted, reactive } from 'vue'
import { displayValue, mapNames } from '../composables/useProfile'
import '../profile.css'
import AvatarUploadModal from './AvatarUploadModal.vue'

const authStore = useAuthStore()
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const state = reactive({
  loading: false,
  avatarModalShow: false,
  me: null as any,
})

const avatarUrl = computed(() => state.me?.avatar || undefined)
const displayName = computed(() => {
  const nickname = String(state.me?.nickname ?? '').trim()
  return nickname || '-'
})
const deptText = computed(() => mapNames(state.me?.dept_id_names))
const roleText = computed(() => mapNames(state.me?.role_id_names))
const groupText = computed(() => mapNames(state.me?.group_id_names))

onMounted(async () => {
  await refresh()
})

async function refresh() {
  state.loading = true
  try {
    state.me = await authStore.refreshUserInfo()
  } finally {
    state.loading = false
  }
}

defineExpose({ refresh })
</script>

<template>
  <aside class="profile__summary">
    <NSpin :show="state.loading">
      <div class="profile__avatar-card">
        <button
          class="profile__avatar-edit"
          type="button"
          title="更换头像"
          @click="state.avatarModalShow = true"
        >
          <NAvatar
            v-if="avatarUrl"
            round
            :size="160"
            :src="avatarUrl"
            :img-props="avatarImgProps"
          />
          <NAvatar
            v-else
            round
            :size="160"
          >
            <NovaIcon
              icon="icon-park-outline:user"
              :size="64"
            />
          </NAvatar>
          <span class="profile__avatar-badge">
            <NovaIcon
              icon="icon-park-outline:edit"
              :size="14"
            />
            编辑
          </span>
        </button>
        <div class="profile__avatar-name">
          {{ displayName }}
        </div>
        <div class="profile__avatar-account">
          {{ state.me?.account || '-' }}
        </div>
        <NDescriptions
          class="profile__avatar-desc"
          :column="1"
          label-placement="left"
          size="small"
        >
          <NDescriptionsItem label="部门">
            {{ displayValue(deptText) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="角色">
            {{ displayValue(roleText) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="用户组">
            {{ displayValue(groupText) }}
          </NDescriptionsItem>
        </NDescriptions>
      </div>
    </NSpin>

    <AvatarUploadModal
      v-model:show="state.avatarModalShow"
      :avatar="avatarUrl"
      @uploaded="refresh"
    />
  </aside>
</template>
