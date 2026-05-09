<template>
  <div v-if="app.showBreadcrumb" class="layout-breadcrumb">
    <ABreadcrumb>
      <ABreadcrumbItem v-for="item in breadcrumbs" :key="item.path">
        <component :is="item.icon" v-if="item.icon && app.showBreadcrumbIcon" class="mr-1" />
        {{ item.title }}
      </ABreadcrumbItem>
    </ABreadcrumb>
  </div>
</template>

<script setup lang="ts">
import { h, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import * as Icons from '@ant-design/icons-vue'

const route = useRoute()
const app = useAppStore()

function resolveIcon(name: string) {
  const pascal = name.replace(/[-_]/g, '').replace(/^(\w)/, (_, c) => c.toUpperCase())
  const key = pascal.endsWith('Outlined') ? pascal : pascal + 'Outlined'
  return (Icons as any)[key] || null
}

const breadcrumbs = computed(() => {
  const matched = route.matched.filter((r) => r.path !== '/')
  return matched.map((r) => ({
    path: r.path,
    title: (r.meta?.title as string) || r.name as string || '',
    icon: r.meta?.icon ? resolveIcon(r.meta.icon as string) : null,
  }))
})
</script>
