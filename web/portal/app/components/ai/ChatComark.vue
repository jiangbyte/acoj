<script setup lang="ts">
defineProps<{
  markdown: string
  streaming?: boolean
}>()
</script>

<template>
  <div class="prose prose-sm dark:prose-invert max-w-none whitespace-pre-wrap leading-6 text-sm">
    {{ markdown }}
  </div>
  <span
    v-if="streaming"
    class="inline-block size-1.5 bg-primary rounded-full animate-pulse ml-0.5"
  />
</template>
