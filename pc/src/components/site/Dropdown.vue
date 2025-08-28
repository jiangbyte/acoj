<script lang="ts" setup>
import { NDropdown, useDialog } from 'naive-ui'
import { useTokenStore, useUserStore } from '@/stores'
import { AesCrypto, iconRender } from '@/utils'

const useToken = useTokenStore()
const router = useRouter()
const dialog = useDialog()
const useUser = useUserStore()
function handleSelect(key: string | number) {
  if (key === 'logout') {
    dialog.info({
      title: '登出',
      content: '你确定要登出吗？',
      positiveText: '登出',
      negativeText: '取消',
      onPositiveClick: async () => {
        useToken.logoutAndRedirect()
      },
    })
  }
  else if (key === 'profile') {
    const userId = useUser.getUserId
    // router.push('/profile')
    router.push({
      name: 'user',
      query: { userId: AesCrypto.encrypt(String(userId)) },
    })
  }
  else if (key === 'security') {
    router.push('/security')
  }
}

const options = [
  {
    label: '个人信息',
    key: 'profile',
    icon: iconRender('icon-park-outline:id-card-h'),
  },
  {
    label: '账户安全',
    key: 'security',
    icon: iconRender('icon-park-outline:people-safe'),
  },
  {
    label: '登出',
    key: 'logout',
    icon: iconRender('icon-park-outline:logout'),
  },
]
</script>

<template>
  <NDropdown
    trigger="click"
    :options="options"
    @select="handleSelect"
  >
    <slot name="avatar" />
  </NDropdown>
</template>

<style>

</style>
