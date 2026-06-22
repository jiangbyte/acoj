<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import { getIconComponent } from '@/utils/icons'
import { translateWithFallback } from '@/utils/i18n'

const route = useRoute()
const { t } = useI18n()

function isMenuItem(item: unknown): item is { label: string; labelKey?: string; icon?: string | null } {
  return Boolean(item && typeof item === 'object' && 'label' in item)
}

const breadcrumbItems = computed(() =>
  (route.meta.breadcrumb?.length ? route.meta.breadcrumb : route.matched)
    .filter((item) => isMenuItem(item) || item.meta?.title)
    .map((item) => ({
      title: isMenuItem(item)
        ? translateWithFallback(item.labelKey, item.label)
        : translateWithFallback(item.meta.titleKey, String(item.meta.title)),
      icon: getIconComponent(isMenuItem(item) ? item.icon : item.meta.icon),
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
