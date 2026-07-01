<script setup lang="ts">
import { useAppStore } from '@/stores'

const appStore = useAppStore()
</script>

<template>
  <div class="w-full min-w-0">
    <router-view v-slot="{ Component, route }">
      <component :is="Component" v-if="appStore.loadFlag" :key="route.fullPath" />
    </router-view>
  </div>
</template>
