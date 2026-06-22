<script setup lang="ts">
import { LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { Modal } from 'ant-design-vue'
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const auth = useAuthStore()
const user = useUserStore()
const router = useRouter()
const { t } = useI18n()

const profileName = computed(
  () => user.profile?.nickname || user.me?.account_id || t('layout.profileFallback'),
)
const profileTitle = computed(() => user.profile?.level || t('layout.profileTitleFallback'))
const avatarText = computed(() => profileName.value.slice(0, 1).toUpperCase())

onMounted(() => {
  if (auth.isAuthenticated && !user.me) {
    user.ensureMe().catch(() => {
      auth.clearSession()
      user.clear()
    })
  }
})

function goProfile() {
  router.push('/profile')
}

function handleLogout() {
  Modal.confirm({
    title: t('layout.logout'),
    content: t('layout.logoutConfirm'),
    async onOk() {
      await auth.logout()
      await router.push('/')
    },
  })
}
</script>

<template>
  <ADropdown placement="bottomRight" :trigger="['click']">
    <button
      class="inline-flex h-10 cursor-pointer items-center gap-2 rounded-2 border-0 bg-transparent px-1.5 text-slate-700 transition hover:bg-slate-100 dark:text-zinc-200 dark:hover:bg-zinc-800 lg:px-2"
      type="button"
    >
      <AAvatar :size="28" class="bg-blue-600 text-white">
        {{ avatarText }}
      </AAvatar>
      <span class="hidden max-w-24 truncate text-13px font-500 lg:inline">{{ profileName }}</span>
    </button>
    <template #overlay>
      <AMenu :selected-keys="[]">
        <AMenuItem key="profile-card" disabled>
          <div class="flex min-w-52 items-center gap-3 py-1">
            <AAvatar :size="38" class="bg-blue-600 text-white">{{ avatarText }}</AAvatar>
            <div class="min-w-0">
              <div class="truncate text-slate-900 font-600 dark:text-zinc-100">{{ profileName }}</div>
              <div class="truncate text-12px text-slate-500 dark:text-zinc-400">{{ profileTitle }}</div>
            </div>
          </div>
        </AMenuItem>
        <AMenuDivider />
        <AMenuItem key="center" @click="goProfile">
          <UserOutlined />
          {{ t('layout.profileCenter') }}
        </AMenuItem>
        <AMenuDivider />
        <AMenuItem key="logout" @click="handleLogout">
          <LogoutOutlined />
          {{ t('layout.logout') }}
        </AMenuItem>
      </AMenu>
    </template>
  </ADropdown>
</template>
