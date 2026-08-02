<script setup lang="ts">
defineProps<{
  open: () => void
}>()
</script>

<template>
  <UButton icon="i-lucide-paperclip" color="neutral" variant="ghost" size="sm" @click="open">
    <span class="hidden sm:inline text-xs">附件</span>
  </UButton>
</template>
