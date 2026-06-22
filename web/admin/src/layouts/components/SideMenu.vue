<script setup lang="ts">
import type { MenuProps } from 'ant-design-vue'
import { computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { getIconComponent } from '@/utils/icons'
import { useRouteStore } from '@/stores/route'
import type { RouteMenuItem } from '@/types/route'
import { translateWithFallback } from '@/utils/i18n'

defineProps<{
  collapsed?: boolean
  theme?: 'dark' | 'light'
}>()

const route = useRoute()
const router = useRouter()
const routeStore = useRouteStore()
const { locale } = useI18n()

function buildMenuItems(items: RouteMenuItem[]): MenuProps['items'] {
  return items.map((item) => {
    const icon = getIconComponent(item.icon)
    return {
      key: item.key,
      label: translateWithFallback(item.labelKey, item.label),
      icon: icon ? () => h(icon) : undefined,
      children: item.children ? buildMenuItems(item.children) : undefined,
    }
  })
}

const menuItems = computed(() => {
  locale.value
  return buildMenuItems(routeStore.menus)
})
const selectedKeys = computed(() => [String(route.meta.activeMenu || route.path)])
const openKeys = computed(() =>
  route.matched.map((item) => item.path).filter((item) => item !== '/'),
)
const handleClick: MenuProps['onClick'] = ({ key }) => {
  const nextPath = String(key)
  if (/^https?:\/\//.test(nextPath)) {
    window.open(nextPath, '_blank', 'noopener,noreferrer')
    return
  }
  if (nextPath !== route.path) {
    router.push(nextPath)
  }
}
</script>

<template>
  <AMenu
    :items="menuItems"
    :open-keys="collapsed ? [] : openKeys"
    :selected-keys="selectedKeys"
    :theme="theme"
    :inline-indent="20"
    mode="inline"
    class="side-menu w-full"
    @click="handleClick"
  />
</template>
