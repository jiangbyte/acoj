<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { renderIcon } from '@/utils/icon'
import { resolveFileUrl } from '@/utils'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const homePath = import.meta.env.VITE_HOME_PATH

const displayName = computed(() => authStore.userInfo?.nickname || '-')

const avatar = computed(() => resolveFileUrl(authStore.userInfo?.avatar))
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

// 桌面端用户菜单项。项目首页使用环境配置路径。
const options = computed<DropdownOption[]>(() => [
  {
    label: 'User Center',
    key: 'userCenter',
    icon: renderIcon('icon-park-outline:user'),
  },
  {
    type: 'divider',
    key: 'divider-1',
  },
  {
    label: 'Project Home',
    key: 'home',
    icon: renderIcon('icon-park-outline:home'),
  },
  {
    label: 'Log Out',
    key: 'logout',
    icon: renderIcon('icon-park-outline:logout'),
  },
])

// 根据下拉菜单 key 分发行为；保留弹窗确认可以避免后续接入真实登出时误触。
function handleSelect(key: string | number) {
  if (key === 'userCenter') {
    router.push('/usercenter')
  }
  if (key === 'home') {
    router.push(homePath)
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
    <div class="flex items-center gap-2">
      <n-avatar
        v-if="avatar"
        round
        class="cursor-pointer"
        :src="avatar"
        :img-props="avatarImgProps"
      />
      <n-avatar v-else round class="cursor-pointer">
        <NovaIcon icon="icon-park-outline:user" />
      </n-avatar>
      <span class="hidden md:inline">{{ displayName }}</span>
    </div>
  </n-dropdown>
</template>
