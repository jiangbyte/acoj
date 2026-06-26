<script setup lang="ts">
import { ref, watch } from 'vue'
import { ProLayout, useLayoutMenu } from 'pro-naive-ui'
import { useI18n } from 'vue-i18n'
import { useAppStore, useRouteStore } from '@/stores'
import {
  BackTop,
  Breadcrumb,
  CollapaseButton,
  FullScreen,
  LanguageSwitch,
  Logo,
  MobileDrawer,
  Notices,
  Search,
  TabBar,
  UserCenter,
} from './components'
import Content from './Content.vue'

const appStore = useAppStore()
const routeStore = useRouteStore()
const { locale } = useI18n()

const { layout, activeKey } = useLayoutMenu({
  mode: 'vertical',
  accordion: true,
  menus: routeStore.menus,
} as never)

watch(
  () => routeStore.currentMenuPath,
  (currentMenuPath) => {
    activeKey.value = currentMenuPath
  },
  { immediate: true },
)

const showMobileDrawer = ref(false)
</script>

<template>
  <ProLayout
    v-model:collapsed="appStore.collapsed"
    mode="vertical"
    :is-mobile="appStore.isMobile"
    :show-logo="!appStore.isMobile"
    show-footer
    show-tabbar
    nav-fixed
    show-nav
    show-sidebar
    :nav-height="60"
    :tabbar-height="45"
    :footer-height="40"
    :sidebar-width="240"
    :sidebar-collapsed-width="64"
  >
    <template #logo>
      <Logo />
    </template>

    <template #nav-left>
      <template v-if="appStore.isMobile">
        <Logo />
      </template>
      <template v-else>
        <div class="h-full flex-y-center gap-1 p-x-sm">
          <CollapaseButton />
          <Breadcrumb />
        </div>
      </template>
    </template>

    <template #nav-right>
      <div class="h-full flex-y-center gap-1 p-x-xl">
        <template v-if="appStore.isMobile">
          <Search />
          <Notices />
          <LanguageSwitch />
          <DarkModeSwitch />
          <UserCenter @open-mobile-drawer="showMobileDrawer = true" />
        </template>
        <template v-else>
          <Search />
          <Notices />
          <FullScreen />
          <LanguageSwitch />
          <DarkModeSwitch />
          <UserCenter />
        </template>
      </div>
    </template>

    <template #sidebar>
      <n-menu :key="locale" v-bind="layout.verticalMenuProps" :collapsed-width="64" />
    </template>

    <template #sidebar-extra>
      <n-scrollbar class="flex-[1_0_0]">
        <n-menu :key="locale" v-bind="layout.verticalExtraMenuProps" :collapsed-width="64" />
      </n-scrollbar>
    </template>

    <template #tabbar>
      <TabBar />
    </template>

    <template #footer>
      <div class="flex-center h-full">
        {{ appStore.footerText }}
      </div>
    </template>

    <Content />
    <BackTop />

    <MobileDrawer v-model:show="showMobileDrawer">
      <n-menu v-bind="layout.verticalMenuProps" :collapsed="false" />
    </MobileDrawer>
  </ProLayout>
</template>
