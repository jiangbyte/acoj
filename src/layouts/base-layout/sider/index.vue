<template>
  <ALayoutSider v-model:collapsed="app.collapsed" :width="220" theme="light" collapsible>
    <Logo :collapsed="app.collapsed" />
    <AMenu
      mode="inline"
      :selectedKeys="[route.path]"
      :openKeys="openKeys"
      theme="light"
      :items="menuItems"
      @click="handleMenuClick"
    />
  </ALayoutSider>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useRouteStore } from '@/store/route'
import Logo from '../components/Logo.vue'
import { menuToItems } from './menuHelper'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const routeStore = useRouteStore()

const openKeys = ref<string[]>([])

const menuItems = computed(() => menuToItems(routeStore.menus))

watch(() => route.path, (path) => {
  const segments = path.split('/').filter(Boolean)
  openKeys.value = segments.slice(0, -1).map((_, i) => '/' + segments.slice(0, i + 1).join('/'))
}, { immediate: true })

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>
