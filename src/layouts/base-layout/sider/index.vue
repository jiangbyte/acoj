<template>
  <ALayoutSider
    v-model:collapsed="app.collapsed"
    :width="220"
    :theme="siderTheme"
    collapsible
    :trigger="null"
    :style="{ overflow: 'auto' }"
  >
    <Logo :collapsed="app.collapsed" />
    <AMenu
      mode="inline"
      :selectedKeys="[route.path]"
      :openKeys="openKeys"
      :theme="menuTheme"
      :items="menuItems"
      @click="handleMenuClick"
      @openChange="handleOpenChange"
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

const siderTheme = computed(() => app.theme === 'light' ? 'light' : 'dark')
const menuTheme = computed(() => siderTheme.value)

const menuItems = computed(() => menuToItems(routeStore.menus))

// Expand parent submenu when navigating to a child page
watch(() => route.path, (path) => {
  if (app.collapsed) {
    openKeys.value = []
    return
  }
  for (const item of menuItems.value) {
    if (item.children?.length && item.children.some((c: any) => c.key === path)) {
      openKeys.value = [item.key]
      return
    }
  }
  openKeys.value = []
})

// 收起侧边栏时清空 openKeys，避免弹出子菜单
watch(() => app.collapsed, (collapsed) => {
  if (collapsed) openKeys.value = []
})

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}

function handleOpenChange(keys: string[]) {
  // Accordion: keep only the most recently opened submenu
  if (keys.length > 1) {
    const added = keys.filter(k => !openKeys.value.includes(k))
    openKeys.value = added.length ? [added[0]] : keys.slice(-1)
  } else {
    openKeys.value = keys
  }
}
</script>
