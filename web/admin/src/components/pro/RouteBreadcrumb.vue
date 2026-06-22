<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import { getIconComponent } from '@/utils/icons'
import { translateWithFallback } from '@/utils/i18n'

const route = useRoute()
const { t } = useI18n()

const breadcrumbItems = computed(() =>
  route.matched
    .filter((item) => item.meta?.title && item.path !== '/')
    .map((item) => ({
      title: translateWithFallback(item.meta.titleKey, String(item.meta.title)),
      icon: getIconComponent(item.meta.icon),
    })),
)

const visibleItems = computed(() =>
  breadcrumbItems.value.length
    ? breadcrumbItems.value
    : [{ title: translateWithFallback(route.meta.titleKey, String(route.meta.title || t('app.console'))), icon: getIconComponent(route.meta.icon) }],
)
</script>

<template>
  <ABreadcrumb>
    <ABreadcrumbItem v-for="item in visibleItems" :key="item.title">
      <span class="inline-flex items-center gap-1.5">
        <component :is="item.icon" v-if="item.icon" class="text-13px" />
        <span>{{ item.title }}</span>
      </span>
    </ABreadcrumbItem>
  </ABreadcrumb>
</template>
