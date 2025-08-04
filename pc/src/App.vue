<script setup lang="ts">
import { useSiteStore } from '@/stores'
import { dateZhCN, zhCN } from 'naive-ui'

const siteStore = useSiteStore()
</script>

<template>
  <n-config-provider
    inline-theme-disabled
    :theme-overrides="siteStore.themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
    :breakpoints="{ s: 640, m: 768, l: 1024 }"
  >
    <NaiveProvider>
      <router-view />
    </NaiveProvider>
  </n-config-provider>
</template>

<style scoped>

</style>
