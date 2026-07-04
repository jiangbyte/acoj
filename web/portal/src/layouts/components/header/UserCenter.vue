<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { renderIcon } from '@/utils/icon'
import { resolveFileUrl } from '@/utils'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const displayName = computed(() => authStore.userInfo?.nickname || '-')

const avatar = computed(() => resolveFileUrl(authStore.userInfo?.avatar))
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const options = computed<DropdownOption[]>(() => [
  {
    label: 'User Center',
    key: 'userCenter',
    icon: renderIcon('icon-park-outline:user'),
  },
  {
    label: 'My Space',
    key: 'mySpace',
    icon: renderIcon('icon-park-outline:user-positioning'),
  },
  {
    type: 'divider',
    key: 'divider-1',
  },
  {
    label: 'Log Out',
    key: 'logout',
    icon: renderIcon('icon-park-outline:logout'),
  },
])

function handleSelect(key: string | number) {
  if (key === 'userCenter') {
    router.push({ name: 'usercenter' })
  }
  if (key === 'mySpace') {
    router.push({ name: 'my-space' })
  }
  if (key === 'logout') {
    window.$dialog.info({
      title: 'Log Out',
      content: 'Log out of the current account?',
      positiveText: 'Confirm',
      negativeText: 'Cancel',
      onPositiveClick: async () => {
        await authStore.logout()
        window.$message.success('Logged out')
      },
    })
  }
}
</script>

<template>
  <n-dropdown trigger="click" :options="options" @select="handleSelect">
    <button
      class="min-w-0 inline-flex items-center gap-2 border-0 bg-transparent p-1 text-[var(--text-color-base)] cursor-pointer"
      type="button"
    >
      <n-avatar v-if="avatar" round :size="32" :src="avatar" :img-props="avatarImgProps" />
      <n-avatar v-else round :size="32">
        <NovaIcon icon="icon-park-outline:user" />
      </n-avatar>
      <span class="hidden max-w-28 truncate text-sm font-medium lg:inline">
        {{ displayName }}
      </span>
    </button>
  </n-dropdown>
</template>
