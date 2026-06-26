<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const loading = ref(false)

function handleReload() {
  loading.value = true
  appStore.reloadPage()
  window.setTimeout(() => {
    loading.value = false
  }, 800)
}
</script>

<template>
  <n-tooltip placement="bottom" trigger="hover">
    <template #trigger>
      <CommonWrapper @click="handleReload">
        <NovaIcon icon="icon-park-outline:refresh" :class="{ 'animate-spin': loading }" />
      </CommonWrapper>
    </template>
    刷新
  </n-tooltip>
</template>
