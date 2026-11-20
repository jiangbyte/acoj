<script setup lang="ts">
import { useSiteStore } from '@/stores'
import Header from '@/components/site/Header.vue'
import Footer from '@/components/site/Footer.vue'

const siteStore = useSiteStore()
</script>

<template>
  <n-layout
    has-sider
    embedded
  >
    <n-layout-sider
      bordered
      :collapsed="siteStore.collapsed"
      collapse-mode="width"
      :collapsed-width="64"
      content-style="display: flex; flex-direction: column; height: 100vh"
    >
      <Logo />
      <n-scrollbar class="flex-1">
        <SiderNav />
      </n-scrollbar>
    </n-layout-sider>
    <n-layout
      embedded
      :native-scrollbar="false"
    >
      <n-layout-header
        bordered
        class="w-full flex flex-col justify-between h-20 px-4 z-999"
        position="absolute"
      >
        <Header />
      </n-layout-header>
      <div class="h-[calc(100vh-2.5rem-5rem)] mt-20 flex flex-col">
        <router-view v-slot="{ Component, route }" class="flex-1">
          <component
            :is="Component"
            :key="route.fullPath"
          />
        </router-view>
      </div>
      <n-layout-footer
        bordered
        position="absolute"
        class="h-10 flex justify-center items-center"
      >
        <Footer />
      </n-layout-footer>
    </n-layout>
  </n-layout>
</template>
