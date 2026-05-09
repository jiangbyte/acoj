<template>
  <ALayout class="h-screen">
    <ALayoutHeader class="flex items-center px-4 h-16 bg-white border-b">
      <Logo :collapsed="false" />
      <AMenu mode="horizontal" :items="menuItems" @click="handleMenuClick" class="flex-1 ml-4" />
      <UserAvatar />
    </ALayoutHeader>
    <ALayout>
      <Tab />
      <ALayoutContent class="p-4 bg-gray-50 overflow-auto">
        <router-view />
      </ALayoutContent>
    </ALayout>
  </ALayout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRouteStore } from '@/store/route'
import Logo from './components/Logo.vue'
import UserAvatar from './components/UserAvatar.vue'
import Tab from './tab/index.vue'
import { menuToItems } from './sider/menuHelper'

const router = useRouter()
const routeStore = useRouteStore()
const menuItems = computed(() => menuToItems(routeStore.menus))

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>
