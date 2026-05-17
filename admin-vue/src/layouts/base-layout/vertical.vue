<template>
  <div class="h-screen">
    <ALayout class="h-full">
      <Sider v-if="!app.isMobile" />
      <ALayout>
        <Header
          @toggle-mobile-menu="showMobileDrawer = true"
          @toggle-user-drawer="showUserDrawer = true"
        />
        <Breadcrumb />
        <Tab />
        <ALayoutContent class="layout-content layout-content-bg p-4">
          <router-view v-slot="{ Component }">
            <keep-alive :include="routeStore.cacheRoutes">
              <component :is="Component" />
            </keep-alive>
          </router-view>
          <FooterBar v-if="app.showFooter" />
        </ALayoutContent>
      </ALayout>
    </ALayout>
    <MobileDrawer v-if="app.isMobile" v-model:open="showMobileDrawer" />
    <UserDrawer v-if="app.isMobile" v-model:open="showUserDrawer" />
    <ThemeDrawer />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore, useRouteStore } from '@/store'
import Sider from './sider/index.vue'
import Header from './header/index.vue'
import Breadcrumb from './breadcrumb/index.vue'
import Tab from './tab/index.vue'
import FooterBar from './components/FooterBar.vue'
import ThemeDrawer from './components/ThemeDrawer.vue'
import MobileDrawer from './components/MobileDrawer.vue'
import UserDrawer from './components/UserDrawer.vue'

const app = useAppStore()
const routeStore = useRouteStore()

const showMobileDrawer = ref(false)
const showUserDrawer = ref(false)
</script>
