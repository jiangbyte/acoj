<script setup lang="ts">
import { useTabStore } from '@/stores'
import type { RouteLocationNormalized } from 'vue-router'
import { Icon } from '@iconify/vue'
import type { IconifyIcon } from '@iconify/vue'

const scrollContainer = ref<HTMLElement | null>(null)

function handleWheel(event: WheelEvent) {
  if (event.deltaY !== 0) {
    event.preventDefault()
    if (scrollContainer.value) {
      scrollContainer.value.scrollLeft += event.deltaY
    }
  }
}

onMounted(() => {
  const container = scrollContainer.value
  if (container) {
    container.addEventListener('wheel', handleWheel)
  }
})

onUnmounted(() => {
  const container = scrollContainer.value
  if (container) {
    container.removeEventListener('wheel', handleWheel)
  }
})

const router = useRouter()
function handleTab(route: RouteLocationNormalized) {
  router.push(route.path)
}

const tabStore = useTabStore()
function closeHandle(path: string) {
  tabStore.closeTab(path)
}
</script>

<template>
  <div ref="scrollContainer" class="flex gap-2 overflow-x-scroll overflow-y-hidden">
    <n-tag
      v-for="item in tabStore.pinedTabs"
      :key="item.path"
      :type="tabStore.currentPath === item.path ? 'info' : 'default'"
      @click="handleTab(item)"
      @close="closeHandle(item.path)"
    >
      <template #icon>
        <Icon :icon="(item.meta.icon as string | IconifyIcon)" />
      </template>
      {{ item.meta.title }}
    </n-tag>
    <n-tag
      v-for="item in tabStore.tabs"
      :key="item.path"
      closable
      :type="tabStore.currentPath === item.path ? 'info' : 'default'"
      @click="handleTab(item)"
      @close="closeHandle(item.path)"
    >
      <template #icon>
        <Icon :icon="(item.meta.icon as string | IconifyIcon)" />
      </template>
      {{ item.meta.title }}
    </n-tag>
  </div>
</template>

<style>

</style>
