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
    label: '个人中心',
    key: 'userCenter',
    icon: renderIcon('icon-park-outline:user'),
  },
  {
    label: '我的空间',
    key: 'mySpace',
    icon: renderIcon('icon-park-outline:user-positioning'),
  },
  {
    type: 'divider',
    key: 'divider-1',
  },
  {
    label: '退出登录',
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
      title: '退出登录',
      content: '确定退出当前账号？',
      positiveText: '确认',
      negativeText: '取消',
      onPositiveClick: async () => {
        await authStore.logout()
        window.$message.success('已退出登录')
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
