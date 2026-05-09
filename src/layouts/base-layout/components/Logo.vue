<template>
  <div class="logo flex items-center gap-2 px-4 h-16" :style="{ width: collapsed ? '64px' : '200px' }">
    <div class="w-8 h-8 bg-primary-dynamic rounded flex items-center justify-center text-white font-bold text-sm">H</div>
    <span v-if="!collapsed" class="logo-text text-lg font-semibold whitespace-nowrap">Hei Admin</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{ collapsed: boolean }>()
</script>
