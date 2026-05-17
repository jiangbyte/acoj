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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store'
import { resolveIcon } from '@/utils'

const route = useRoute()
const app = useAppStore()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.path !== '/')
  return matched
    .filter(r => (r.meta?.breadcrumb as boolean) ?? true)
    .map(r => ({
      path: r.path,
      title: (r.meta?.title as string) || (r.name as string) || '',
      icon: r.meta?.icon ? resolveIcon(r.meta.icon as string) : null,
    }))
})
</script>
