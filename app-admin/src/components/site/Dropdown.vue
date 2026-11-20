<script lang="ts" setup>
import { NDropdown, useDialog } from 'naive-ui'
import { useTokenStore } from '@/stores'
import { iconRender } from '@/utils'

const useToken = useTokenStore()
const router = useRouter()
const dialog = useDialog()
function handleSelect(key: string | number) {
  if (key === 'logout') {
    dialog.info({
      title: 'Sign Out',
      content: 'Are you sure you want to sign out?',
      positiveText: 'Sign Out',
      negativeText: 'Cancel',
      onPositiveClick: async () => {
        useToken.logoutAndRedirect()
      },
    })
  }
  else if (key === 'profile') {
    router.push('/mine')
  }
  // else if (key === 'security') {
  //   router.push('/settings/security')
  // }
}

const options = [
  {
    label: 'Profile',
    key: 'profile',
    icon: iconRender('icon-park-outline:id-card-h'),
  },
  // {
  //   label: 'Account Security',
  //   key: 'security',
  //   icon: iconRender('icon-park-outline:people-safe'),
  // },
  {
    label: 'Sign Out',
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
