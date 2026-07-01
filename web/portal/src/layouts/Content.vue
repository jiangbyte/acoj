<script setup lang="ts">
import { useAppStore } from '@/stores'

const appStore = useAppStore()
</script>

<template>
  <main class="min-h-[calc(100vh-136px)] bg-[var(--body-color)]">
    <router-view v-slot="{ Component, route }">
      <component :is="Component" v-if="appStore.loadFlag" :key="route.fullPath" />
    </router-view>
  </main>
</template>
