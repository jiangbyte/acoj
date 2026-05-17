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
      :selected-keys="selectedKeys"
      :open-keys="openKeys"
      :theme="menuTheme"
      :items="menuItems"
      @click="handleMenuClick"
      @open-change="handleOpenChange"
    />
  </ALayoutSider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore, useRouteStore } from '@/store'
import Logo from '../components/Logo.vue'
import { useMenu } from './useMenu'

const route = useRoute()
const app = useAppStore()
const routeStore = useRouteStore()

const { openKeys, menuItems, handleMenuClick, handleOpenChange } = useMenu()

const selectedKeys = computed(() => {
  const path = route.path
  const name = route.name as string | undefined

  // Exact match in sidebar
  if (menuItems.value.some((item: any) => item.key === path)) return [path]

  // Build lookup maps from the full resource tree
  const routeMap = new Map<string, any>() // route_path → resource
  const idMap = new Map<string, any>() // id → resource
  const codeMap = new Map<string, any>() // code → resource
  const visibleSet = new Set<string>()

  function walk(nodes: any[]) {
    for (const n of nodes) {
      if (n.route_path) routeMap.set(n.route_path, n)
      if (n.id) idMap.set(n.id, n)
      if (n.code) codeMap.set(n.code, n)
      if (n.children) walk(n.children)
    }
  }
  function collectVisible(items: any[]) {
    for (const item of items) {
      visibleSet.add(item.key)
      if (item.children) collectVisible(item.children)
    }
  }
  walk(routeStore.menus)
  collectVisible(menuItems.value)

  // Collect all candidates (visible menu keys that could match)
  const candidates = new Set<string>()

  // 1. Resource tree ancestry via route.name (resource code)
  if (name) {
    const resource = codeMap.get(name)
    if (resource) {
      if (visibleSet.has(resource.route_path)) candidates.add(resource.route_path)
      let currentId = resource.parent_id
      while (currentId) {
        const parent = idMap.get(currentId)
        if (!parent) break
        if (visibleSet.has(parent.route_path)) candidates.add(parent.route_path)
        currentId = parent.parent_id
      }
    }
  }

  // 2. URL prefix matching on sidebar items
  function findDeepest(items: any[], p: string): string | undefined {
    for (const item of items) {
      if (p === item.key) return item.key
      if (p.startsWith(item.key + '/')) {
        if (item.children) {
          const m = findDeepest(item.children, p)
          if (m) return m
        }
        return item.key
      }
    }
    return undefined
  }
  const prefixMatch = findDeepest(menuItems.value, path)
  if (prefixMatch) candidates.add(prefixMatch)

  // Pick the most specific (longest path) candidate
  if (candidates.size > 0) {
    const sorted = [...candidates].sort((a, b) => b.length - a.length)
    return [sorted[0]]
  }

  return [path]
})

const siderTheme = computed(() => (app.theme === 'light' ? 'light' : 'dark'))
const menuTheme = computed(() => siderTheme.value)
</script>
