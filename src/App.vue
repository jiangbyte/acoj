<template>
  <AConfigProvider :theme="themeConfig">
    <router-view />
  </AConfigProvider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/store/app'

const app = useAppStore()

const themeConfig = computed(() => ({
  token: {
    colorPrimary: app.colorPrimary,
  },
}))
</script>
