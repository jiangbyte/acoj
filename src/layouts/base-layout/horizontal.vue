<template>
  <div class="h-screen">
    <ALayout class="h-full">
      <ALayoutHeader class="layout-header flex items-center justify-between !px-4">
        <div class="flex items-center">
          <Logo :collapsed="false" />
          <AMenu
            mode="horizontal"
            :items="menuItems"
            :theme="menuTheme"
            @click="handleMenuClick"
            class="flex-1 border-0"
          />
        </div>
        <UserAvatar />
      </ALayoutHeader>
      <ALayout>
        <Breadcrumb />
        <Tab />
        <ALayoutContent class="layout-content layout-content-bg p-4">
          <router-view />
          <FooterBar v-if="app.showFooter" />
        </ALayoutContent>
      </ALayout>
    </ALayout>
    <ThemeDrawer />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRouteStore } from '@/store/route'
import { useAppStore } from '@/store/app'
import Logo from './components/Logo.vue'
import UserAvatar from './components/UserAvatar.vue'
import Breadcrumb from './breadcrumb/index.vue'
import Tab from './tab/index.vue'
import FooterBar from './components/FooterBar.vue'
import ThemeDrawer from './components/ThemeDrawer.vue'
import { menuToItems } from './sider/menuHelper'

const router = useRouter()
const routeStore = useRouteStore()
const app = useAppStore()
const menuItems = computed(() => menuToItems(routeStore.menus))
const menuTheme = computed(() => app.theme === 'realDark' ? 'dark' : 'light')

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>
