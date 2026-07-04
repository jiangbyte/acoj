<script setup lang="ts">
import type { MenuOption } from 'naive-ui'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { computed, h, ref, watch } from 'vue'
import { useAppStore, useAuthStore, useRouteStore } from '@/stores'
import { renderIcon } from '@/utils/icon'
import { BackTop, Logo, MessageBell, MobileDrawer, UserCenter } from './components'
import Content from './Content.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const routeStore = useRouteStore()
const copyright = import.meta.env.VITE_COPYRIGHT_INFO
const showMobileMenu = ref(false)

const staticMenus = computed<MenuOption[]>(() => [
  {
    key: '/home',
    label: () => h(RouterLink, { to: '/home' }, { default: () => 'Home' }),
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
  <n-el
    tag="div"
    class="h-full w-full min-w-0 flex flex-col overflow-hidden bg-[var(--body-color)] text-[var(--text-color-base)]"
  >
    <header
      class="portal-header z-50 shrink-0 border-b border-[var(--border-color)] bg-[var(--card-color)]"
    >
      <div
        class="h-16 w-full min-w-0 flex items-center justify-between"
        :class="appStore.isMobile ? 'gap-2 px-2' : 'gap-4 px-8'"
      >
        <div class="min-w-0 shrink-0 flex items-center gap-3">
          <CommonWrapper v-if="appStore.isMobile" @click="showMobileMenu = true">
            <NovaIcon icon="icon-park-outline:hamburger-button" />
          </CommonWrapper>
          <Logo v-else />
        </div>

        <nav v-if="!appStore.isMobile" class="min-w-0 flex flex-1 justify-center">
          <n-menu
            mode="horizontal"
            responsive
            :value="activeKey"
            :options="menus"
            class="max-w-full border-0 bg-transparent"
          />
        </nav>

        <div
          class="shrink-0 flex items-center justify-end"
          :class="appStore.isMobile ? 'gap-1' : 'gap-2'"
        >
          <DarkModeSwitch />
          <MessageBell v-if="authStore.isLogin" />
          <UserCenter v-if="authStore.isLogin" />
          <n-button v-else type="primary" :focusable="false" @click="goLogin">
            {{ 'Sign In' }}
          </n-button>
        </div>
      </div>
    </header>

    <main
      class="min-h-0 w-full min-w-0 flex-1 overflow-x-hidden overflow-y-auto bg-[var(--body-color)]"
    >
      <Content />

      <footer class="border-t border-[var(--border-color)] bg-[var(--card-color)]">
        <div
          class="w-full min-w-0 flex gap-3 py-6 text-sm text-[var(--text-color-3)]"
          :class="
            appStore.isMobile ? 'flex-col px-4' : 'flex-row items-center justify-between px-8'
          "
        >
          <div class="font-medium text-[var(--text-color-2)]">
            {{ 'Enterprise Portal' }}
          </div>
          <div>{{ copyright }}</div>
        </div>
      </footer>
    </main>

    <MobileDrawer v-model:show="showMobileMenu">
      <n-menu
        :value="activeKey"
        :options="menus"
        class="border-0"
        @update:value="handleMenuUpdate"
      />
    </MobileDrawer>

    <BackTop class="z-999" />
  </n-el>
</template>

<style scoped>
.portal-header {
  box-shadow:
    0 1px 0 color-mix(in srgb, var(--border-color) 72%, transparent),
    0 6px 16px rgba(15, 23, 42, 0.035);
}

:global(html.dark) .portal-header {
  box-shadow:
    0 1px 0 color-mix(in srgb, var(--border-color) 76%, transparent),
    0 6px 16px rgba(0, 0, 0, 0.16);
}
</style>
