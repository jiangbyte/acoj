<script setup lang="ts">
import { useMagicKeys } from '@vueuse/core'
import { useAppStore } from '@/stores'

const appStore = useAppStore()

useMagicKeys({
  passive: false,
  onEventFired(e) {
    if (e.key === 'F11' && e.type === 'keydown') {
      e.preventDefault()
      appStore.toggleFullScreen()
    }
  },
})
</script>

<template>
  <n-tooltip placement="bottom" trigger="hover">
    <template #trigger>
      <CommonWrapper @click="appStore.toggleFullScreen">
        <NovaIcon
          :icon="
            appStore.fullScreen ? 'icon-park-outline:off-screen' : 'icon-park-outline:full-screen'
          "
        />
      </CommonWrapper>
    </template>
    全屏
  </n-tooltip>
</template>
