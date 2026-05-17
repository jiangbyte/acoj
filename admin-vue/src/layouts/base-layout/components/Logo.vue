<template>
  <div class="logo flex items-center justify-center gap-2 h-16 w-full">
    <div
      class="w-8 h-8 bg-primary-dynamic rounded flex items-center justify-center text-white font-bold text-sm shrink-0"
    >
      H
    </div>
    <span v-if="!collapsed" class="logo-text text-lg font-semibold whitespace-nowrap">
      Hei Admin
    </span>
  </div>
</template>

<script setup lang="ts">
defineProps<{ collapsed: boolean }>()
</script>
