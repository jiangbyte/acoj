<script setup lang="ts">
import type { MenuOption } from 'naive-ui'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { computed, h, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore, useAuthStore, useRouteStore } from '@/stores'
import { renderIcon } from '@/utils/icon'
import { BackTop, LanguageSwitch, Logo, UserCenter } from './components'
import Content from './Content.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const routeStore = useRouteStore()
const { t } = useI18n()
const copyright = import.meta.env.VITE_COPYRIGHT_INFO

const showMobileMenu = ref(false)

const staticMenus = computed<MenuOption[]>(() => [
  {
    key: '/home',
    label: () => h(RouterLink, { to: '/home' }, { default: () => t('app.portal.home') }),
    icon: renderIcon('icon-park-outline:home'),
  },
])

const menus = computed<MenuOption[]>(() => [
  ...staticMenus.value,
  ...(routeStore.menus as MenuOption[]),
])

const activeKey = computed(() => route.path)

watch(
  () => appStore.isMobile,
  (isMobile) => {
    if (!isMobile) {
      showMobileMenu.value = false
    }
  },
)

function handleMenuUpdate(key: string) {
  showMobileMenu.value = false
  router.push(key)
}

function goLogin() {
  router.push({
    path: '/auth/login',
    query: route.path.startsWith('/auth') ? undefined : { redirect: route.fullPath },
  })
}
</script>

<template>
  <n-el tag="div" class="min-h-screen bg-[var(--body-color)] text-[var(--text-color-base)]">
    <header
      class="portal-header sticky top-0 z-50 border-b border-[var(--border-color)] bg-[var(--card-color)]"
    >
      <div class="h-16 flex items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
        <div class="min-w-0 flex items-center gap-3">
          <CommonWrapper v-if="appStore.isMobile" @click="showMobileMenu = true">
            <NovaIcon icon="icon-park-outline:hamburger-button" />
          </CommonWrapper>
          <Logo v-if="!appStore.isMobile" />
        </div>

        <nav class="hidden min-w-0 flex-1 justify-center md:flex">
          <n-menu
            mode="horizontal"
            responsive
            :value="activeKey"
            :options="menus"
            class="max-w-full border-0 bg-transparent"
          />
        </nav>

        <div class="shrink-0 flex items-center justify-end gap-1 sm:gap-2">
          <LanguageSwitch />
          <DarkModeSwitch />
          <UserCenter v-if="authStore.isLogin" />
          <n-button v-else type="primary" :focusable="false" @click="goLogin">
            {{ t('auth.login') }}
          </n-button>
        </div>
      </div>
    </header>

    <Content />

    <footer class="border-t border-[var(--border-color)] bg-[var(--card-color)]">
      <div
        class="flex flex-col gap-3 px-4 py-6 text-sm text-[var(--text-color-3)] sm:px-6 md:flex-row md:items-center md:justify-between lg:px-8"
      >
        <div class="font-medium text-[var(--text-color-2)]">
          {{ t('app.portal.footer_title') }}
        </div>
        <div>{{ copyright }}</div>
      </div>
    </footer>

    <n-drawer v-model:show="showMobileMenu" placement="left" :width="300">
      <n-drawer-content :native-scrollbar="false" body-content-class="p-0!">
        <template #header>
          <Logo />
        </template>
        <n-el tag="div" class="min-h-full bg-[var(--card-color)] text-[var(--text-color-base)]">
          <n-menu
            :value="activeKey"
            :options="menus"
            class="border-0"
            @update:value="handleMenuUpdate"
          />
        </n-el>
      </n-drawer-content>
    </n-drawer>

    <BackTop class="z-999" />
  </n-el>
</template>

<style scoped>
.portal-header {
  box-shadow:
    0 1px 0 color-mix(in srgb, var(--border-color) 72%, transparent),
    0 6px 16px rgba(15, 23, 42, 0.035);
}

:deep(.dark) .portal-header,
:deep(html.dark) .portal-header {
  box-shadow:
    0 1px 0 color-mix(in srgb, var(--border-color) 76%, transparent),
    0 6px 16px rgba(0, 0, 0, 0.16);
}
</style>
