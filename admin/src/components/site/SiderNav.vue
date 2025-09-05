<script setup lang="ts">
import type { MenuInst } from 'naive-ui'
import { useRouterStore, useSiteStore } from '@/stores'

const siteStore = useSiteStore()
const routerStore = useRouterStore()

const route = useRoute()
const menuInstRef = ref<MenuInst | null>(null)
watch(
  () => route.path,
  () => {
    menuInstRef.value?.showOption(routerStore.activeMenu as string)
  },
  { immediate: true },
)
</script>

<template>
  <n-menu
    :options="routerStore.menus"
    :collapsed="siteStore.collapsed"
    :value="routerStore.activeMenu"
    :indent="20"
    :collapsed-width="64"
    :collapsed-icon-size="22"
  />
</template>

<style scoped></style>
