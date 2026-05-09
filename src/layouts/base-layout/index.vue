<template>
  <ALayout class="h-screen">
    <VerticalLayout v-if="app.layoutMode === 'vertical'" />
    <HorizontalLayout v-else-if="app.layoutMode === 'horizontal'" />
    <div v-else class="flex-1 flex items-center justify-center text-gray-400">
      布局开发中
    </div>
  </ALayout>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app'
import VerticalLayout from './vertical.vue'
import HorizontalLayout from './horizontal.vue'

const app = useAppStore()
</script>
