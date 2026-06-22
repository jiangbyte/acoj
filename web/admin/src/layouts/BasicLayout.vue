<script setup lang="ts">
import { theme } from 'ant-design-vue'
import { computed } from 'vue'

import AppHeader from './components/AppHeader.vue'
import AppLogo from './components/AppLogo.vue'
import SideMenu from './components/SideMenu.vue'
import MultiTabBar from '@/components/pro/MultiTabBar.vue'
import PageContainer from '@/components/pro/PageContainer.vue'
import { useAppStore } from '@/stores/app'

const app = useAppStore()
const { token } = theme.useToken()
const layoutMetrics = {
  sidebarWidth: 224,
  sidebarCollapsedWidth: 80,
  drawerWidth: 272,
  headerHeight: 56,
  tabBarHeight: 40,
} as const
const menuTheme = computed(() => (app.isDark ? 'dark' : 'light'))
const headerOffset = layoutMetrics.headerHeight + layoutMetrics.tabBarHeight
const panelStyle = computed(() => ({
  background: token.value.colorBgContainer,
  borderColor: token.value.colorBorderSecondary,
}))
const pageStyle = computed(() => ({
  background: token.value.colorBgLayout,
}))
const siderWidth = computed(() =>
  app.collapsed ? layoutMetrics.sidebarCollapsedWidth : layoutMetrics.sidebarWidth,
)
const layoutStyle = computed(() => ({
  ...pageStyle.value,
  '--layout-sidebar-offset': `${siderWidth.value}px`,
}))
const fixedHeaderStyle = computed(() => ({
  ...panelStyle.value,
}))
const contentStyle = computed(() => ({
  paddingTop: `${headerOffset}px`,
  minHeight: '100vh',
}))
</script>

<template>
  <ALayout class="admin-layout min-h-screen text-slate-900 dark:text-zinc-100" :style="layoutStyle">
    <ALayoutSider
      v-model:collapsed="app.collapsed"
      :collapsed-width="layoutMetrics.sidebarCollapsedWidth"
      :trigger="null"
      :width="layoutMetrics.sidebarWidth"
      collapsible
      class="fixed! bottom-0! left-0! top-0! z-40 hidden! h-screen! min-h-0 overflow-hidden border-r shadow-sm lg:block!"
      :style="panelStyle"
    >
      <div class="flex h-full min-h-0 flex-col">
        <AppLogo :collapsed="app.collapsed" :dark="app.isDark" />
        <div class="min-h-0 flex-1 overflow-x-hidden overflow-y-auto">
          <SideMenu :collapsed="app.collapsed" :theme="menuTheme" />
        </div>
      </div>
    </ALayoutSider>

    <ADrawer
      v-model:open="app.mobileMenuOpen"
      :body-style="{ padding: 0 }"
      :closable="false"
      :width="layoutMetrics.drawerWidth"
      placement="left"
    >
      <div class="flex h-full min-h-0 flex-col" :style="panelStyle">
        <AppLogo :dark="app.isDark" />
        <div class="min-h-0 flex-1 overflow-x-hidden overflow-y-auto">
          <SideMenu :theme="menuTheme" @click="app.setMobileMenuOpen(false)" />
        </div>
      </div>
    </ADrawer>

    <ALayout
      class="min-h-screen min-w-0 lg:transition-[padding-left] lg:duration-200"
      :style="pageStyle"
    >
      <div
        class="admin-layout__header fixed left-0 right-0 top-0 z-30 shrink-0 border-b shadow-sm backdrop-blur lg:transition-[left] lg:duration-200"
        :style="fixedHeaderStyle"
      >
        <AppHeader />
        <MultiTabBar />
      </div>
      <ALayoutContent
        class="admin-layout__content min-w-0 lg:transition-[padding-left] lg:duration-200"
        :style="contentStyle"
      >
        <PageContainer>
          <RouterView />
        </PageContainer>
      </ALayoutContent>
    </ALayout>
  </ALayout>
</template>

<style scoped>
@media (min-width: 1024px) {
  .admin-layout__header {
    left: var(--layout-sidebar-offset);
  }

  .admin-layout__content {
    padding-left: var(--layout-sidebar-offset);
  }
}
</style>
