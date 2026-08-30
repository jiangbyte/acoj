<!-- Author: Charlie -->

<script setup lang="ts">
import type { MenuOption } from 'naive-ui'
import { computed, nextTick, ref, watch } from 'vue'
import { ProLayout, useLayoutMenu } from 'pro-naive-ui'
import { useAppStore, useRouteStore } from '@/stores'
import {
  BackTop,
  Breadcrumb,
  CollapaseButton,
  FullScreen,
  Logo,
  MobileDrawer,
  ModuleSwitch,
  Notices,
  Search,
  TabBar,
  ThemeSettingsButton,
  UserCenter,
} from './components'
import Content from './Content.vue'
import NoticePopupHost from './components/common/NoticePopupHost.vue'
import SidebarMenuProvider from './components/common/SidebarMenuProvider.vue'

const appStore = useAppStore()
const routeStore = useRouteStore()

const menus = computed(() => routeStore.menus as MenuOption[])

const accordion = computed(() => appStore.accordionMenu)

const { layout, activeKey } = useLayoutMenu({
  mode: 'vertical',
  accordion,
  menus,
})

watch(
  () => routeStore.currentMenuPath,
  (currentMenuPath) => {
    activeKey.value = currentMenuPath
  },
  { immediate: true },
)

/** 开启排他后收拢到当前激活项祖先，避免已展开的多组菜单残留 */
watch(
  () => appStore.accordionMenu,
  async (enabled) => {
    if (!enabled || !activeKey.value) {
      return
    }
    const key = activeKey.value
    activeKey.value = null
    await nextTick()
    activeKey.value = key
  },
)

const showMobileDrawer = ref(false)

function handleMobileMenuSelect(key: string | number) {
  activeKey.value = String(key)
  showMobileDrawer.value = false
}
</script>

<template>
  <ProLayout
    v-model:collapsed="appStore.collapsed"
    mode="vertical"
    :is-mobile="appStore.isMobile"
    :show-logo="!appStore.isMobile"
    :show-footer="false"
    :show-tabbar="appStore.showTabbar"
    nav-fixed
    show-nav
    show-sidebar
    :nav-height="56"
    :tabbar-height="40"
    :sidebar-width="240"
    :sidebar-collapsed-width="64"
    :builtin-theme-overrides="appStore.layoutThemeOverrides"
    content-class="layout-content-surface"
    tabbar-class="layout-tabbar-surface"
  >
    <template #logo>
      <Logo sidebar />
    </template>

    <template #nav-left>
      <template v-if="appStore.isMobile">
        <div class="h-full flex-y-center gap-3 p-x-sm">
          <CommonWrapper @click="showMobileDrawer = true">
            <NovaIcon icon="icon-park-outline:hamburger-button" />
          </CommonWrapper>
          <ModuleSwitch />
        </div>
      </template>
      <template v-else>
        <div class="h-full flex-y-center gap-1 p-x-sm">
          <CollapaseButton />
          <ModuleSwitch />
          <Breadcrumb v-if="appStore.showBreadcrumb" />
        </div>
      </template>
    </template>

    <template #nav-right>
      <div class="h-full flex-y-center gap-1 p-x-xl">
        <template v-if="appStore.isMobile">
          <Search />
          <Notices />
          <ThemeSettingsButton />
          <DarkModeSwitch />
          <UserCenter />
        </template>
        <template v-else>
          <Search />
          <Notices />
          <FullScreen />
          <ThemeSettingsButton />
          <DarkModeSwitch />
          <UserCenter />
        </template>
      </div>
    </template>

    <template #sidebar>
      <SidebarMenuProvider>
        <n-scrollbar class="sidebar-menu-scrollbar">
          <n-menu
            v-bind="layout.verticalMenuProps"
            :accordion="appStore.accordionMenu"
            :collapsed-width="64"
          />
        </n-scrollbar>
      </SidebarMenuProvider>
    </template>

    <template #sidebar-extra>
      <n-scrollbar class="flex-[1_0_0]">
        <n-menu
          v-bind="layout.verticalExtraMenuProps"
          :accordion="appStore.accordionMenu"
          :collapsed-width="64"
        />
      </n-scrollbar>
    </template>

    <template
      v-if="appStore.showTabbar"
      #tabbar
    >
      <TabBar />
    </template>

    <Content />
    <BackTop class="z-999" />
    <NoticePopupHost />

    <MobileDrawer v-model:show="showMobileDrawer">
      <n-menu
        v-bind="layout.verticalMenuProps"
        :accordion="appStore.accordionMenu"
        :collapsed="false"
        :collapsed-width="64"
        :on-update-value="handleMobileMenuSelect"
      />
    </MobileDrawer>
  </ProLayout>
</template>

<style scoped>
:deep(.n-pro-layout__sidebar) {
  min-height: 0;
  overflow: hidden;
}

/* 滚动容器：SidebarMenuProvider 已撑满 sidebar，这里让 scrollbar 占满并滚动 */
.sidebar-menu-scrollbar {
  min-height: 0;
  flex: 1 1 0;
}

/*
 * 深色侧边栏：仅作用于侧边栏容器（aside），不影响顶栏/内容区。
 * --pro-layout-color 同时用于顶栏背景，因此在此作用域内单独覆盖。
 * 配色引用 style.css 中的模式感知变量：浅色模式深蓝灰、暗黑模式更深一档并与内容协调。
 */
:deep(.n-pro-layout__aside) {
  --pro-layout-color: var(--sidebar-bg);
  --pro-layout-border-color: var(--sidebar-border);
  /* 侧边栏内滚动条浅色滑块（深底） */
  --app-scrollbar-thumb-color: var(--sidebar-scrollbar-thumb);
  --app-scrollbar-thumb-color-hover: var(--sidebar-scrollbar-thumb-hover);
}

/* 多标签栏白底，与顶栏连贯；底部分隔线压住内容灰底 */
:deep(.layout-tabbar-surface) {
  background: var(--card-color) !important;
  border-top: 0 !important;
  border-bottom: 1px solid color-mix(in srgb, #000 6%, transparent) !important;
}

html.dark :deep(.layout-tabbar-surface) {
  border-bottom-color: color-mix(in srgb, #fff 8%, transparent) !important;
}

:deep(.layout-content-surface) {
  background: var(--body-color) !important;
}
</style>
