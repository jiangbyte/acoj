<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const router = useRouter()
const toast = useToast()
const { isLoggedIn, user, logout } = useAuth()
const authStore = useAuthStore()

const navItems: NavigationMenuItem[] = [
  { label: '首页', to: '/' },
  { label: '功能', to: '/features' },
  { label: '关于', to: '/about' },
  { label: 'AI Chat', to: '/ai/chat' },
]

const dropdownItems = [
  { label: '个人中心', icon: 'i-lucide-user', to: '/usercenter' },
  { type: 'separator' as const },
  { label: '退出登录', icon: 'i-lucide-log-out', onSelect: handleLogout },
]

async function handleLogout() {
  await logout()
  toast.add({ title: '已退出登录', color: 'success' })
  router.push('/')
}
</script>

<template>
  <UHeader :ui="{ container: '!max-w-none' }">
    <template #left>
      <div class="flex items-center gap-6">
        <AppLogo />
        <UNavigationMenu
          :items="navItems"
          class="hidden lg:flex"
          :ui="{
            link: 'before:!bg-transparent !bg-transparent data-[state=active]:text-primary data-[state=active]:font-semibold',
          }"
        />
      </div>
    </template>
    <template #right>
      <div class="flex items-center gap-2">
        <template v-if="isLoggedIn">
          <UDropdownMenu :items="dropdownItems" :content="{ side: 'bottom', align: 'end' }">
            <button
              class="flex items-center gap-2 rounded-full p-1 pr-2 transition-colors hover:bg-muted"
            >
              <UAvatar
                :src="authStore.userInfo?.avatar ?? undefined"
                :alt="user?.username"
                size="sm"
                :icon="authStore.userInfo?.avatar ? undefined : 'i-lucide-user'"
              />
              <span class="text-sm text-muted hidden sm:inline max-w-24 truncate">{{
                authStore.userInfo?.nickname || user?.username
              }}</span>
            </button>
          </UDropdownMenu>
        </template>
        <template v-else>
          <UButton label="登录" color="neutral" variant="ghost" size="sm" to="/auth/login" />
          <UButton label="注册" color="primary" variant="solid" size="sm" to="/auth/register" />
        </template>
        <UColorModeButton />
      </div>
    </template>
    <template #body>
      <div class="space-y-2 px-2 pt-4">
        <UNavigationMenu
          :items="navItems"
          orientation="vertical"
          class="-mx-2.5"
          :ui="{
            link: 'before:!bg-transparent !bg-transparent data-[state=active]:text-primary data-[state=active]:font-semibold',
          }"
        />
      </div>
    </template>
  </UHeader>
</template>
