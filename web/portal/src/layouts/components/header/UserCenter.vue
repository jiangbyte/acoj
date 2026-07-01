<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { renderIcon } from '@/utils/icon'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    t('app.nickname'),
)

const avatar = computed(() => authStore.userInfo?.avatar || undefined)
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const options = computed<DropdownOption[]>(() => [
  {
    label: t('app.user_center.title'),
    key: 'userCenter',
    icon: renderIcon('icon-park-outline:user'),
  },
  {
    label: t('app.my_space'),
    key: 'mySpace',
    icon: renderIcon('icon-park-outline:user-positioning'),
  },
  {
    type: 'divider',
    key: 'divider-1',
  },
  {
    label: t('app.login_out'),
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
      title: t('app.login_out_title'),
      content: t('app.login_out_content'),
      positiveText: t('common.confirm'),
      negativeText: t('common.cancel'),
      onPositiveClick: async () => {
        await authStore.logout()
        window.$message.success(t('app.login_out_success'))
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
