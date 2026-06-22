<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import RouteBreadcrumb from './RouteBreadcrumb.vue'

const route = useRoute()

const title = computed(() => String(route.meta.title || '页面'))
const description = computed(() => String(route.meta.description || ''))
const hideHeader = computed(() => Boolean(route.meta.hideHeader))
</script>

<template>
  <div class="min-h-full">
    <div v-if="!hideHeader" class="border-b border-slate-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
      <div class="px-4 py-4 lg:px-6">
        <RouteBreadcrumb />
        <div class="mt-2">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div class="min-w-0 flex-1">
              <h1 class="m-0 truncate text-20px text-slate-900 font-600 leading-8 dark:text-zinc-100">
                {{ title }}
              </h1>
              <p v-if="description" class="m-0 mt-1 text-14px text-slate-500 leading-6 dark:text-zinc-400">
                {{ description }}
              </p>
            </div>
            <div v-if="$slots.extra" class="shrink-0">
              <slot name="extra" />
            </div>
          </div>
          <div v-if="$slots.tabs" class="mt-4">
            <slot name="tabs" />
          </div>
        </div>
      </div>
    </div>
    <div class="p-2 lg:p-4">
      <slot />
    </div>
  </div>
</template>
