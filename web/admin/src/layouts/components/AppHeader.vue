<script setup lang="ts">
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import { useAppStore } from '@/stores/app'
import HeaderRightContent from '@/components/pro/HeaderRightContent.vue'
import { getIconComponent } from '@/utils/icons'

const app = useAppStore()
const route = useRoute()
const isMobile = ref(false)
let mediaQuery: MediaQueryList | null = null

const title = computed(() => String(route.meta.title || '控制台'))
const titleIcon = computed(() => getIconComponent(route.meta.icon))
const triggerIcon = computed(() => {
  if (isMobile.value) {
    return MenuUnfoldOutlined
  }
  return app.collapsed ? MenuUnfoldOutlined : MenuFoldOutlined
})
const triggerLabel = computed(() => {
  if (isMobile.value) {
    return '打开侧边菜单'
  }
  return app.collapsed ? '展开侧边栏' : '折叠侧边栏'
})

function updateIsMobile(event?: MediaQueryListEvent) {
  isMobile.value = event?.matches ?? Boolean(mediaQuery?.matches)
}

function handleTriggerClick() {
  if (isMobile.value) {
    app.setMobileMenuOpen(true)
    return
  }
  app.toggleCollapsed()
}

onMounted(() => {
  mediaQuery = window.matchMedia('(max-width: 1023px)')
  updateIsMobile()
  mediaQuery.addEventListener('change', updateIsMobile)
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', updateIsMobile)
})
</script>

<template>
  <div class="flex h-14 items-center gap-2 px-2 lg:px-4">
    <AButton
      :aria-label="triggerLabel"
      class="inline-flex! h-10! w-10! shrink-0! items-center! justify-center! rounded-2! p-0! text-18px! text-slate-600! hover:bg-slate-100! focus-visible:ring-2 focus-visible:ring-brand-500/35 dark:text-zinc-300! dark:hover:bg-zinc-800!"
      type="text"
      @click="handleTriggerClick"
    >
      <component :is="triggerIcon" />
    </AButton>

    <div class="min-w-0 flex-1 overflow-hidden">
      <div class="inline-flex max-w-full items-center gap-2 text-14px text-slate-900 font-600 leading-5 dark:text-zinc-100 lg:text-15px">
        <component :is="titleIcon" v-if="titleIcon" class="shrink-0 text-16px" />
        <span class="min-w-0 truncate">{{ title }}</span>
      </div>
    </div>

    <HeaderRightContent />
  </div>
</template>
