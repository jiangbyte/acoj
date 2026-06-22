<script setup lang="ts">
import {
  CloseOutlined,
  CustomerServiceOutlined,
  AppstoreOutlined,
  BulbFilled,
  BulbOutlined,
  HomeOutlined,
  LoginOutlined,
  MenuOutlined,
  NotificationOutlined,
  UserAddOutlined,
} from '@ant-design/icons-vue'
import { computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

import { APP_TITLE } from '@/config/app'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import LocaleSwitch from './LocaleSwitch.vue'
import UserDropdown from './UserDropdown.vue'

interface NavItem {
  key: string
  labelKey: string
  to: string
  icon: typeof HomeOutlined
}

const app = useAppStore()
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const navItems: NavItem[] = [
  { key: 'home', labelKey: 'layout.nav.home', to: '/', icon: HomeOutlined },
  { key: 'notice', labelKey: 'layout.nav.notice', to: '/#notice', icon: NotificationOutlined },
  { key: 'features', labelKey: 'layout.nav.features', to: '/#features', icon: AppstoreOutlined },
  { key: 'support', labelKey: 'layout.nav.support', to: '/#support', icon: CustomerServiceOutlined },
]

const selectedKeys = computed(() => {
  if (route.hash === '#notice') {
    return ['notice']
  }
  if (route.hash === '#features') {
    return ['features']
  }
  if (route.hash === '#support') {
    return ['support']
  }
  if (route.path === '/') {
    return ['home']
  }
  return []
})
const menuItems = computed<MenuProps['items']>(() =>
  navItems.map((item) => ({
    key: item.key,
    label: t(item.labelKey),
    icon: () => h(item.icon),
  })),
)
const themeIcon = computed(() => (app.isDark ? BulbFilled : BulbOutlined))
const themeLabel = computed(() => (app.isDark ? t('layout.lightMode') : t('layout.darkMode')))

function openMobileMenu() {
  app.setMobileMenuOpen(true)
}

function closeMobileMenu() {
  app.setMobileMenuOpen(false)
}

function getNavTarget(key: string) {
  return navItems.find((item) => item.key === key)?.to || '/'
}

async function handleMenuClick({ key }: { key: string | number }) {
  await router.push(getNavTarget(String(key)))
  closeMobileMenu()
}
</script>

<template>
  <header
    class="sticky top-0 z-40 border-b border-slate-200/80 bg-white/92 shadow-sm backdrop-blur dark:border-zinc-800 dark:bg-zinc-950/88"
  >
    <div class="h-16 w-full flex items-center gap-3 px-4 sm:px-6 lg:px-10 xl:px-14">
      <RouterLink
        to="/"
        class="min-w-0 inline-flex items-center gap-3 text-slate-950 no-underline dark:text-white"
        @click="closeMobileMenu"
      >
        <span
          class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-2 bg-blue-600 text-18px text-white font-700 shadow-sm shadow-blue-600/20"
        >
          {{ APP_TITLE.charAt(0) }}
        </span>
        <span class="min-w-0 truncate text-17px font-700 leading-6">{{ APP_TITLE }}</span>
      </RouterLink>

      <nav class="ml-8 hidden min-w-0 flex-1 items-center lg:flex">
        <RouterLink
          v-for="item in navItems"
          :key="item.key"
          :to="item.to"
          class="inline-flex h-10 items-center gap-2 rounded-2 px-4 text-14px text-slate-600 font-500 no-underline transition hover:bg-slate-100 hover:text-slate-950 dark:text-zinc-300 dark:hover:bg-zinc-800 dark:hover:text-white"
          :class="selectedKeys.includes(item.key) ? 'bg-slate-100 text-slate-950 dark:bg-zinc-800 dark:text-white' : ''"
        >
          <component :is="item.icon" class="text-15px" />
          <span>{{ t(item.labelKey) }}</span>
        </RouterLink>
      </nav>

      <div class="ml-auto flex shrink-0 items-center gap-2">
        <LocaleSwitch class="hidden lg:inline-flex" />
        <AButton
          :aria-label="themeLabel"
          class="inline-flex! h-10! w-10! items-center! justify-center! rounded-2! p-0!"
          type="text"
          @click="app.toggleThemeMode"
        >
          <component :is="themeIcon" />
        </AButton>
        <UserDropdown v-if="auth.isAuthenticated" />
        <RouterLink v-if="!auth.isAuthenticated" to="/login" class="hidden lg:inline-flex">
          <AButton type="primary">
            <template #icon><LoginOutlined /></template>
            {{ t('layout.login') }}
          </AButton>
        </RouterLink>
        <RouterLink v-if="!auth.isAuthenticated" to="/register" class="hidden lg:inline-flex">
          <AButton>
            <template #icon><UserAddOutlined /></template>
            {{ t('layout.register') }}
          </AButton>
        </RouterLink>
        <AButton
          :aria-label="t('layout.openMenu')"
          class="inline-flex! h-10! w-10! items-center! justify-center! rounded-2! p-0! lg:hidden!"
          type="text"
          @click="openMobileMenu"
        >
          <MenuOutlined />
        </AButton>
      </div>
    </div>

    <ADrawer
      v-model:open="app.mobileMenuOpen"
      :body-style="{ padding: 0 }"
      :closable="false"
      placement="right"
      width="304"
    >
      <div class="flex h-full min-h-0 flex-col bg-white dark:bg-zinc-950">
        <div class="flex h-16 shrink-0 items-center justify-between border-b border-slate-200 px-4 dark:border-zinc-800">
          <RouterLink
            to="/"
            class="inline-flex min-w-0 items-center gap-3 text-slate-950 no-underline dark:text-white"
            @click="closeMobileMenu"
          >
            <span
              class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-2 bg-blue-600 text-18px text-white font-700"
            >
              {{ APP_TITLE.charAt(0) }}
            </span>
            <span class="truncate text-16px font-700">{{ APP_TITLE }}</span>
          </RouterLink>
          <AButton
            :aria-label="t('layout.closeMenu')"
            class="inline-flex! h-10! w-10! items-center! justify-center! rounded-2! p-0!"
            type="text"
            @click="closeMobileMenu"
          >
            <CloseOutlined />
          </AButton>
        </div>

        <AMenu
          :items="menuItems"
          :selected-keys="selectedKeys"
          class="border-0!"
          mode="inline"
          @click="handleMenuClick"
        />

        <div class="mt-auto grid gap-2 border-t border-slate-200 p-4 dark:border-zinc-800">
          <div class="mb-1 flex justify-center">
            <LocaleSwitch />
          </div>
          <div v-if="auth.isAuthenticated" class="flex justify-center">
            <UserDropdown />
          </div>
          <RouterLink v-if="!auth.isAuthenticated" to="/login" @click="closeMobileMenu">
            <AButton block type="primary">
              <template #icon><LoginOutlined /></template>
              {{ t('layout.login') }}
            </AButton>
          </RouterLink>
          <RouterLink v-if="!auth.isAuthenticated" to="/register" @click="closeMobileMenu">
            <AButton block>
              <template #icon><UserAddOutlined /></template>
              {{ t('layout.register') }}
            </AButton>
          </RouterLink>
        </div>
      </div>
    </ADrawer>
  </header>
</template>
