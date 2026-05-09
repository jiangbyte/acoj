<template>
  <div class="h-screen">
    <ALayout class="h-full">
      <Sider />
      <ALayout>
        <Header />
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
import { useAppStore } from '@/store/app'
import Sider from './sider/index.vue'
import Header from './header/index.vue'
import Breadcrumb from './breadcrumb/index.vue'
import Tab from './tab/index.vue'
import FooterBar from './components/FooterBar.vue'
import ThemeDrawer from './components/ThemeDrawer.vue'

const app = useAppStore()
</script>
